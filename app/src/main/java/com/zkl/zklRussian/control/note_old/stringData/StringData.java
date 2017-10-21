package com.zkl.zklRussian.control.note_old.stringData;

import java.util.ArrayList;

public class StringData {
	private ArrayList<Object> data;
	public StringData(){
		data=new ArrayList<>();
	}
	
	
	public int size(){ return data.size(); }
	protected Object get(int index) {
		return data.get(index);
	}
	public StringData getStringData(int index){
		if(index>-1 && index<data.size()) {
			if (isStringData(index)) return (StringData) data.get(index);
		}
		return null;
	}
	public String getString(int index){
		if(index>-1 && index<data.size()) {
			if (isString(index)) return (String) data.get(index);
		}
		return null;
	}
	public EncodedStringData getEncodedStringData(int index){
		if(index>-1 && index<data.size()) {
			if (isEncodedStringData(index)) return (EncodedStringData) data.get(index);
		}
		return null;
	}
	/**if the index or the value is not legal,the default value -1 will be returned**/
	public int getInteger(int index){ return getInteger(index, -1); }
	private int getInteger(int index, int defaultValue){
		if(index>-1 && index<data.size()) {
			return Integer.parseInt(getString(index));
		}
		return defaultValue;
	}
	public long getLong(int index) { return Long.parseLong(getString(index)); }
	public float getFloat(int index) { return Float.parseFloat(getString(index)); }


	public void add(String string){ data.add(string); }
	private void addAsEncodedStringData(String string){ data.add(new EncodedStringData(string)); }
	public void add(StringData sd){
		if(sd==this){
			throw new RuntimeException("A stringData can not add itself as its sub data or it will cause a dead cycle. "+
					"You can use method clone() to make a copy of itself to add.");
		}else{
			data.add(sd);
		}
	}
	protected void add(EncodedStringData encodedStringData) {
		data.add(encodedStringData);
	}

	public void add(boolean b) { add(b + ""); }
	public void add(int i){add(i + "");}
	public void add(long l) { add(l+""); }
	public void add(float f) { add(String.valueOf(f)); }



	private boolean isString(int index){ return (data.get(index) instanceof String); }
	private boolean isStringData(int index){ return (data.get(index) instanceof StringData); }
	private boolean isEncodedStringData(int index){ return (data.get(index) instanceof EncodedStringData);}



	@Override public String toString() { return encode(this); }


	public static StringData decode(String string){ return StringDataCoder.decode(string); }
	/** @param cursorString 读取完后cursor会停在结束端的后面一个字符
	 * @return 读取的最近一个StringData，若cursor之后不存在StringData则返回null */
	protected static StringData decode(CursorString cursorString){
		int nextStartBracket=cursorString.string.indexOf(StringDataCoder.START_BRACKET,cursorString.cursor);
		if (nextStartBracket == -1) {
			return null;
		} else {
			cursorString.cursor = nextStartBracket;
			return StringDataCoder.decode(cursorString);
		}
	}

	private static String encode(StringData stringData){ return StringDataCoder.encode(stringData); }
	private static StringData clone(StringData stringData) {
		StringData re=new StringData();
		for (int i=0;i<stringData.size();i++) {
			Object object = stringData.get(i);
			if(object instanceof String){
				re.addAsEncodedStringData(object + "");
			}else if(object instanceof StringData){
				re.add(clone((StringData) object));
			}else if (object instanceof EncodedStringData) {
				re.addAsEncodedStringData(object.toString());
			}
		}
		return re;
	}

}
