package com.zkl.ZKLRussian.control.note_old.database;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SQLite extends Database<SQLiteDatabase> {
	public SQLite(SQLiteDatabase sqLiteDatabase) {
		super(sqLiteDatabase);
	}
	
	/**
	 * 打开一个数据库
	 * @param file 数据库的目标路径，若传入null可表示在内存中创建临时数据库
	 * @param openMode 数据库打开的方式
	 * @param openCallback 用于处理数据库打开过程中遇到的各种情况
	 * @return 一个数据库对象；若打开失败了则返回null
	 */@Nullable
	static public SQLite openFromFile(
		@Nullable File file, @NonNull final OpenMode openMode, @Nullable OpenCallback openCallback,
		@Nullable SQLiteDatabase.CursorFactory cursorFactory , @Nullable DatabaseErrorHandler errorHandler ) {
		if (openCallback == null) {
			openCallback = new OpenCallback() {
				@Override public boolean onNeedWipe(File file) { return openMode !=OpenMode.read; }
				@Override public void onWiped() { }
				@Override public boolean onNeedCreation(File file) { return openMode !=OpenMode.read; }
				@Override public void onCreated(Database database) { }
				@Override public void onError(Exception exception) { }
			};
		}

		try {
			if (file == null) return new SQLite(SQLiteDatabase.create(cursorFactory));
			switch (openMode) {
				case read:
					return new SQLite(
						SQLiteDatabase.openDatabase(file.getPath(), cursorFactory, SQLiteDatabase.OPEN_READONLY, errorHandler)
					);
				case write_passively:
					try {
						return new SQLite(
							SQLiteDatabase.openDatabase(file.getPath(), cursorFactory, SQLiteDatabase.OPEN_READWRITE, errorHandler)
						);
					} catch (Exception e) {
						return null;
					}
				case write_lenient:
					SQLite re;
					try {
						re = new SQLite(
							SQLiteDatabase.openDatabase(file.getPath(), cursorFactory, SQLiteDatabase.OPEN_READWRITE, errorHandler)
						);
					} catch (Exception e) {
						re = openFromFile(file, OpenMode.write_override, openCallback, cursorFactory, errorHandler);
					}
					return re;
				case write_override:
					file.getParentFile().mkdirs();
					if (file.exists() && file.isFile()) {
						if (openCallback.onNeedWipe(file)) {
							boolean deleted = file.delete();
							if (deleted) {
								openCallback.onWiped();
							} else {
								return null;
							}
						}
					}
					if (openCallback.onNeedCreation(file)) {
						SQLite database = new SQLite(
							SQLiteDatabase.openDatabase(file.getPath(), cursorFactory, SQLiteDatabase.CREATE_IF_NECESSARY, errorHandler)
						);
						openCallback.onCreated(database);
						return database;
					}
					return null;
			}
		} catch (Exception e) {
			openCallback.onError(e);
		}
		return null;
    }
	@Nullable
	static public SQLite openFromFile(@Nullable File file, OpenMode openMode, @Nullable OpenCallback openCallback) {
		return openFromFile(file, openMode, openCallback, null, null);
	}
	@Nullable
	static public SQLite openFromRAM(@Nullable SQLiteDatabase.CursorFactory cursorFactory) {
		return openFromFile(null, OpenMode.write_override, null, cursorFactory, null);
	}
	@Nullable
	static public SQLite openFromRAM() {
		return openFromFile(null, OpenMode.write_override, null, null, null);
	}
	@Nullable
	static public SQLite openOrCreateFromFile(File file) {
		return openFromFile(file, OpenMode.write_lenient, null);
	}
	@Nullable
	static public SQLite openReadableFromFile(File file) {
		return openFromFile(file, OpenMode.read, null);
	}
	@Nullable
	static public SQLite openWritablePassively(File file) {
		return openFromFile(file, OpenMode.write_passively,null);
	}
	

	
	@Override public int getVersion(){
		return getDriver().getVersion();
	}
	@Override public void setVersion(int newVersion){
		getDriver().setVersion(newVersion);
		
	}
	@Override public String getPath(){
		return getDriver().getPath();
	}


	public boolean enableWAL(){ return getDriver().enableWriteAheadLogging(); }
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void disableWAL(){ getDriver().disableWriteAheadLogging(); }
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public boolean isWALEnabled(){ return getDriver().isWriteAheadLoggingEnabled(); }
	@Override public boolean processTransaction(boolean lock, TransactionProcessor transactionProcessor) {
		boolean success=false;
		try {
			if(lock){
				getDriver().beginTransaction();
			}else{
				getDriver().beginTransactionNonExclusive();
			}
			success=transactionProcessor.onProcess(this);
			if (success) {
				getDriver().setTransactionSuccessful();
			}
		} catch (Exception e) {
			transactionProcessor.onError(this,e);
		}finally {
			getDriver().endTransaction();
		}
		return success;
	}
	@Override public void close() { getDriver().close(); }


	@Override public void execSQL(String sql){
		getDriver().execSQL(sql);
	}
	
	@Override public List<String> getTableNames() {
		Cursor cursor= getDriver().rawQuery("select name from sqlite_master where type='table' order by name", null);
		ArrayList<String> re=new ArrayList<>(cursor.getCount());
		while(cursor.moveToNext()){
			re.add(cursor.getString(0));
		}
		cursor.close();
		return re;
	}
	@Override public boolean existTable(String tableName) {
		Cursor cursor= getDriver().rawQuery("select name from sqlite_master where type='table' and tbl_name='"+tableName+"'",null);
		boolean exist=cursor.getCount()==1;
		cursor.close();
		return exist;
	}
	@Override @Nullable
	public Table getTable(String tableName) {
		if(existTable(tableName)) {
			return new Table(this, tableName);
		}else return null;
	}
	@Override public void createTable(TableStruct tableStruct) {
		String sql ="create table "+tableStruct.tableName+"(";
		for(TableStruct.Column column:tableStruct.columns){
			sql+=column.name+" "+column.typeExpress+",";
		}
		sql=sql.substring(0,sql.length()-1)+")";
		getDriver().execSQL(sql);
	}
	@Override public void renameTable(String oldTableName, String newTableName) {
		copyTable(oldTableName, newTableName);
		dropTableIfExists(oldTableName);
	}
	@Override public void copyTable(String oldTableName, String newTableName) {
		getDriver().execSQL("CREATE TABLE "+newTableName+" AS SELECT * FROM "+oldTableName);
	}
	@Override public void dropTableIfExists(String tableName) {
		if (!tableName.equals("sqlite_sequence")) {
			getDriver().execSQL("drop table if exists " + tableName);
		}
	}
	@Override public void dropAllTables() {
		processTransaction(true, new TransactionProcessor() {
			@Override public boolean onProcess(Database database) {
				for(String name: getTableNames()) {
					if (!name.equals("sqlite_sequence")) {
						getDriver().execSQL("drop table " + name);
					}
				}
				return true;
			}
			
			@Override
			public void onError(Database database, Exception e) {
				e.printStackTrace();
			}
		});
	}

	@Override public List<String> getIndexNames() {
		Cursor cursor= getDriver().rawQuery("select name from sqlite_master where type='index' order by name", null);
		ArrayList<String> re=new ArrayList<>(cursor.getCount());
		while(cursor.moveToNext()){
			re.add(cursor.getString(0));
		}
		cursor.close();
		return re;
	}
	@Override public boolean existIndex(String tableName) {
		return getTableNames().contains(tableName);
	}
	@Override public void createIndex(String indexName, String tableName, String column) {
		execSQL("create index " + indexName+ " on " + tableName + "(" + column + ")");
	}
	@Override public void dropIndex(String indexName) {
		execSQL("drop index if exists " + indexName);
	}


	@Override public Cursor rawSelect(String sql) {
		return getDriver().rawQuery(sql,null);
	}
	@Override public Cursor select(String table, Selection selection) {
		return getDriver().query(table,selection.includedColumns, selection.whereExpression,null,
			selection.groupByExpression,selection.havingExpression,selection.orderByExpression,selection.limit);
	}

	@Override public long insert(String table, ContentValues contentValues) {
		return getDriver().insert(table,null,contentValues);
	}
	@Override public int modify(String table, String whereExpression, ContentValues contentValues) {
		return getDriver().update(table, contentValues, whereExpression, null);
	}
	@Override public int delete(String table, String whereExpression) {
		return getDriver().delete(table,whereExpression,null);
	}
}
