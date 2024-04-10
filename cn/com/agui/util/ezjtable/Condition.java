package cn.com.agui.util.ezjtable;

public class Condition {

    private String columnName ;
    private Object value ;
    private int thisOption = 2 ;
    private Condition nextCon ;
    private int nextOption = 1 ;

    public static final int THIS_OPTION_NOT = 1 ;
    public static final int THIS_OPTION_EQUAL = 2 ;
    public static final int THIS_OPTION_LT = 3 ;
    public static final int THIS_OPTION_NOT_LT = 4 ;
    public static final int THIS_OPTION_GT = 5 ;
    public static final int THIS_OPTION_NOT_GT = 6 ;
    public static final int THIS_OPTION_LIKE = 7 ;
    public static final int THIS_OPTION_IGNORECASE_EQUAL = 8 ;
    public static final int THIS_OPTION_IGNORECASE_NOT = 9 ;
    public static final int THIS_OPTION_IGNORECASE_LIKE = 10 ;

    public static final int NEXT_OPTION_AND = 1 ;
    public static final int NEXT_OPTION_OR = 2 ;

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public int getThisOption() {
        return thisOption;
    }

    public void setThisOption(int thisOption) {
        this.thisOption = thisOption;
    }

    public Condition getNextCon() {
        return nextCon;
    }

    public void setNextCon(Condition nextCon) {
        this.nextCon = nextCon;
    }

    public int getNextOption() {
        return nextOption;
    }

    public void setNextOption(int nextOption) {
        this.nextOption = nextOption;
    }

}
