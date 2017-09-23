package com.zkl.zklRussian.control.note_old.database;

public class Selection {
    public String[] includedColumns;


    public void setIncludedColumnsByArray(String[] includedColumns) { this.includedColumns = includedColumns; }
    public void setIncludedColumns(String... includedColumns) {
        setIncludedColumnsByArray(includedColumns);
    }
    public void setIncludedAllColumns() { this.includedColumns=null; }

    /**文本值记得加单引号！！**/
    public String whereExpression;
    /**文本值记得加单引号！！**/
    public void setWhereExpression(String whereExpression) { this.whereExpression = whereExpression; }
    public void setSelectAllRaw() { this.whereExpression=null; }

    public String orderByExpression;
    public void setOrderBy(String column,boolean isAsc) {
        this.orderByExpression = column+" "+(isAsc?"asc":"desc");
    }
    public void setOrderByExpression(String[] columns){
        this.orderByExpression ="";
        for(int i=0;i<columns.length;i++){
            if(columns[i]!=null){
                this.orderByExpression +=columns[i];
            }
            if(i!=columns.length-1){
                this.orderByExpression +=",";
            }
        }
    }
    public void setOrderBy(String[] columns,Boolean[] isAsc){
        this.orderByExpression ="";
        for(int i=0;i<columns.length;i++){
            if(columns[i]!=null){
                this.orderByExpression +=columns[i];
                if(isAsc[i]!=null){
                    this.orderByExpression +=" "+(isAsc[i]?"asc":"desc");
                }
            }
            if(i!=columns.length-1){
                this.orderByExpression +=",";
            }
        }
    }

    public String havingExpression;
    public String groupByExpression;

	public String limit;
	public void setLimit(long offset, int count) {
		limit=offset + "," + count;
	}
}
