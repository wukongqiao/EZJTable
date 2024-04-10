package cn.com.agui.util.ezjtable;

public class Sort {

    private boolean desc = false ;
    private String columnName ;

    public static Sort createSort( String columnName , boolean desc ){
        Sort s = new Sort();
        s.setColumnName(columnName);
        s.setDesc(desc);
        return s ;
    }

    public boolean isDesc() {
        return desc;
    }

    public void setDesc(boolean desc) {
        this.desc = desc;
    }

    public void setColumnName( String columnName ){
        this.columnName = columnName ;
    }

    public String getColumnName(){
        return this.columnName ;
    }
}
