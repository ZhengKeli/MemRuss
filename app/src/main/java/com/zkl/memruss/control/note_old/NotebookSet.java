package com.zkl.memruss.control.note_old;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.zkl.memruss.control.note_old.coder.v2.SyncNotebook2;
import com.zkl.memruss.control.note_old.database.Database;
import com.zkl.memruss.control.note_old.database.Table;
import com.zkl.memruss.control.note_old.database.TableStruct;
import com.zkl.memruss.control.note_old.note.Notebook;
import com.zkl.memruss.control.note_old.note.VersionControl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NotebookSet {
	public static class TableBookSet {
		final static String name = "bookSet";

		public static class Column{
			final static String bookId = "bookId";
			final static String pathType = "pathType";
			final static String path = "path";

		}
		public static TableStruct getTableStruct(){
			TableStruct struct = new TableStruct(name);
			struct.addIdColumn(Column.bookId);
			struct.addIntegerColumn(Column.pathType);
			struct.addTextColumn(Column.path);
			return struct;
		}

		final static PathType pathType_init= PathType.relative;
		final static String path_init="/init";
		public static ContentValues getInitializeContentValues(){
			ContentValues contentValues = new ContentValues();
			//id不写
			contentValues.put(Column.pathType, pathType_init.ordinal());
			contentValues.put(Column.path, path_init);
			return contentValues;
		}
	}

	@NonNull
	final File dir;
	@NonNull
	final Database database;

	public NotebookSet(@NonNull Database database) {
		this.database = database;
		File temDir=new File(database.getPath()).getParentFile();
		if (temDir == null) {
		  throw new NullPointerException();
		}else{
			dir=temDir;
		}

		Table temTable = database.getTable(TableBookSet.name);
		if (temTable == null) {
			database.createTable(TableBookSet.getTableStruct());
			temTable = database.getTable(TableBookSet.name);
			if(temTable==null) throw new NullPointerException();
		}
		bookSetTable = temTable;
		initializeLoadBooks();
	}
	@Override protected void finalize() throws Throwable { super.finalize();
		database.close(); }



	@NonNull
	final Table bookSetTable;
	Map<Long,SyncNotebook2> books;
	public void initializeLoadBooks(){
		Cursor cursor = bookSetTable.selectAll();
		books = new HashMap<>(cursor.getCount());
		books = new HashMap<>(books.size());
		cursor.moveToFirst();

		int ci_id = cursor.getColumnIndex(TableBookSet.Column.bookId);
		int ci_pathType = cursor.getColumnIndex(TableBookSet.Column.pathType);
		int ci_path = cursor.getColumnIndex(TableBookSet.Column.path);
		while (!cursor.isAfterLast()) {
			long id = cursor.getLong(ci_id);
			PathType pathType = PathType.values()[cursor.getInt(ci_pathType)];
			String path = cursor.getString(ci_path);

			SyncNotebook2 syncNotebook = loadBookFile(pathType, path);
			if (syncNotebook != null) {
				books.put(id, syncNotebook);
			}else{
				//其实这里应该做一个不兼容警告
				//deleteBook(id, false);
			}
			cursor.moveToNext();
		}
	}

	public Map<Long,SyncNotebook2> getAllBooks() { return (new HashMap<>(books)); }
	public List<Map.Entry<Long,SyncNotebook2>> getAllBooksOfArray(){
		Set<Map.Entry<Long, SyncNotebook2>> entrySet = books.entrySet();
		List<Map.Entry<Long, SyncNotebook2>> re = new ArrayList<>(entrySet.size());
		re.addAll(entrySet);
		return re;
	}
	public boolean existBook(long bookId) {
		return books.containsKey(bookId);
	}

	public SyncNotebook2 getBook(long id) {
		if (books.containsKey(id)) {
			return books.get(id);
		}else {
			return null;
		}
	}
	public long createBook() {
		final long id = bookSetTable.insert(TableBookSet.getInitializeContentValues());
		SyncNotebook2 notebook = createBookFile(PathType.relative, makeBookPathById(id));
		if (notebook != null) {
			ContentValues contentValues = new ContentValues();
			contentValues.put(TableBookSet.Column.pathType, PathType.relative.ordinal());
			contentValues.put(TableBookSet.Column.path, makeBookPathById(id));
			if(bookSetTable.modify(TableBookSet.Column.bookId + "=" + id, contentValues)==1){
				books.put(id, notebook);
				return id;
			}
		}
		bookSetTable.delete(TableBookSet.Column.bookId + "=" + id);
		return -1;
	}
	public long addBookCopy(Notebook notebook) {
		long id = createBook();
		SyncNotebook2 newBook = getBook(id);
		newBook.rawCopy(notebook);
		return id;
	}
	public long addExistedBook(PathType pathType, String path){
		SyncNotebook2 notebook = loadBookFile(pathType, path);
		if (notebook != null) {
			ContentValues contentValues = new ContentValues();
			contentValues.put(TableBookSet.Column.pathType, pathType.ordinal());
			contentValues.put(TableBookSet.Column.path, path);
			return bookSetTable.insert(contentValues);
		}
		return -1;
	}
	public boolean deleteBook(long id, boolean deleteSource) {
		if (deleteSource) {
			Cursor cursor = bookSetTable.select(TableBookSet.Column.bookId + "=" + id);
			if(cursor.getCount()==0) return false;
			cursor.moveToFirst();
			deleteBookFile(
				PathType.values()[cursor.getInt(cursor.getColumnIndex(TableBookSet.Column.pathType))],
				cursor.getString(cursor.getColumnIndex(TableBookSet.Column.path))
			);
		}
		boolean deleted= bookSetTable.delete(TableBookSet.Column.bookId + "=" + id)==1;
		if(deleted) books.remove(id);
		return deleted;
	}



	public enum PathType{relative}
	@Nullable
	private SyncNotebook2 createBookFile(PathType pathType, String path) {
		File file = getFile(pathType, path);
		return VersionControl.getMainBookLoader().loadBookWithPreferredVersion(
			new VersionControl.FileBookSource(file), VersionControl.LoadMode.write_override,null);
	}
	@Nullable
	private SyncNotebook2 loadBookFile(PathType pathType, String path) {
		File file = getFile(pathType, path);
		return VersionControl.getMainBookLoader().loadBookWithPreferredVersion(
			new VersionControl.FileBookSource(file), VersionControl.LoadMode.write_passively,null
		);
	}
	private boolean deleteBookFile(PathType pathType, String path) {
		File file = getFile(pathType, path);
		return SQLiteDatabase.deleteDatabase(file);
	}

	private String makeBookPathById(long id) {
		return "/book" + id;
	}
	@Nullable
	private File getFile(PathType pathType, String path) {
		File file=null;
		switch (pathType) {
			case relative:
				file = new File(dir, path);
				break;
		}
		return file;
	}
}
