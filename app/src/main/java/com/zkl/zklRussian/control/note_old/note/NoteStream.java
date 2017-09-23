package com.zkl.zklRussian.control.note_old.note;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public abstract class NoteStream {
	/**
	 * @return 返回总长度，-1可表示不确定总长度
	 */
	abstract public int begin();

	/**
	 * 读取下一个元素，读取成功后可以用{@link #get()} 获取。
	 *
	 * @return 读取是否成功，若返回false则{@link #get()} 获取的将是null
	 */
	abstract public boolean goNext();

	/**
	 * 该方法应在调用（至少一次）{@link #goNext()}之后使用
	 *
	 * @return 刚刚读取到的元素，若读取失败了则返回null
	 */
	abstract public LocalNote get();

	/**
	 * 释放资源以节省空间（即使未调用此方法，当被GC回收时也会调用）
	 */
	abstract public void release();

	@Override
	protected void finalize() throws Throwable {
		release();
		super.finalize();
	}

	public ArrayList<LocalNote> toArrayList() {
		int size = begin();
		ArrayList<LocalNote> re = new ArrayList<>(size == -1 ? 0 : size);
		while (goNext()) {
			re.add(get());
		}
		return re;
	}



	public static class ListNoteStream extends NoteStream {
		Iterable<? extends LocalNote> iterable;
		Iterator<? extends LocalNote> iterator;

		public ListNoteStream(@NonNull Iterable<? extends LocalNote> iterable) {
			this.iterable = iterable;
		}
		public ListNoteStream(LocalNote note) {
			ArrayList<LocalNote> notes = new ArrayList<>(1);
			notes.add(note);
			iterable = notes;
		}

		@Override public int begin() {
			iterator = iterable.iterator();
			if (iterable instanceof Collection) {
				return ((Collection) iterable).size();
			} else {
				return -1;
			}
		}
		@Override public boolean goNext() {
			return iterator.hasNext();
		}
		@NonNull
		@Override public LocalNote get() {
			return iterator.next();
		}
		@Override public void release() {
			iterable=null;
			iterator = null;
		}

		@Override public ArrayList<LocalNote> toArrayList() {
			if (iterable instanceof ArrayList) {
				return (ArrayList<LocalNote>) iterable;
			}
			return super.toArrayList();
		}
	}
	public static class GroupedNoteStream extends NoteStream {
		Iterable<NoteStream> noteStreams;
		Iterator<NoteStream> noteStreamIterator;
		@Nullable
		NoteStream noteStream;

		public GroupedNoteStream(Iterable<NoteStream> noteStreams) {
			this.noteStreams = noteStreams;
		}
		void goNextStream() {
			if (noteStreamIterator.hasNext()) {
				noteStream = noteStreamIterator.next();
				noteStream.begin();
			} else {
				noteStream = null;
			}
		}
		@Override public int begin() {
			noteStreamIterator = noteStreams.iterator();
			goNextStream();
			return -1;
		}
		@Override public boolean goNext() {
			if (noteStream != null) {
				if (!noteStream.goNext()) {
					goNextStream();
					return goNext();
				}
				return true;
			}
			return false;
		}
		@NonNull
		@Override public LocalNote get() {
			if (noteStream != null) {
				return noteStream.get();
			}
			return null;
		}
		@Override public void release() {
			noteStreamIterator=null;
			noteStream=null;
			for (NoteStream stream : noteStreams) {
				stream.release();
			}
		}
	}

	public interface NoteFilter{
		LocalNote filter(LocalNote note);
	}
	public static class FilterNoteStream extends NoteStream {
		@NonNull
		private NoteStream source;
		@NonNull
		private NoteFilter noteFilter;
		public FilterNoteStream(@NonNull NoteStream source, @NonNull NoteFilter noteFilter) {
			this.source = source;
			this.noteFilter = noteFilter;
		}
		@Override public int begin() {
			return source.begin();
		}
		@Override public boolean goNext() {
			return source.goNext();
		}
		@Override public LocalNote get() {
			return noteFilter.filter(source.get());
		}
		@Override public void release() {
			source.release();
		}
	}
}
