package com.zkl.ZKLRussian.control.tools

import android.database.sqlite.SQLiteDatabase
import kotlin.reflect.jvm.internal.impl.utils.StringsKt


fun SQLiteDatabase.createIndex(indexName: String,tableName:String, ifNotExists: Boolean = false,vararg columns:String) {
	var sql = "CREATE INDEX "
	if (ifNotExists) sql += "IF NOT EXISTS "
	sql += "$indexName ON $tableName"
	sql += "("
	sql += StringsKt.join(columns.asIterable(), ",")
	sql += ")"
	execSQL(sql)
}
