package com.zkl.memruss.control.note_old.note;


import android.support.annotation.NonNull;

import java.util.Collection;
import java.util.List;

/**
 * 本类是上层（UI层）的note部分对底层的主要接口类，有重要的中介作用，代表了多种多样的note储存方式。<br/>
 * 由于其中介的特殊性，其方法分为几类：开放接口方法、内部接口方法、底层接口方法<br/>
 * ——开放接口方法一般没有前缀，供UI层调用的方法，可能以get、modify、delete、raw开头。get表示读取类方法；modify,delete是修改类方法；raw是“生操作”方法<br/>
 * ——内部接口方法以m开头，是对类内部及其子类开放的方法，所有开放接口方法都应该最终调用内部接口方法。
 * 内部接口方法可实现对所有操作的监控、处理类内部事务以及调用底层方法<br/>
 * ——底层接口方法以on开头，一般是抽象方法，可以对子类开放，用于托付给底层实现，一般在其中调用底层操作<br/>
 */
public abstract class Notebook {
	protected Notebook(@NonNull BookInfo bookInfo) { this.bookInfo=bookInfo; }
	protected Notebook(String bookName) { this.bookInfo = new BookInfo(bookName); }
	/**
	 * 用生操作完全复制另一个Notebook
	 */public boolean rawCopy(Notebook notebook) {
		return rawModifyBookInfo(notebook.getBookInfo()) && rawAdd(notebook.getAllNotes());
	}



	//bookInfo
	public static class BookInfo{
		@NonNull
		private final String bookName;
		@NonNull
		public String getBookName() { return bookName; }

		public BookInfo(@NonNull String bookName) {
			this.bookName = bookName;
		}
		public BookInfo() {
			this("");
		}
	}
	@NonNull
	private  BookInfo bookInfo;
	@NonNull
	public BookInfo getBookInfo() { return bookInfo; }
	public String getBookName() {
		return bookInfo.getBookName();
	}

	/**
	 * 当笔记本信息被更改<b>前</b>，交由底层做必要的同步操作
	 * @param bookInfo 新的笔记本信息
	 * @return 同步操作是否成功。若成功返回true，此时上层修改操作将继续；否则上层修改操作将终止
	 */abstract protected boolean onModifyBookInfo(BookInfo bookInfo);
	/**
	 * modifyBookInfo的中介方法，用于处理内部事务
	 */synchronized final protected boolean mModifyBookInfo(@NonNull BookInfo bookInfo) {
		if(onModifyBookInfo(bookInfo)){
			this.bookInfo = bookInfo;
			return true;
		}
		return false;
	}

	final public boolean modifyBookName(String bookName) {
		return this.mModifyBookInfo(new BookInfo(bookName));
	}

	/**
	 * 生操作修改bookInfo
	 */synchronized final public boolean rawModifyBookInfo(@NonNull BookInfo bookInfo) {
		return mModifyBookInfo(bookInfo);
	}

	//duplicate
	public interface DuplicationFixer {
		@NonNull
		NoteDuplication.DuplicationDeal onDuplicationOccur(NoteDuplication.Similarity similarity, LocalNote oldNote, LocalNote newNote);
	}
	public class DuplicateMatchResult {
		public final LocalNote duplicate;
		public final NoteDuplication.Similarity similarity;
		public DuplicateMatchResult(LocalNote note, NoteDuplication.Similarity similarity) {
			this.duplicate = note;
			this.similarity = similarity;
		}
	}
	/**
	 * 搜寻是否有重复的项
	 * @param toAdd 要进行重复查询的Note
	 * @return 返回查询的结果，若为null则表示无重复
	 */abstract protected DuplicateMatchResult searchDuplication(LocalNote toAdd);
	synchronized public boolean addWithDuplicationCheck(LocalNote newNote, DuplicationFixer duplicationFixer){
		DuplicateMatchResult result = searchDuplication(newNote);
		if (result != null) {
			//有重复！
			LocalNote oldNote=result.duplicate;
			NoteDuplication.DuplicationDeal deal = duplicationFixer.onDuplicationOccur(result.similarity, oldNote, newNote);
			if (!deal.getRemainOld()) delete(oldNote);
			addAll(deal.getToAdds());
		}else{
			//无重复，直接添加
			return add(newNote);
		}
		return false;
	}
	/**
	 * @param toAdds 要添加的词条
	 * @param duplicationFixer 遇到重复和冲突时的解决办法
	 * @return 成功条目的数量，若为-1表示添加全部成功，0表示全部失败
	 */synchronized public int addAllWithDuplicationCheck(NoteStream toAdds, DuplicationFixer duplicationFixer){
		int length=toAdds.begin();
		int success=0;
		while (toAdds.goNext()) {
			if(addWithDuplicationCheck(toAdds.get(), duplicationFixer)) success++;
		}
		if (length == success) {
			return -1;
		}else {
			return success;
		}
	}



	//search
	@NonNull
	abstract public List<LocalNote> search(String key, int limit, long offset);



	// get_name,set,modify 操作
	public static class LostItemException extends RuntimeException {
		private Object item;
		public LostItemException(Object item) {
			super("lost a note!");
			this.item = item;
		}
		public Object getItem() {
			return item;
		}
	}

	abstract public int size();
	final public boolean isEmpty(){
		return size()==0;
	}
	abstract public NoteStream getNotes(long offset, int limit);
	public LocalNote getNoteByOffset(long offset) {
		NoteStream noteStream = getNotes(offset, 1);
		noteStream.begin();
		if (noteStream.goNext()) {
			return noteStream.get();
		}else{
			return null;
		}
	}
	abstract public NoteStream getAllNotes();
	abstract public long getOffset(LocalNote note);

	/**
	 * add操作的底层接口
	 * @param toAdds 要添加的词条
	 * @return 是否全部成功，true表示全部成功
	 */protected abstract boolean onAdd(NoteStream toAdds,boolean raw);
	/**
	 * delete操作的底层接口
	 * @param note 要删除的词条，其中包括了定位需要的id
	 * @return 若删除成功则返回被删除的对象，若失败则返回null
	 */protected abstract LocalNote onDelete(LocalNote note);

	/**
	 * add操作的中介方法，用于处理内部事务
	 * @param toAdds 要添加的词条
	 * @param raw 表示是否是“生操作”，“生操作”用于导入导出操作的rawAdd，在添入note时不进行相关关联操作（不重设时间戳、不计算workLoad、不查重复）
	 * @return 返回添加是否成功，若成功则返回true
	 */synchronized protected boolean mAdd(NoteStream toAdds,boolean raw) {
		return onAdd(toAdds,raw);
	}
	/**
	 *  delete操作的中介方法，用于处理内部事务
	 * @return 若删除成功则返回被删除的对象，若失败则返回null
	 */synchronized protected LocalNote mDelete(LocalNote note) {
		return onDelete(note);
	}

	/**
	 * @param toAdds 要添加的词条
	 * @return 返回添加是否成功，若成功则返回true
	 */public boolean add(NoteStream toAdds) {
		return mAdd(toAdds,false);
	}
	/**
	 * @param toAdd 要添加的词条
	 * @return 返回添加是否成功，若成功则返回true
	 */public boolean add(LocalNote toAdd) {
		return add(new NoteStream.ListNoteStream(toAdd));
	}
	/**
	 * @param toAdds 要添加的词条
	 * @return 是否全部成功，true表示全部成功
	 */public boolean addAll(@NonNull final Collection<? extends LocalNote> toAdds){
		return add(new NoteStream.ListNoteStream(toAdds));
	}
	/**
	 * @return 若删除成功则返回被删除的对象，若失败则返回null
	 */public LocalNote delete(LocalNote note) {
		return mDelete(note);
	}

	public boolean rawAdd(NoteStream noteStream) {
		return mAdd(noteStream,true);
	}
}
