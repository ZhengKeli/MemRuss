package com.zkl.zklRussian.control.note_old.note;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.zkl.zklRussian.control.note_old.NotebookSet;
import com.zkl.zklRussian.control.note_old.coder.v2.SyncNotebook2;
import com.zkl.zklRussian.control.note_old.coder.v2.VersionTag2;
import com.zkl.zklRussian.control.note_old.database.Database;
import com.zkl.zklRussian.control.note_old.database.SQLite;

import java.io.File;
import java.util.ArrayList;

public class VersionControl {
	public final static int latestVersionCode =2;

	//book version
	public enum BookType {temp,localSynced,webSynced}
	public static class BookVersion {
		public final int versionCode;
		public final BookType bookType;
		public BookVersion(int versionCode, BookType bookType) {
			this.versionCode = versionCode;
			this.bookType = bookType;
		}

		@Override public boolean equals(Object o) {
			if (o instanceof BookVersion) {
				return versionCode==((BookVersion) o).versionCode && bookType ==((BookVersion) o).bookType;
			}
			return false;
		}
	}
	public static BookVersion preferredVersion = new BookVersion(latestVersionCode, BookType.localSynced);

	//book load operation
	public enum LoadMode {
		/**
		 * 仅读取，不会对任何数据作修改。也不会创建数据库。
		 */read,
		/**
		 * 被动写入模式，如果文件符合条件则打开，否则返回null
		 */write_passively,
		/**
		 * 兼容写入模式。若原来有可用文件则会直接使用若；
		 * 如果原有文件无法使用则试图删除后再重建数据库；
		 * 若没有文件则会创建
		 */write_lenient,
		/**
		 * 强制写入模式，会直接创建新的文件再使用（若原来有文件则会被删除）
		 */write_override
	}
	public interface LoadCallback {
		/**
		 * 表示有文件冲突，需要删除原来的文件。
		 * @param file 文件的路径
		 * @return 若返回true则表示同意删除；返回false表示不删除，但可能导致打开数据库失败
		 */boolean onNeedWipe(File file);
		/**
		 * 当删除了冲突的文件成功后调用
		 */void onWiped();
		/**
		 * 在需要创建新的数据库文件时调用。
		 * @param file 新的数据库文件将要被创建的目标路径。
		 * @return 返回true表示同意创建；返回false表示不同意创建，但可能导致数据库打开失败
		 */boolean onNeedCreation(File file);
		/**
		 * 当本子被创建成功时调用
		 * @param syncNotebook 新创建的本子
		 */void onCreated(SyncNotebook2 syncNotebook);
		/**
		 * 当需要进行版本转换的时候，调用此方法
		 * @return 返回true表示同意操作，返回false表示不同意
		 */boolean onNeedConvert(BookVersion oldVersion, BookVersion newVersion);
		/**
		 * 版本转换成功后调用
		 */void onConverted(SyncNotebook2 syncNotebook);
		/**
		 * 当打开过程出现错误时会被调用。
		 * @param exception 出现的错误
		 */void onError(Exception exception);
	}

	//book source
	public static abstract class BookSource { }
	public static class FileBookSource extends BookSource{
		public final File file;
		public FileBookSource(File file) { this.file = file; }
	}
	public static class RAMBookSource extends BookSource{}

	//book loader
	public static abstract class BookLoader {
		/**
		 * 加载一个notebook
		 * @param source 用于加载notebook的数据源，可能是文件、网址、uri等等
		 * @param aimedVersion 加载的目标版本
		 * @param loadMode 加载的模式
		 * @param loadCallback 用于处理加载过程中遇到的一些问题
		 * @return 加载出来的SyncNotebook，若加载失败则返回null
		 */@Nullable
		protected abstract SyncNotebook2 loadBook(@NonNull BookSource source, @Nullable BookVersion aimedVersion,
		                                          LoadMode loadMode, @Nullable LoadCallback loadCallback);
	}
	public static class MainBookLoader extends BookLoader {
		@Nullable
		public SyncNotebook2 loadBookWithPreferredVersion(BookSource source, LoadMode loadMode, LoadCallback loadCallback) {
			return loadBook(source, preferredVersion, loadMode, loadCallback);
		}
		@Nullable
		@Override
		public SyncNotebook2 loadBook(@NonNull BookSource source, @Nullable BookVersion aimedVersion,
		                              LoadMode loadMode, @Nullable LoadCallback loadCallback) {
			SyncNotebook2 re=null;
			for (BookLoader bookLoader : bookLoaders) {
				try {
					re = bookLoader.loadBook(source, aimedVersion, loadMode, loadCallback);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (re != null) return re;
			}
			return null;
		}
		ArrayList<BookLoader> bookLoaders = new ArrayList<>();
		protected void addBookLoader(@NonNull BookLoader bookLoader) {
			bookLoaders.add(bookLoader);
		}
	}
	private static MainBookLoader mainBookLoader;
	@NonNull
	public static MainBookLoader getMainBookLoader() {
		if (mainBookLoader == null) {
			mainBookLoader = new MainBookLoader();
			//最新的放最前面！！
			mainBookLoader.addBookLoader(new VersionTag2.BookLoader2());
		}
		return mainBookLoader;
	}




	//bookSet
	/**
	 * 若文件不合法则会重建以使之合法
	 */
	@Nullable
	public static NotebookSet loadBookSet(File file) {
		Database database = SQLite.openOrCreateFromFile(file);
		if (database != null) {
			database.setVersion(latestVersionCode);
			return new NotebookSet(database);
		}
		return null;
	}
}
