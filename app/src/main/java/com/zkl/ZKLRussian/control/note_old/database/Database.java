package com.zkl.ZKLRussian.control.note_old.database;


import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.Nullable;

import java.io.File;
import java.util.List;

abstract public class Database<Driver> {
	public enum OpenMode{
		/**
		 * 仅读取，不会对任何数据作修改。也不会创建数据库。
		 */read,
	    /**
	     * 被动写入模式，如果文件符合条件则打开，否则返回null
	     */write_passively,
		/**
		 * 强制写入模式，会直接创建新的文件再使用（若原来有文件则会被删除）
		 */write_override,
		/**
		 * 兼容写入模式。若原来有可用文件则会直接使用若；
		 * 如果原有文件无法使用则试图删除后再重建数据库；
		 * 若没有文件则会创建
		 */write_lenient
	}
    public interface OpenCallback{
	    /**
	     * 表示有文件冲突，需要删除原来的文件。
	     * @param file 文件的路径
	     * @return 若返回true则表示同意删除；返回false表示不删除，但可能导致打开数据库失败
	     */boolean onNeedWipe(File file);
	    /**
	     * 当删除原文件成功时调用
	     */void onWiped();
	    /**
	     * 在需要创建新的数据库文件时调用。
	     * @param file 新的数据库文件将要被创建的目标路径。
	     * @return 返回true表示同意创建；返回false表示不同意创建，但可能导致数据库打开失败
	     */boolean onNeedCreation(File file);
	    /**
	     * 当数据库被创建成功时调用
	     * @param database 新创建的数据库
	     */void onCreated(Database database);
	    /**
	     * 当打开过程出现错误时会被调用。
	     * @param exception 出现的错误
	     */void onError(Exception exception);
    }

	private Driver driver;
	private void setDriver(Driver sqLiteDatabase) { this.driver = sqLiteDatabase; }
	public Driver getDriver() { return driver; }
	public Database(Driver driver){ this.setDriver(driver); }



	public abstract int getVersion();
	public abstract void setVersion(int newVersion);
	public abstract String getPath();


	public interface TransactionProcessor{
		/**
		 * @return 返回true表示正常执行commit，返回false表示要中断操作，回滚
		 */boolean onProcess(Database database);
		/**
		 * 该方法被调用后一定会回滚
		 */void onError(Database database, Exception e);
	}
	public abstract boolean processTransaction(boolean lock, TransactionProcessor transactionProcessor);
	public abstract void close();

	public abstract void execSQL(String sql);

	public abstract List<String> getTableNames();
	public abstract boolean existTable(String tableName);
	@Nullable
	public abstract Table getTable(String tableName);
	public abstract void createTable(TableStruct tableStruct);
	public abstract void renameTable(String oldTableName, String newTableName);
	public abstract void copyTable(String oldTableName, String newTableName);
	/**
	 * 在删除该表时，若有相关的索引，也会跟着自动删除
	 */
	public abstract void dropTableIfExists(String tableName);
	public abstract void dropAllTables();

	public abstract List<String> getIndexNames();
	public abstract boolean existIndex(String tableName);
	/**
	 * 创建一个索引
	 */
	public abstract void createIndex(String indexName, String tableName, String column);
	public abstract void dropIndex(String indexName);

	public abstract Cursor rawSelect(String sql);
	public abstract Cursor select(String table,Selection selection);
	public Cursor select(String table,String[] includedColumns,String where,String groupBy,String having,String orderBy){
		Selection selection = new Selection();
		selection.includedColumns=includedColumns;
		selection.whereExpression = where;
		selection.groupByExpression=groupBy;
		selection.havingExpression=having;
		selection.groupByExpression=groupBy;
		return select(table,selection);
	}
	public Cursor select(String table,String where){
		return select(table, null, where, null, null, null);
	}
	public Cursor selectAll(String table){
		return select(table, (String)null);
	}
	public long count(String tableName,String where) {
		String sql="select count(*) from "+ tableName +" where "+where;
		Cursor cursor = rawSelect(sql);
		cursor.moveToFirst();
		long re= cursor.getLong(0);
		cursor.close();
		return re;
	}

	/** @return  the row id of the inserted item, or -1 if an error occurred  **/public abstract long insert(String table,ContentValues contentValues);
	public void insert(final String table, final ContentValues[] contentValuesList){
		processTransaction(true, new Database.TransactionProcessor() {
			@Override public boolean onProcess(Database database) {
				for (ContentValues contentValues : contentValuesList) {
					insert(table,contentValues);
				}
				return true;
			}
			@Override public void onError(Database database, Exception e) {
				e.printStackTrace();
			}
		});
	}

	/** contentValues 里没加到的不变，只覆盖contentValues里的量**/public abstract int modify(String table,String whereExpression, ContentValues contentValues);
	public int modifyAll(String table,ContentValues contentValues) {
		return modify(table,null, contentValues);
	}
	public abstract int delete(String table, String whereExpression);
	public int deleteAll(String table){return delete(table,null);}
}
