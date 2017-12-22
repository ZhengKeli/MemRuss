package com.zkl.memruss.control.note_old.database;


import android.content.ContentValues;
import android.database.Cursor;

public class Table {
	public static String column_rowId="rowid";

	Database database;
	public Database getDatabase() { return database; }
	String tableName;
	public String getTableName() { return tableName; }
	protected Table(Database database, String tableName) {
		this.database = database;
		this.tableName = tableName;
	}

	public Cursor select(String[] includedColumns,String where,String groupBy,String having,String orderBy){
		return database.select(tableName, includedColumns,where,groupBy,having,orderBy);
	}
	public Cursor select(String where){ return database.select(tableName,where); }
	public Cursor select(Selection selection) {
		return database.select(tableName,selection);
	}
	public Cursor selectAll(){
		return database.selectAll(tableName);
	}

	public long count(String where) {
		return database.count(tableName, where);
	}


	/** @return  the row id of the inserted item, or -1 if an error occurred  **/public long insert(ContentValues contentValues) { return database.insert(tableName,contentValues); }
	public void insert(final ContentValues[] contentValuesList){
		database.insert(tableName, contentValuesList);
	}

	/** contentValues 里没加到的不变，只覆盖contentValues里的量**/public int modify(String whereExpression, ContentValues contentValues){
		return database.modify(tableName,whereExpression,contentValues);
	}
	public int modifyAll(ContentValues contentValues) {
		return database.modifyAll(tableName,contentValues);
	}
	public int delete(String whereExpression){return database.delete(tableName, whereExpression);}
	public int deleteAll(){return database.deleteAll(tableName);}
}
