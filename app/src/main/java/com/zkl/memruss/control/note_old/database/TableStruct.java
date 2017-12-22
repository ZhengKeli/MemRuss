package com.zkl.memruss.control.note_old.database;

import java.util.ArrayList;


public class TableStruct {
    String tableName;
    ArrayList<Column> columns=new ArrayList<>();

    public TableStruct(String tableName) {
        this.tableName = tableName;
    }

    class Column{
        String name;
        String typeExpress;
        Column(String name, String typeExpress) {
            this.name = name;
            this.typeExpress = typeExpress;
        }
    }

    /**可以作为 rowId的别名**/
    public void addIdColumn(String name) { columns.add(new Column(name,"INTEGER PRIMARY KEY")); }
    public void addIncreasedIdColumn(String name) { columns.add(new Column(name,"INTEGER PRIMARY KEY AUTOINCREMENT")); }
    /**要储存long数据也可以用这个**/
    public void addIntegerColumn(String name){ columns.add(new Column(name,"integer")); }
    public void addFloatColumn(String name) { columns.add(new Column(name,"float")); }
    public void addDoubleColumn(String name) { columns.add(new Column(name,"double")); }
    /**@param p 可储存的总位数
     * @param s 可储存的小数位数**/
    public void addDecimalColumn(String name, int p, int s) { columns.add(new Column(name,"decimal("+p+","+s+")")); }
    public void addTextColumn(String name) { columns.add(new Column(name,"text")); }
    public void addRawDataColumn(String name) { columns.add(new Column(name,"blob")); }
}
