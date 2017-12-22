package com.zkl.memruss.control.tools.stringData;


class CursorString {
	public int cursor = 0;
	public String string;

	CursorString(String string, int cursor) {
		this.string = string;
		this.cursor = cursor;
	}

	char getChar() {
		return string.charAt(cursor);
	}
}
