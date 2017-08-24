package com.zkl.ZKLRussian.control.tools.stringData;


class CursorString {
	public int cursor = 0;
	public String string;

	public CursorString(String string, int cursor) {
		this.string = string;
		this.cursor = cursor;
	}

	public char getChar() {
		return string.charAt(cursor);
	}
}
