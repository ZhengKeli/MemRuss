package com.zkl.ZKLRussian.control.note_old.coder.v2;

import android.support.annotation.Nullable;

import com.zkl.ZKLRussian.control.note_old.note.LocalNote;
import com.zkl.ZKLRussian.control.note_old.note.LocalQuestionNote;
import com.zkl.ZKLRussian.control.note_old.note.NoteBody;
import com.zkl.ZKLRussian.control.note_old.note.NoteStream;
import com.zkl.ZKLRussian.control.tools.stringData.StringData;

import java.util.ArrayList;

public class TextCoder {
	public static final int writingNoteHeader_version_meaningNote1 = 1;
	public static String getStringNoteHeader(){
		StringData header = new StringData();
		header.add(writingNoteHeader_version_meaningNote1);
		header.add("不要动这个大括号哦！");
		header.add(NoteBody.Type.meaning.ordinal());
		 return header.toString()+"\n\n\n";
	}

	public static String encodeStringNoteBody(NoteStream noteStream, boolean withHeader) {
		StringBuilder stringBuilder = new StringBuilder();
		if(withHeader) stringBuilder.append(getStringNoteHeader());
		noteStream.begin();
		while (noteStream.goNext()) {
			LocalNote note=noteStream.get();
			if (note.getNoteBody() instanceof NoteBody.QuestionNoteBody) {
				stringBuilder.append(encodeStringMeaningSingle((NoteBody.QuestionNoteBody)note.getNoteBody()));
			}
			//else...
		}
		return stringBuilder.toString();
	}
	private static String encodeStringMeaningSingle(NoteBody.QuestionNoteBody questionNoteBody) {
		return questionNoteBody.getQuestion() + "\n" + questionNoteBody.getAnswer() + "\n\n";
	}
	@Nullable
	public static NoteStream decodeStringMeaningNote(String string) {
		boolean headerLegal=false;

		StringData header=null;
		try {
			header = StringData.decode(string);
		} catch (Exception ignored) { }
		if (header == null) {
			string = getStringNoteHeader()+string;
			header = StringData.decode(string);
		}
		try {
			if (header.getInteger(0) == writingNoteHeader_version_meaningNote1 && header.getInteger(2) == NoteBody.Type.meaning.ordinal()) {
				headerLegal = true;
			}
		} catch (Exception ignored) { }


		if (headerLegal) {
			ArrayList<LocalNote> re = new ArrayList<>();
			int cursor = find(string,0,'\n','\t');
			if(cursor==-1) cursor = string.length();
			String word = null;

			while (true) {
				if (cursor!=string.length()) {
					int lineEnd=find(string,cursor+1,'\n','\t');
					if (lineEnd == -1) {
						lineEnd=string.length();
					}
					String nextLine = trimSpace(string.substring(cursor + 1, lineEnd));
					if (nextLine.length() != 0) {
						if (word == null) {
							word = nextLine;
						} else {
							re.add(new LocalQuestionNote(-1,-1,-1,new NoteBody.QuestionNoteBody(word, nextLine),null));
							word = null;
						}
					}
					cursor = lineEnd;
				}else{
					break;
				}
			}
			return new NoteStream.ListNoteStream(re);
		}
		return null;
	}

	private static String trimSpace(String string) {
		int start=string.length();
		for(int i=0;i<start;i++) {
			char c=string.charAt(i);
			if (c != ' ' && c != '\t' && c != '\n' && c!='\r') {
				start = i;
				break;
			}
		}

		int end=start;
		for (int i=string.length()-1;i>=start;i--) {
			char c=string.charAt(i);
			if (c != ' ' && c != '\t' && c != '\n' && c!='\r') {
				end=i+1;
				break;
			}
		}
		return string.substring(start, end);
	}
	private static int find(String string,int start,char... chars) {
		for(int i=start;i<string.length();i++) {
			for (char cha : chars) {
				if (string.charAt(i) == cha) {
					return i;
				}
			}
		}
		return -1;
	}
}
