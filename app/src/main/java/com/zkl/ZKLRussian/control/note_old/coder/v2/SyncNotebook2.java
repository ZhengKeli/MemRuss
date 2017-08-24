package com.zkl.ZKLRussian.control.note_old.coder.v2;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.zkl.ZKLRussian.control.note_old.note.LocalNote;
import com.zkl.ZKLRussian.control.note_old.note.Memory;
import com.zkl.ZKLRussian.control.note_old.note.NoteDuplication;
import com.zkl.ZKLRussian.control.note_old.note.NoteStream;
import com.zkl.ZKLRussian.control.note_old.note.VersionControl;

import java.util.ArrayList;


/**
 * 表示对修改操作都进行硬盘同步的NoteBook
 */
public class SyncNotebook2 extends Memory.MemoryBook {
	final BookSyncCoder coder;
	final VersionControl.BookVersion bookVersion;
	public SyncNotebook2(BookSyncCoder coder){
		super(coder.getBookInfo(),coder.getMemoryPlan());
		this.coder = coder;
		this.bookVersion = coder.getBookVersion();
	}
	public void close(){
		coder.close();
	}


	//book info & memoryPlan
	@Override protected boolean onModifyBookInfo(BookInfo bookInfo) {
		return coder.modifyBookInfo(bookInfo);
	}
	@Override
	public boolean onModifyMemoryPlan(@Nullable Memory.MemoryPlan memoryPlan) { return coder.modifyMemoryPlan(memoryPlan); }

	//search & duplicate
	@NonNull
	@Override public ArrayList<LocalNote> search(String key, int limit, long offset) {
		NoteStream noteStream = coder.searchNotes(key,limit,offset);
		return noteStream.toArrayList();
	}



	//duplicate
	
	@Override protected DuplicateMatchResult searchDuplication(LocalNote toAdd) {
		NoteStream stream = coder.matchDuplicate(toAdd.getNoteBody().getDuplicateTags());
		stream.begin();
		while (stream.goNext()) {
			LocalNote old=stream.get();
			NoteDuplication.Similarity similarity = old.getNoteBody().compareWith(toAdd.getNoteBody());
			if(similarity!= NoteDuplication.Similarity.different) {
				return  new DuplicateMatchResult(old, similarity);
			}
		}
		return null;
	}


	//memory
		//for memory plan
	@Override public Memory.MemoryStatistics getMemoryStatistics() {
		return coder.getStatistics();
	}
	@Override protected float onRecountWorkLoad(){
		if (getMemoryPlan() != null) {
			NoteStream noteStream = coder.getAllNotes();
			float workLoadSum = 0;
			noteStream.begin();
			while (noteStream.goNext()) {
				LocalNote note = noteStream.get();
				int progress = note.getProgress();
				if (progress >= 0) {
					workLoadSum += Memory.PlanProgress.computeNoteWorkLoad(
						getMemoryPlan().args.computeNextTime(0, progress), note.getNoteBody().getWorkLoadRate());
				}
			}
			noteStream.release();
			return workLoadSum;
		}
		return 0;
	}
	@Override protected NoteStream getNotFilledNotes(int limit) {
		return coder.queryNotInPlan(limit);
	}

		//for memory _name
	@Override public void prepareResourcesForMemory(){ }
	@Override public void releaseResourcesForMemory(){ }

	@Override @Nullable
	protected LocalNote getNextMemoryNote(int maxProgress){
		LocalNote nextMemoryNote = null;
		NoteStream stream = coder.queryLearning(1,maxProgress);
		stream.begin();
		if(stream.goNext()){
			nextMemoryNote=stream.get();
		}
		return nextMemoryNote;
	}



	//get & set & modify
	@Override public int size() {
		return coder.getSize();
	}

	public LocalNote getNote(long id) {
		NoteStream noteStream = coder.getNote(id);
		noteStream.begin();
		if (noteStream.goNext()) {
			return noteStream.get();
		}
		return null;
	}
	@Override public NoteStream getNotes(long offset, int limit) {
		return coder.getNotes(offset, limit);
	}
	@Override public NoteStream getAllNotes() { return coder.getAllNotes(); }
	public long getOffset(LocalNote note) {
		return coder.getOffset(note.getId());
	}

	@Override protected boolean onAdd(final NoteStream toAdds, boolean raw) {
		if (!raw) {
			return coder.addNote(toAdds);
		} else {
			return coder.rawAddNote(toAdds);
		}

	}
	@Override protected LocalNote onDelete(LocalNote note) {
		long id = note.getId();
		if(id!=-1) {
			LocalNote note1 = getNote(id);
			if (coder.deleteNote(id)) {
				return note1;
			}
		}
		return null;
	}

	@Override protected boolean onModifyNoteMemory(LocalNote note, Memory.NoteProgress progress) {
		long id = note.getId();
		return id != -1 && coder.modifyNoteProgress(id, progress);
	}
	@Override protected boolean onClearAllNotesMemory() {
		return coder.modifyAllNoteProgress(null);
	}
}
