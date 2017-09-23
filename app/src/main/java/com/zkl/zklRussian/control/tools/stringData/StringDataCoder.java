package com.zkl.zklRussian.control.tools.stringData;

public class StringDataCoder
{
	static public final String START_BRACKET ="{";
	static public final String END_BRACKET="}";
	static public final String DIVIDER="|";
	static public final String TRANSFORMER="\\";
	static public final String OFFICIAL_STRING=START_BRACKET+END_BRACKET+DIVIDER+TRANSFORMER;
	
	static public final char CHAR_START_BRACKET =START_BRACKET.charAt(0);
	static public final char CHAR_END_BRACKET =END_BRACKET.charAt(0);
	static public final char CHAR_DIVIDER=DIVIDER.charAt(0);
	static public final char CHAR_TRANSFORMER=TRANSFORMER.charAt(0);
	static public final char[] CHARS_OFFICIAL_STRING=OFFICIAL_STRING.toCharArray();
	
	
	static protected  String getTransformed(String str){
		StringBuilder sb=new StringBuilder(str);
		for(int i=0;i<sb.length();i++){
			for(char c:CHARS_OFFICIAL_STRING){
				if(sb.charAt(i)==c){
					sb.insert(i,TRANSFORMER);
					i++;
					break;
				}
			}
		}
		return sb.toString();
	}
	static protected String ridTransform(String string){
		StringBuilder sb=new StringBuilder(string);
		for(int i=0;i<sb.length();i++){
			if(sb.charAt(i)==CHAR_TRANSFORMER){
				sb.deleteCharAt(i);
			}
		}
		return sb.toString();
	}
	
	static private boolean isTransformed(String string,int index){
		if(index!=0){
			if(string.charAt(index-1)==CHAR_TRANSFORMER){
				if(!isTransformed(string,index-1)){
					return true;
				}
			}
		}
		return false;
	}
	
	static private int next(String string,char target,int startIndex){
		for(int i=startIndex;i<string.length();i++){
			if(string.charAt(i)==target){
				if(!isTransformed(string,i)){
					return i;
				}
			}
		}
		return -1;
	}
	static private int nextStartBracket(String string,int startIndex){
		return next(string, CHAR_START_BRACKET,startIndex);
	}
	static private int nextEndBracket(String string,int startIndex){
		return next(string, CHAR_END_BRACKET,startIndex);
	}
	static private int nextDivider(String string,int startIndex){
		return next(string,CHAR_DIVIDER,startIndex);
	}
	
	protected static StringData decode(String string){
		CursorString cursorString = new CursorString(string,nextStartBracket(string,0));
		return decode(cursorString);
	}
	protected static StringData decode(CursorString cursorString) {
		StringData re=new StringData();

		cursorString.cursor++;
		while (true) {
			if (cursorString.getChar() == CHAR_START_BRACKET) {
				re.add(decode(cursorString));
				cursorString.cursor++;
			}else if(cursorString.getChar()== CHAR_END_BRACKET){
				cursorString.cursor++;
				break;
			}else{
				int start=cursorString.cursor;
				int end=nextDivider(cursorString.string, start);
				re.add(ridTransform(cursorString.string.substring(start, end)));
				cursorString.cursor=end+1;
			}
		}
		return re;
	}
	protected static String encode(StringData sd){
		StringBuilder sb=new StringBuilder("");
		sb.append(START_BRACKET);
		for(int i=0;i<sd.size();i++){
			Object obj=sd.get(i);
			if(obj instanceof String){
				sb.append(getTransformed((String) obj));
			}else if(obj instanceof EncodedStringData){
				sb.append(((EncodedStringData) obj).string);
			}else if(obj instanceof StringData){
				sb.append(encode((StringData) obj));
			}else if (obj == null) {
				sb.append("");
			}
			sb.append(DIVIDER);
		}
		sb.append(END_BRACKET);
		return sb.toString();
	}

}
