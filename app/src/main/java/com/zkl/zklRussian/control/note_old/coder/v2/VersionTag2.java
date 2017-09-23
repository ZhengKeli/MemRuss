package com.zkl.zklRussian.control.note_old.coder.v2;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.zkl.zklRussian.control.note_old.database.Database;
import com.zkl.zklRussian.control.note_old.database.SQLite;
import com.zkl.zklRussian.control.note_old.database.Table;
import com.zkl.zklRussian.control.note_old.database.TableStruct;
import com.zkl.zklRussian.control.note_old.note.VersionControl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class VersionTag2 {
	static final public int versionCode =2;
	final static public VersionControl.BookVersion bookVersion =
		new VersionControl.BookVersion(versionCode, VersionControl.BookType.localSynced);

	public static final class TableBookVersion{
		static final String name = "BookVersion";

		static final class Column {
			static final String versionCode = "versionCode";
			static final String typeCode = "typeCode";
		}
		static TableStruct getTableStruct() {
			TableStruct tableStruct = new TableStruct(name);
			tableStruct.addIntegerColumn(Column.versionCode);
			tableStruct.addIntegerColumn(Column.typeCode);
			return tableStruct;
		}

		static ContentValues encode(VersionControl.BookVersion bookVersion) {
			ContentValues contentValues = new ContentValues();
			contentValues.put(Column.versionCode, bookVersion.versionCode);
			contentValues.put(Column.typeCode, bookVersion.bookType.ordinal());
			return contentValues;
		}
		static VersionControl.BookVersion decode(Cursor cursor) {
			cursor.moveToFirst();
			return new VersionControl.BookVersion(
				cursor.getInt(cursor.getColumnIndex(Column.versionCode)),
				VersionControl.BookType.values()[cursor.getInt(cursor.getColumnIndex(Column.typeCode))]
			);
		}
	}
	public static class BookLoader2 extends VersionControl.BookLoader{
		final static Map<VersionControl.LoadMode,Database.OpenMode> databaseModeMap;
		static {
			databaseModeMap = new HashMap<>();
			databaseModeMap.put(VersionControl.LoadMode.read, Database.OpenMode.read);
			databaseModeMap.put(VersionControl.LoadMode.write_passively, Database.OpenMode.write_passively);
			databaseModeMap.put(VersionControl.LoadMode.write_lenient, Database.OpenMode.write_lenient);
			databaseModeMap.put(VersionControl.LoadMode.write_override, Database.OpenMode.write_override);
		}
		final static Map<VersionControl.LoadMode,BookSyncCoder.OpenMode> coderModeMap;
		static {
			coderModeMap = new HashMap<>();
			coderModeMap.put(VersionControl.LoadMode.read, BookSyncCoder.OpenMode.input);
			coderModeMap.put(VersionControl.LoadMode.write_passively, BookSyncCoder.OpenMode.using);
			coderModeMap.put(VersionControl.LoadMode.write_lenient, BookSyncCoder.OpenMode.using);
			coderModeMap.put(VersionControl.LoadMode.write_override, BookSyncCoder.OpenMode.output);
		}

		@Nullable
		@Override
		protected SyncNotebook2 loadBook(
			@NonNull VersionControl.BookSource source, @Nullable VersionControl.BookVersion aimedVersion,
			VersionControl.LoadMode loadMode, @Nullable final VersionControl.LoadCallback loadCallback) {
			if (aimedVersion==null || aimedVersion.equals(bookVersion)) {
				if (source instanceof VersionControl.FileBookSource) {
					final boolean[] created = {false};
					Database.OpenCallback openCallback = null;
					if (loadCallback != null) {
						openCallback = new Database.OpenCallback() {
							@Override
							public boolean onNeedWipe(File file) {
								return loadCallback.onNeedWipe(file);
							}

							@Override
							public void onWiped() {
								loadCallback.onWiped();
							}

							@Override
							public boolean onNeedCreation(File file) {
								return loadCallback.onNeedCreation(file);
							}

							@Override
							public void onCreated(Database database) {
								created[0] = true;
							}

							@Override
							public void onError(Exception exception) {
								loadCallback.onError(exception);
							}
						};
					}
					Database database = SQLite.openFromFile(
						((VersionControl.FileBookSource) source).file,
						databaseModeMap.get(loadMode), openCallback);

					if (database != null) {
						if (created[0] || loadMode == VersionControl.LoadMode.write_lenient ||
							loadMode == VersionControl.LoadMode.write_override) {
							if(!setVersion(database)){
								database.close();
								SQLiteDatabase.deleteDatabase(new File(database.getPath()));
								return null;
							}
						}
						VersionControl.BookVersion bookVersion = checkVersion(database);
						if (bookVersion!=null && bookVersion.equals(aimedVersion)) {
							SyncNotebook2 re = new SyncNotebook2(new BookSyncCoder(database,bookVersion,coderModeMap.get(loadMode)));
							if (created[0] && loadCallback != null) {
								loadCallback.onCreated(re);
							}
							return re;
						}
					}
				}
			}
			return null;
		}
		@Nullable
		public static VersionControl.BookVersion checkVersion(Database database) {
			Table table = database.getTable(TableBookVersion.name);
			if (table != null) {
				try {
					return TableBookVersion.decode(table.selectAll());
				} catch (Exception ignore) {}
			}
			return null;
		}
		public static boolean setVersion(Database database) {
			database.createTable(TableBookVersion.getTableStruct());
			Table table = database.getTable(TableBookVersion.name);
			return table != null && table.insert(TableBookVersion.encode(bookVersion)) == 1;
		}
	}
}
