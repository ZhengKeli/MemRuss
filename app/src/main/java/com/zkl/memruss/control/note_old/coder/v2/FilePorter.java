package com.zkl.memruss.control.note_old.coder.v2;

import android.os.Environment;

import com.zkl.memruss.control.note_old.VersionException;
import com.zkl.memruss.control.note_old.note.Notebook;
import com.zkl.memruss.control.note_old.note.TempNotebook;
import com.zkl.memruss.control.note_old.note.VersionControl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FilePorter {
	public static final String exportFileDefaultDirectory = Environment.getExternalStorageDirectory() + "/ZKLRussian/";


	static class ParseException extends Exception { }
	static class FileParseException extends ParseException { }
	public static class FileTooBigException extends FileParseException { }


	public static abstract class Porter {
		public abstract Notebook[] importFromFile(File file) throws IOException,ParseException;
		public abstract boolean canImport(File file);
		public abstract boolean isImportRecommended(File file);

		public abstract boolean exportToFile(File file,Notebook notebook) throws FileNotFoundException;
	}
	public static abstract class StreamPorter extends Porter {
		@Override public Notebook[] importFromFile(File file) throws IOException, ParseException {
			FileInputStream inputStream=new FileInputStream(file);
			Notebook[] re=importFromStream(inputStream, file.length());
			inputStream.close();
			return re;
		}
		@Override public boolean exportToFile(File file,Notebook notebook) throws FileNotFoundException {
			FileOutputStream outputStream=new FileOutputStream(file);
			boolean re= exportToStream(outputStream, notebook);
			try {
				outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return re;
		}

		abstract public Notebook[] importFromStream(InputStream inputStream, long length) throws IOException, FileParseException;
		abstract public boolean exportToStream(OutputStream outputStream,Notebook notebook);
	}

	public static class BookPorter extends Porter {
		static final String fileExtension = ".zrb";
		@Override public Notebook[] importFromFile(File file) throws IOException, ParseException {
			try {
				Notebook re= VersionControl.getMainBookLoader().loadBookWithPreferredVersion(
					new VersionControl.FileBookSource(file), VersionControl.LoadMode.read, null
				);

				if (re != null) {
					return new Notebook[]{re};
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new ParseException();
			}
			return new Notebook[0];
		}

		@Override public boolean canImport(File file) { return true; }
		@Override public boolean isImportRecommended(File file) {
			return canImport(file) && file.getName().endsWith(fileExtension) || file.getName().endsWith(".db");
		}
		@Override public boolean exportToFile(File file, Notebook notebook) throws FileNotFoundException {
			try {
				SyncNotebook2 outputBook = VersionControl.getMainBookLoader().loadBookWithPreferredVersion(
					new VersionControl.FileBookSource(file), VersionControl.LoadMode.write_override, null);
				if (outputBook != null) {
					outputBook.rawCopy(notebook);
					outputBook.close();

					try {
						(new File(file.getPath() + "-journal")).delete();
					} catch (Exception ignored) {}
					return true;
				}
				return false;
			} catch (VersionException e) {
				e.printStackTrace(); }
			return false;
		}
	}
	public static class StringPorter extends StreamPorter {
		static final String fileExtension = ".txt";

		@Override public Notebook[] importFromStream(InputStream inputStream, long length) throws IOException, FileParseException {
			byte[] bytes = new byte[(int) length];
			inputStream.read(bytes);

			try {
				TempNotebook noteBook = new TempNotebook();
				noteBook.add(com.zkl.memruss.control.note_old.coder.v2.TextCoder.decodeStringMeaningNote(new String(bytes)));
				return new Notebook[]{noteBook};
			} catch (Exception e) {
				throw new FileParseException();
			}
		}

		@Override public boolean canImport(File file) {
			return file.length() < Integer.MAX_VALUE && file.getName().endsWith(fileExtension);
		}
		@Override public boolean isImportRecommended(File file) {
			return canImport(file);
		}

		@Override public boolean exportToStream(OutputStream outputStream, Notebook notebook) {
			try {
				outputStream.write(TextCoder.encodeStringNoteBody(notebook.getAllNotes(),true).getBytes());
				return true;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
	}


	/**
	 * @return 返回值不在bookSet下！
	 */public static Notebook[] importFromFile(File file){
		Porter[] porters = new Porter[]{new BookPorter(),new StringPorter()};

		Notebook[] re = null;
		for (Porter porter : porters) {
			if (porter.isImportRecommended(file)) {
				try {
					re = porter.importFromFile(file);
				} catch (Exception ignored) {}
				break;
			}
		}
		if (re == null) {
			for (Porter porter : porters) {
				if (porter.canImport(file)) {
					try {
						re = porter.importFromFile(file);
					} catch (Exception e) {
						continue;
					}
					if (re == null || re.length == 0) {
						continue;
					}
					break;
				}
			}
		}
		return re;
	}

}
