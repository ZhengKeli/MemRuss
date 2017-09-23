package com.zkl.zklRussian.control.note_old.note;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 表示对修改操作不进行硬盘同步的NoteBook
 * 会用一个ArrayList来装载所以Note
 */
//todo 有机会把这个类弃用，改用arrayList或RamSyncNotebook来做临时工作
public class TempNotebook extends Notebook {
	public TempNotebook(String bookName) {
		super(bookName);
	}
	public TempNotebook() { this(""); }


	//info
	@Override protected boolean onModifyBookInfo(BookInfo bookInfo) { return true ; }

	//duplicate
	@Override protected DuplicateMatchResult searchDuplication(LocalNote toAdd) {
		for(int i=0;i<size();i++) {
			LocalNote old = getNoteByOffset(i);
			NoteDuplication.Similarity similarity = old.getNoteBody().compareWith(toAdd.getNoteBody());
			if (similarity != NoteDuplication.Similarity.different) {
				return new DuplicateMatchResult(old, similarity);
			}
		}
		return null;
	}

	//search
	@NonNull
	@Override public List<LocalNote> search(String key, int limit, long offset) {
		ArrayList<LocalNote> re = new ArrayList<>();
		for (LocalNote note : notes) {
			if (note.getNoteBody().matchSearchTags(key)) {
				re.add(note);
			}
		}
		long end=offset+limit;
		if (offset < re.size()){
			if (end > re.size()) {
				end=re.size();
			}
			return re.subList((int) offset, (int)end);
		}
		return new ArrayList<>(0);
	}

	// get_name & set & modify
	ArrayList<LocalNote> notes = new ArrayList<>();
	@Override public int size() { return notes.size(); }

	@Override public NoteStream getNotes(long offset, int limit) {
		return new NoteStream.ListNoteStream(notes.subList((int)offset, (int) (offset + limit)));
	}
	@Override public NoteStream getAllNotes() {
		return new NoteStream.ListNoteStream(notes);
	}
	@Override public long getOffset(LocalNote note) {
		return notes.indexOf(note);
	}

	synchronized public boolean onAdd(@NonNull final LocalNote toAdd) {
		return notes.add(toAdd);
	}
	@Override synchronized public boolean onAdd(NoteStream toAdds, boolean raw) {
		int expand= toAdds.begin();
		expand=expand==-1?0:expand;
		notes.ensureCapacity(notes.size() + expand);
		while (toAdds.goNext()) {
			notes.add(toAdds.get());
		}
		return true;
	}
	@Override public LocalNote onDelete(LocalNote note) {
		if(notes.remove(note)) return note; return null;
	}
	synchronized public LocalNote delete(int index){
		return notes.remove(index);
	}
}
