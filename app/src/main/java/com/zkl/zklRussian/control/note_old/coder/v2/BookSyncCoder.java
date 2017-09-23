package com.zkl.zklRussian.control.note_old.coder.v2;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.zkl.zklRussian.control.note_old.database.Database;
import com.zkl.zklRussian.control.note_old.database.Selection;
import com.zkl.zklRussian.control.note_old.database.Table;
import com.zkl.zklRussian.control.note_old.database.TableStruct;
import com.zkl.zklRussian.control.note_old.note.LocalNote;
import com.zkl.zklRussian.control.note_old.note.LocalQuestionNote;
import com.zkl.zklRussian.control.note_old.note.Memory;
import com.zkl.zklRussian.control.note_old.note.NoteBody;
import com.zkl.zklRussian.control.note_old.note.NoteStream;
import com.zkl.zklRussian.control.note_old.note.Notebook;
import com.zkl.zklRussian.control.note_old.note.VersionControl;
import com.zkl.zklRussian.control.tools.VersionException;
import com.zkl.zklRussian.control.tools.stringData.StringData;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BookSyncCoder {
	//标准
	static final class TableManifests {
		static final String name = "Manifests";
		
		static final class Column {
			static final String bookName = "bookName";
			static final String bookInfo = "bookInfo";
			static final String memoryPlanArgs = "memoryPlanArgs";
			static final String memoryPlanProgress = "memoryPlanProgress";
			static final String rawData = "raw";
		}
		
		static TableStruct getTableStruct() {
			TableStruct tableStruct = new TableStruct(name);
			tableStruct.addTextColumn(Column.bookName);
			tableStruct.addTextColumn(Column.bookInfo);
			tableStruct.addTextColumn(Column.memoryPlanArgs);
			tableStruct.addTextColumn(Column.memoryPlanProgress);
			tableStruct.addRawDataColumn(Column.rawData);
			return tableStruct;
		}
	}
	
	static final class TableBook {
		static final String name = "book";
		
		final static class Column {
			static final String noteId = "id";
			static final String modifyTime = "modifyTime";
			
			static final String noteData = "bookInfo";
			static final String noteRawData = "raw";
			
			static final String searchTags = "searchTags";
			static final String duplicateTags = "duplicateTags";
			
			static final String progress = "progress";
			static final String nextTime = "nextTime";
		}
		
		final static class Tags {
			static final String tagSeparator = "|";
			static final char tagSeparator_char = '|';
			
			static String encodeTags(String[] tags) {
				StringBuilder stringBuilder = new StringBuilder();
				for (String tag : tags) {
					stringBuilder.append(tagSeparator).append(tag);
				}
				stringBuilder.append(tagSeparator);
				return stringBuilder.toString();
			}
			
			static String[] decodeTags(String string) {
				ArrayList<String> tags = new ArrayList<>(3);
				int s1 = string.indexOf(0, tagSeparator_char);
				int s2;
				if (s1 != -1) {
					while (true) {
						s2 = string.indexOf(s1 + 1, tagSeparator_char);
						if (s2 == -1) {
							break;
						} else {
							tags.add(string.substring(s1 + 1, s2));
						}
						s1 = s2;
					}
				}
				return tags.toArray(new String[tags.size()]);
			}
		}
		
		final static class Index {
			static final String modifyTime = "index_modifyTime";
			static final String progress = "index_progress";
			static final String nextTime = "index_nextTime";
		}
		
		static TableStruct getTableStruct() {
			TableStruct tableStruct = new TableStruct(name);
			tableStruct.addIdColumn(Column.noteId);
			tableStruct.addIntegerColumn(Column.modifyTime);
			tableStruct.addTextColumn(Column.noteData);
			tableStruct.addRawDataColumn(Column.noteRawData);
			tableStruct.addTextColumn(Column.duplicateTags);
			tableStruct.addTextColumn(Column.searchTags);
			tableStruct.addIntegerColumn(Column.progress);
			tableStruct.addIntegerColumn(Column.nextTime);
			return tableStruct;
		}
	}
	
	public abstract static class NotebookManifestStruct {
		/**
		 * @return notebook的名称
		 */
		public abstract String bookName();
		
		/**
		 * @return noteBook的一些设置信息，如名字，复习计划，笔记类型等设置
		 */
		public abstract String bookData();
		
		public abstract String memoryPlanArgs();
		
		public abstract String memoryPlanProgress();
		
		public abstract byte[] rawData();
		
		public Notebook.BookInfo toBookInfo() {
			StringData stringData = StringData.decode(this.bookData());
			return DataCoder.decodeNoteBookInfo(stringData);
		}
		
		@Nullable
		public Memory.PlanArgs getPlanArgs() {
			String argsString = memoryPlanArgs();
			if (argsString.isEmpty()) return null;
			return DataCoder.decodeMemoryPlanArgs(StringData.decode(argsString));
		}
		
		@Nullable
		public Memory.PlanProgress getPlanProgress() {
			String progress = memoryPlanProgress();
			if (progress.isEmpty()) return null;
			return DataCoder.decodeMemoryPlanProgress(StringData.decode(progress));
		}
		
		public ContentValues toContentValues() {
			ContentValues re = new ContentValues();
			//id行不用写
			re.put(TableManifests.Column.bookName, bookName());
			re.put(TableManifests.Column.bookInfo, bookData());
			re.put(TableManifests.Column.memoryPlanArgs, memoryPlanArgs());
			re.put(TableManifests.Column.memoryPlanProgress, memoryPlanProgress());
			re.put(TableManifests.Column.rawData, rawData());
			return re;
		}
	}
	
	public static class NotebookManifestsStructEncoder extends NotebookManifestStruct {
		@Nullable
		Notebook.BookInfo bookInfo;
		@Nullable
		Memory.PlanArgs planArgs;
		@Nullable
		Memory.PlanProgress planProgress;
		
		public NotebookManifestsStructEncoder(@Nullable Notebook.BookInfo bookInfo, @Nullable Memory.PlanArgs planArgs,
		                                      @Nullable Memory.PlanProgress planProgress) {
			this.bookInfo = bookInfo;
			this.planArgs = planArgs;
			this.planProgress = planProgress;
		}
		
		public NotebookManifestsStructEncoder(@Nullable Memory.MemoryPlan memoryPlan) {
			this(null, memoryPlan != null ? memoryPlan.args : null, memoryPlan != null ? memoryPlan.progress : null);
		}
		
		public NotebookManifestsStructEncoder(@Nullable Notebook.BookInfo bookInfo, @NonNull Memory.MemoryPlan memoryPlan) {
			this(bookInfo, memoryPlan.args, memoryPlan.progress);
		}
		
		public NotebookManifestsStructEncoder(Notebook.BookInfo bookInfo) {
			this(bookInfo, null, null);
		}
		
		public String bookName() {
			return bookInfo != null ? bookInfo.getBookName() : "";
		}
		
		public String bookData() {
			return DataCoder.encodeNoteBookInfo(bookInfo).toString();
		}
		
		public String memoryPlanArgs() {
			StringData re = DataCoder.encodeMemoryPlanArgs(planArgs);
			return re == null ? "" : re.toString();
		}
		
		public String memoryPlanProgress() {
			StringData re = DataCoder.encodeMemoryPlanProgress(planProgress);
			return re == null ? "" : re.toString();
		}
		
		public byte[] rawData() {
			//目前rawData 没有用
			return new byte[0];
		}
		
		public ContentValues toContentValues() {
			ContentValues re = new ContentValues();
			//id行不用写
			if (bookInfo != null) {
				re.put(TableManifests.Column.bookName, bookName());
				re.put(TableManifests.Column.bookInfo, bookData());
			}
			re.put(TableManifests.Column.memoryPlanArgs, memoryPlanArgs());
			re.put(TableManifests.Column.memoryPlanProgress, memoryPlanProgress());
			//re.put(TableManifests.Column.rawData, rawData());
			return re;
		}
	}
	
	public static class NotebookManifestStructDecoder extends NotebookManifestStruct {
		Cursor cursor;
		
		public NotebookManifestStructDecoder(Cursor cursor) {
			cursor.moveToFirst();
			this.cursor = cursor;
		}
		
		public String bookName() {
			return cursor.getString(cursor.getColumnIndex(TableManifests.Column.bookName));
		}
		
		public String bookData() {
			return cursor.getString(cursor.getColumnIndex(TableManifests.Column.bookInfo));
		}
		
		public String memoryPlanArgs() {
			return cursor.getString(cursor.getColumnIndex(TableManifests.Column.memoryPlanArgs));
		}
		
		public String memoryPlanProgress() {
			return cursor.getString(cursor.getColumnIndex(TableManifests.Column.memoryPlanProgress));
		}
		
		public byte[] rawData() {
			//目前rawData 没有用
			return new byte[0];
		}
	}
	
	public abstract static class NoteStruct {
		/**
		 * @return note总数，若为-1则说明不确定总数*
		 */
		abstract int begin();
		
		/**
		 * @return 移动到下一个是否成功，若为false则表示已经走完。<b>注意，该方法应该在第一次数据读取前调用</b>
		 **/
		abstract boolean next();
		
		abstract void finish();
		
		
		abstract long id();
		
		abstract long modifyTime();
		
		abstract String noteData();
		
		abstract byte[] rawData();
		
		abstract String searchTags();
		
		abstract String duplicateTags();
		
		abstract int progress();
		
		abstract long nextTime();
		
		
		public NoteStream toNoteStream() {
			return new NoteStream() {
				DataCoder.NoteBodyCoder<StringData> coder = DataCoder.getMainNoteDataCoder();
				
				public int begin() {
					return NoteStruct.this.begin();
				}
				
				public boolean goNext() {
					return NoteStruct.this.next();
				}
				
				@NonNull
				public LocalNote get() {
					Memory.NoteProgress noteProgress = new Memory.NoteProgress(progress(), nextTime());
					NoteBody.QuestionNoteBody noteBody = (NoteBody.QuestionNoteBody) coder.decode(StringData.decode(noteData()));
					return new LocalQuestionNote(id(), modifyTime(), modifyTime(), noteBody, noteProgress);
				}
				
				public void release() {
					finish();
				}
				
			};
		}
		
		public ContentValues toContentValues(long nowTime) {
			ContentValues re = new ContentValues();
			//id 行不用写
			re.put(TableBook.Column.modifyTime, nowTime);//设置modifyTime
			
			re.put(TableBook.Column.noteData, noteData());
			re.put(TableBook.Column.noteRawData, rawData());
			
			re.put(TableBook.Column.searchTags, searchTags());
			re.put(TableBook.Column.duplicateTags, duplicateTags());
			
			re.put(TableBook.Column.progress, progress());
			re.put(TableBook.Column.nextTime, nextTime());
			
			return re;
		}
		
		public ContentValues toRawContentValues() {
			ContentValues re = new ContentValues();
			//id 行不用写
			re.put(TableBook.Column.modifyTime, modifyTime());//设置为原有时间
			
			re.put(TableBook.Column.noteData, noteData());
			re.put(TableBook.Column.noteRawData, rawData());
			
			re.put(TableBook.Column.searchTags, searchTags());
			re.put(TableBook.Column.duplicateTags, duplicateTags());
			
			re.put(TableBook.Column.progress, progress());
			re.put(TableBook.Column.nextTime, nextTime());
			
			return re;
		}
	}
	
	public static class NoteStructDecoder extends NoteStruct {
		private Cursor cursor;
		
		public NoteStructDecoder(Cursor cursor) {
			this.cursor = cursor;
		}
		
		private int[] indexes;
		
		public int begin() {
			indexes = new int[]{
				cursor.getColumnIndex(TableBook.Column.noteId),
				cursor.getColumnIndex(TableBook.Column.modifyTime),
				
				cursor.getColumnIndex(TableBook.Column.noteData),
				cursor.getColumnIndex(TableBook.Column.noteRawData),
				
				cursor.getColumnIndex(TableBook.Column.searchTags),
				cursor.getColumnIndex(TableBook.Column.duplicateTags),
				
				cursor.getColumnIndex(TableBook.Column.progress),
				cursor.getColumnIndex(TableBook.Column.nextTime)
				
			};
			cursor.moveToPosition(-1);
			return cursor.getCount();
		}
		
		public boolean next() {
			return cursor.moveToNext();
		}
		
		public void finish() {
			cursor.close();
			indexes = null;
		}
		
		public long id() {
			return cursor.getLong(indexes[0]);
		}
		
		public long modifyTime() {
			return cursor.getLong(indexes[1]);
		}
		
		public String noteData() {
			return cursor.getString(indexes[2]);
		}
		
		public byte[] rawData() {
			return cursor.getBlob(indexes[3]);
		}
		
		String searchTags() {
			return cursor.getString(indexes[4]);
		}
		
		String duplicateTags() {
			return cursor.getString(indexes[5]);
		}
		
		public int progress() {
			return cursor.getInt(indexes[6]);
		}
		
		public long nextTime() {
			return cursor.getLong(indexes[7]);
		}
	}
	
	public static class NoteStructEncoder extends NoteStruct {
		NoteStream noteStream;
		
		public NoteStructEncoder(NoteStream noteStream) {
			this.noteStream = noteStream;
		}
		
		private LocalNote note;
		
		public int begin() {
			return noteStream.begin();
		}
		
		public boolean next() {
			boolean re = noteStream.goNext();
			note = re ? noteStream.get() : null;
			return re;
		}
		
		public void finish() {
			noteStream = null;
			note = null;
		}
		
		DataCoder.NoteBodyCoder<StringData> coder = DataCoder.getMainNoteDataCoder();
		
		public long id() {
			return note.getId();
		}
		
		public long modifyTime() {
			return note.getModifyTime();
		}
		
		public String noteData() {
			return coder.encode(note.getNoteBody()).toString();
		}
		
		public byte[] rawData() {
			return new byte[0];
		}
		
		String searchTags() {
			return TableBook.Tags.encodeTags(note.getNoteBody().getSearchTags());
		}
		
		String duplicateTags() {
			return TableBook.Tags.encodeTags(note.getNoteBody().getDuplicateTags());
		}
		
		public int progress() {
			return note.getProgress();
		}
		
		public long nextTime() {
			return note.getNextTime();
		}
		
		public LocalNote getCurrentNote() {
			return note;
		}
	}
	
	public static class DataCoder {
		//books
		public static StringData encodeNoteBookInfo(Notebook.BookInfo bookInfo) {
			StringData info = new StringData();
			info.add(bookInfo.getBookName());
			return info;
		}
		
		public static Notebook.BookInfo decodeNoteBookInfo(StringData stringData) {
			return new Notebook.BookInfo(
				stringData.getString(0));
		}
		
		//memory plan
		@Nullable
		protected static StringData encodeMemoryPlanArgs(@Nullable Memory.PlanArgs planArgs) {
			if (planArgs == null) return null;
			StringData argsSD = new StringData();
			argsSD.add(planArgs.workLoadLimit);
			argsSD.add(planArgs.refillInterval);
			argsSD.add(planArgs.arg_times);
			argsSD.add(planArgs.arg_k);
			argsSD.add(planArgs.arg_a);
			return argsSD;
		}
		
		@Nullable
		protected static Memory.PlanArgs decodeMemoryPlanArgs(@Nullable StringData stringData) {
			if (stringData == null) return null;
			return new Memory.PlanArgs(
				stringData.getFloat(0),
				stringData.getLong(1),
				stringData.getFloat(2),
				stringData.getFloat(3),
				stringData.getFloat(4)
			);
		}
		
		@Nullable
		protected static StringData encodeMemoryPlanProgress(@Nullable Memory.PlanProgress planProgress) {
			if (planProgress == null) return null;
			StringData progressSD = new StringData();
			progressSD.add(planProgress.state.ordinal());
			progressSD.add(planProgress.workLoad);
			progressSD.add(planProgress.createTime);
			progressSD.add(planProgress.lastRefillTime);
			progressSD.add(planProgress.lastPausedTime);
			progressSD.add(planProgress.pausedTimeOffset);
			return progressSD;
		}
		
		@Nullable
		protected static Memory.PlanProgress decodeMemoryPlanProgress(@Nullable StringData stringData) {
			if (stringData == null) return null;
			return new Memory.PlanProgress(
				Memory.PlanState.values()[stringData.getInteger(0)],
				stringData.getFloat(1),
				stringData.getLong(2),
				stringData.getLong(3),
				stringData.getLong(4),
				stringData.getLong(5)
			);
		}
		
		
		//note content
		public interface NoteBodyCoder<D> {
			D encode(NoteBody note);
			
			NoteBody decode(D data);
		}
		
		public interface TypedNoteBodyCoder<D> extends NoteBodyCoder<D> {
			NoteBody.Type getType();
			
			/**
			 * @return 从1开始的version
			 */
			int getVersion();
		}
		
		public static MainNoteBodyCoder getMainNoteDataCoder() {
			MainNoteBodyCoder coder = new MainNoteBodyCoder();
			coder.addCoder(new MeaningNoteBodyCoder());
			return coder;
		}
		
		public static class MainNoteBodyCoder implements NoteBodyCoder<StringData> {
			public MainNoteBodyCoder() {
			}
			
			public MainNoteBodyCoder(TypedNoteBodyCoder<StringData>[] coders) {
				addCoders(coders);
			}
			
			public MainNoteBodyCoder(ArrayList<TypedNoteBodyCoder<StringData>> coders) {
				addCoders(coders);
			}
			
			ArrayList<Map<NoteBody.Type, TypedNoteBodyCoder<StringData>>> coders = new ArrayList<>();
			
			public void addCoder(TypedNoteBodyCoder<StringData> coder) {
				while (coder.getVersion() > coders.size()) {
					coders.add(null);
				}
				Map<NoteBody.Type, TypedNoteBodyCoder<StringData>> typeMap = coders.get(coder.getVersion() - 1);
				if (typeMap == null) {
					typeMap = new HashMap<>();
					coders.set(coder.getVersion() - 1, typeMap);
				}
				typeMap.put(coder.getType(), coder);
			}
			
			public void addCoders(TypedNoteBodyCoder<StringData>[] coders) {
				for (TypedNoteBodyCoder<StringData> coder : coders) {
					addCoder(coder);
				}
			}
			
			public void addCoders(ArrayList<TypedNoteBodyCoder<StringData>> coders) {
				for (TypedNoteBodyCoder<StringData> coder : coders) {
					addCoder(coder);
				}
			}
			
			public TypedNoteBodyCoder<StringData> getCoder(int version, NoteBody.Type type) {
				try {
					return coders.get(version - 1).get(type);
				} catch (Exception e) {
					return null;
				}
			}
			
			public TypedNoteBodyCoder<StringData> getCoder(NoteBody.Type type) {
				return getCoder(coders.size(), type);
			}
			
			public StringData encode(NoteBody note) {
				TypedNoteBodyCoder<StringData> coder = getCoder(note.getType());
				StringData re = new StringData();
				re.add(coder.getVersion());
				re.add(coder.getType().ordinal());
				re.add(coder.encode(note));
				return re;
			}
			
			public NoteBody decode(StringData data) {
				int version = data.getInteger(0);
				NoteBody.Type type = NoteBody.Type.values()[data.getInteger(1)];
				TypedNoteBodyCoder<StringData> coder = getCoder(version, type);
				if (coder == null) {
					throw new VersionException(coders.size() - 1, version, "Note decoder的版本不支持！");
				}
				return coder.decode(data.getStringData(2));
			}
		}
		
		//note content:typed coders
		public static class MeaningNoteBodyCoder implements TypedNoteBodyCoder<StringData> {
			public StringData encode(NoteBody note) {
				if (note instanceof NoteBody.QuestionNoteBody) {
					StringData body = new StringData();
					body.add(((NoteBody.QuestionNoteBody) note).getQuestion());
					body.add(((NoteBody.QuestionNoteBody) note).getAnswer());
					return body;
				}
				return null;
			}
			
			public NoteBody.QuestionNoteBody decode(StringData data) {
				return new NoteBody.QuestionNoteBody(data.getString(0), data.getString(1));
			}
			
			public NoteBody.Type getType() {
				return NoteBody.Type.meaning;
			}
			
			public int getVersion() {
				return 1;
			}
		}
	}
	
	
	//manifest & load ...
	@NonNull
	final Database database;
	
	private void createTables() {
		database.dropTableIfExists(TableManifests.name);
		database.dropTableIfExists(TableBook.name);
		database.createTable(TableManifests.getTableStruct());
		database.createTable(TableBook.getTableStruct());
		database.createIndex(TableBook.Index.modifyTime, TableBook.name, TableBook.Column.modifyTime);
		database.createIndex(TableBook.Index.progress, TableBook.name, TableBook.Column.progress);
		database.createIndex(TableBook.Index.nextTime, TableBook.name, TableBook.Column.nextTime);
	}
	
	@Nullable
	public Notebook.BookInfo writeInitialize() {
		final Notebook.BookInfo[] bookInfo = {null};
		database.processTransaction(true, new Database.TransactionProcessor() {
			public boolean onProcess(Database database) {
				createTables();
				Table table = database.getTable(TableManifests.name);
				assert table != null;
				
				Notebook.BookInfo info = new Notebook.BookInfo();
				table.insert(new NotebookManifestsStructEncoder(info).toContentValues());
				bookInfo[0] = info;
				return true;
			}
			
			public void onError(Database database, Exception e) {
			}
		});
		return bookInfo[0];
	}
	
	@Nullable
	public Notebook.BookInfo loadInitialize() {
		try {
			Table table = database.getTable(TableManifests.name);
			if (table == null) return null;
			Cursor cursor = table.selectAll();
			return new NotebookManifestStructDecoder(cursor).toBookInfo();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 不合法时自动写成合法的
	 */
	@Nullable
	public Notebook.BookInfo lenientInitialize() {
		Notebook.BookInfo tempInfo = loadInitialize();
		if (tempInfo == null) {
			tempInfo = writeInitialize();
		}
		return tempInfo;
	}
	
	public enum OpenMode {output, input, using}
	
	//initialize & release
	final VersionControl.BookVersion bookVersion;
	
	public VersionControl.BookVersion getBookVersion() {
		return bookVersion;
	}
	
	
	/**
	 * 不建议直接使用该方法创建coder，建议用VersionCompact创建
	 *
	 * @param database 用于硬盘同步信息的数据库
	 * @param openMode 使用的方式，分为导出、导入和使用
	 */
	protected BookSyncCoder(@NonNull Database database, VersionControl.BookVersion bookVersion, OpenMode openMode) {
		this.bookVersion = bookVersion;
		this.database = database;
		Notebook.BookInfo tempInfo = null;
		switch (openMode) {
			case output:
				tempInfo = writeInitialize();
				if (tempInfo == null) {
					throw new RuntimeException("database 写入失败");
				}
				break;
			case input:
				tempInfo = loadInitialize();
				if (tempInfo == null) {
					throw new VersionException(VersionTag2.versionCode, -1, "版本不正确，无法读取");
				}
				break;
			case using:
				tempInfo = lenientInitialize();
				if (tempInfo == null) {
					throw new RuntimeException("database 写入失败");
				}
				break;
		}
		bookInfo = tempInfo;
		
		manifestTable = database.getTable(TableManifests.name);
		bookTable = database.getTable(TableBook.name);
	}
	
	public void close() {
		database.close();
	}
	
	//book
	final Table manifestTable;
	@NotNull
	Notebook.BookInfo bookInfo;
	
	@NonNull
	public Notebook.BookInfo getBookInfo() {
		return bookInfo;
	}
	
	public boolean modifyBookInfo(final Notebook.BookInfo bookInfo) {
		boolean succeed = database.processTransaction(true, new Database.TransactionProcessor() {
			public boolean onProcess(Database dataBase) {
				return manifestTable.modifyAll(new NotebookManifestsStructEncoder(bookInfo).toContentValues()) == 1;
			}
			
			public void onError(Database dataBase, Exception e) {
			}
		});
		if (succeed) {
			this.bookInfo = bookInfo;
			return true;
		} else {
			return false;
		}
	}
	
	//todo 有空把args和progress分开
	@Nullable
	public Memory.MemoryPlan getMemoryPlan() {
		Cursor cursor = manifestTable.selectAll();
		NotebookManifestStruct struct = new NotebookManifestStructDecoder(cursor);
		if (struct.getPlanProgress() == null || struct.getPlanArgs() == null) {
			return null;
		} else return new Memory.MemoryPlan(struct.getPlanArgs(), struct.getPlanProgress());
	}
	
	public boolean modifyMemoryPlan(@Nullable final Memory.MemoryPlan memoryPlan) {
		return database.processTransaction(true, new Database.TransactionProcessor() {
			public boolean onProcess(Database database) {
				NotebookManifestsStructEncoder structEncoder = new NotebookManifestsStructEncoder(memoryPlan);
				return manifestTable.modifyAll(structEncoder.toContentValues()) == 1;
			}
			
			public void onError(Database database, Exception e) {
			}
		});
	}
	
	
	//_name
	final Table bookTable;
	
	public int getSize() {
		String expression = "select count(*) from " + TableBook.name;
		Cursor cursor = database.rawSelect(expression);
		cursor.moveToFirst();
		int re = cursor.getInt(0);
		cursor.close();
		return re;
	}
	
	public Memory.MemoryStatistics getStatistics() {
		Table book = bookTable;
		if (book != null) {
			long learning = book.count(TableBook.Column.progress + ">=" + Memory.progress_begin);
			long finished = book.count(TableBook.Column.progress + "=" + Memory.progress_finish);
			long notFilled = book.count(TableBook.Column.progress + "=" + Memory.progress_none);
			return new Memory.MemoryStatistics(learning, finished, notFilled);
		} else {
			return null;
		}
	}
	
	public NoteStream getNote(long id) {
		Cursor cursor = bookTable.select(TableBook.Column.noteId + "=" + id);
		return (new NoteStructDecoder(cursor)).toNoteStream();
	}
	
	public NoteStream getNotes(long offset, int limit) {
		Selection selection = new Selection();
		selection.setIncludedAllColumns();
		selection.setOrderBy(TableBook.Column.modifyTime, false);
		selection.setLimit(offset, limit);
		Cursor cursor = bookTable.select(selection);
		return new NoteStructDecoder(cursor).toNoteStream();
	}
	
	public NoteStream getAllNotes() {
		Selection selection = new Selection();
		selection.setIncludedAllColumns();
		selection.setOrderBy(TableBook.Column.modifyTime, true);
		Cursor cursor = bookTable.select(selection);
		return (new NoteStructDecoder(cursor)).toNoteStream();
	}
	
	public long getOffset(long noteId) {
		NoteStream noteStream = getNote(noteId);
		noteStream.begin();
		if (noteStream.goNext()) {
			LocalNote note = noteStream.get();
			long rawOffset = bookTable.count(TableBook.Column.modifyTime + ">" + note.getModifyTime());
			NoteStructDecoder suspected = new NoteStructDecoder(bookTable.select(TableBook.Column.modifyTime + "=" + note.getModifyTime()));
			suspected.begin();
			int relativeOffset = 0;
			while (suspected.next()) {
				if (suspected.id() == noteId) {
					return rawOffset + relativeOffset;
				}
				relativeOffset++;
			}
		}
		return -1;
	}
	
	public NoteStream queryLearning(int limit, int maxProgress) {
		Selection selection = new Selection();
		selection.whereExpression = TableBook.Column.progress + ">=" + (Memory.progress_begin) + " and "
			+ TableBook.Column.progress + "<" + maxProgress;
		selection.setIncludedAllColumns();
		selection.setOrderBy(TableBook.Column.nextTime, true);
		selection.setLimit(0, limit);
		Cursor cursor = bookTable.select(selection);
		return new NoteStructDecoder(cursor).toNoteStream();
	}
	
	public NoteStream queryNotInPlan(int limit) {
		Selection selection = new Selection();
		selection.whereExpression = TableBook.Column.progress + "=-1";
		selection.setIncludedAllColumns();
		selection.setOrderBy(TableBook.Column.modifyTime, false);
		selection.setLimit(0, limit);
		Cursor cursor = bookTable.select(selection);
		return new NoteStructDecoder(cursor).toNoteStream();
	}
	
	public NoteStream searchNotes(String key, int limit, long offset) {
		Selection selection = new Selection();
		selection.whereExpression = TableBook.Column.searchTags + " like " + "'%" + key + "%'";
		selection.setLimit(offset, limit);
		Cursor cursor = bookTable.select(selection);
		return new NoteStructDecoder(cursor).toNoteStream();
	}
	
	public NoteStream matchDuplicate(String[] tags) {
		ArrayList<NoteStream> streams = new ArrayList<>(tags.length);
		for (String tag : tags) {
			Cursor cursor = bookTable.select(TableBook.Column.searchTags + " like " + "'%|" + tag + "%'");
			streams.add(new NoteStructDecoder(cursor).toNoteStream());
		}
		return new NoteStream.GroupedNoteStream(streams);
	}
	
	public interface EncodeCallback {
		void finishSingle(LocalNote note);
		
		void finishAll(boolean success);
	}
	
	public boolean addNote(final NoteStream notes) {
		return database.processTransaction(true, new Database.TransactionProcessor() {
			public boolean onProcess(Database database) {
				NoteStructEncoder encoder = new NoteStructEncoder(notes);
				encoder.begin();
				while (encoder.next()) {
					bookTable.insert(encoder.toContentValues(System.currentTimeMillis()));
				}
				return true;
			}
			
			public void onError(Database database, Exception e) {
				e.printStackTrace();
			}
		});
	}
	
	public boolean addNote(final NoteStream notes, @Nullable final EncodeCallback encodeCallback) {
		if (encodeCallback == null) return addNote(notes);
		boolean success = false;
		if (bookTable != null) {
			success = database.processTransaction(true, new Database.TransactionProcessor() {
				public boolean onProcess(Database database) {
					boolean succeed = true;
					NoteStructEncoder struct = new NoteStructEncoder(notes);
					struct.begin();
					while (struct.next()) {
						long nowTime = System.currentTimeMillis();
						long id = bookTable.insert(struct.toContentValues(nowTime));
						if (id != -1) {
							LocalNote newNote = struct.getCurrentNote();
							newNote = newNote.getClone(id, nowTime, newNote.getNoteProgress());
							encodeCallback.finishSingle(newNote);
						} else {
							succeed = false;
							break;
						}
					}
					struct.finish();
					return succeed;
				}
				
				public void onError(Database database, Exception e) {
				}
			});
		}
		encodeCallback.finishAll(success);
		return success;
	}
	
	public boolean rawAddNote(final NoteStream noteStream) {
		return database.processTransaction(true, new Database.TransactionProcessor() {
			public boolean onProcess(Database database) {
				NoteStructEncoder encoder = new NoteStructEncoder(noteStream);
				encoder.begin();
				while (encoder.next()) {
					bookTable.insert(encoder.toRawContentValues());
				}
				return true;
			}
			
			public void onError(Database database, Exception e) {
				e.printStackTrace();
			}
		});
	}
	
	public boolean deleteNote(final long id) {
		return database.processTransaction(true, new Database.TransactionProcessor() {
			public boolean onProcess(Database database) {
				return bookTable.delete(TableBook.Column.noteId + "=" + id) == 1;
			}
			
			public void onError(Database database, Exception e) {
				e.printStackTrace();
			}
		});
	}
	
	public boolean clearNote() {
		return database.processTransaction(true, new Database.TransactionProcessor() {
			public boolean onProcess(Database database) {
				bookTable.deleteAll();
				return true;
			}
			
			public void onError(Database database, Exception e) {
				e.printStackTrace();
			}
		});
	}
	
	public boolean modifyNote(final long id, final LocalNote note) {
		return database.processTransaction(true, new Database.TransactionProcessor() {
			public boolean onProcess(Database database) {
				NoteStructEncoder struct = new NoteStructEncoder(new NoteStream.ListNoteStream(note));
				struct.begin();
				struct.next();
				return bookTable.modify(TableBook.Column.noteId + "=" + id, struct.toContentValues(System.currentTimeMillis())) == 1;
			}
			
			public void onError(Database database, Exception e) {
				e.printStackTrace();
			}
		});
	}
	
	public boolean modifyNoteProgress(final long id, @Nullable final Memory.NoteProgress noteProgress) {
		return database.processTransaction(true, new Database.TransactionProcessor() {
			public boolean onProcess(Database database) {
				ContentValues contentValues = new ContentValues();
				Memory.NoteProgress progress = noteProgress != null ? noteProgress : Memory.getNullProgress();
				contentValues.put(TableBook.Column.nextTime, progress.nextTime);
				contentValues.put(TableBook.Column.progress, progress.progress);
				return bookTable.modify(TableBook.Column.noteId + "=" + id, contentValues) == 1;
			}
			
			public void onError(Database database, Exception e) {
				e.printStackTrace();
			}
		});
	}
	
	public boolean modifyAllNoteProgress(@Nullable final Memory.NoteProgress noteProgress) {
		final Memory.NoteProgress progress = noteProgress != null ? noteProgress : Memory.getNullProgress();
		return database.processTransaction(true, new Database.TransactionProcessor() {
			public boolean onProcess(Database database) {
				ContentValues contentValues = new ContentValues();
				contentValues.put(TableBook.Column.nextTime, progress.nextTime);
				contentValues.put(TableBook.Column.progress, progress.progress);
				bookTable.modify(null, contentValues);
				return true;
			}
			
			public void onError(Database database, Exception e) {
				e.printStackTrace();
			}
		});
	}
}

