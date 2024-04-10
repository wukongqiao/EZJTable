package cn.com.agui.util.ezjtable;

public class Constraint {

    private boolean unique = false ;
    private boolean notNull = false ;
    private Object defaultValue = null ;

    public static Constraint createContraint( boolean notNull , boolean unique , Object defaultValue ){
        Constraint con = new Constraint() ;
        con.setNotNull( notNull );
        con.setUnique( unique );
        con.setDefaultValue( defaultValue );
        return con ;
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    public boolean isNotNull() {
        return notNull;
    }

    public void setNotNull(boolean notNull) {
        this.notNull = notNull;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String toString(){
        StringBuffer buf = new StringBuffer() ;
        buf.append( "Constraint:{");
        buf.append( "notnull=").append(notNull ).append(",") ;
        buf.append( "unique=").append(unique ).append(",") ;
        buf.append( "default=").append( defaultValue ) ;
        buf.append( "}") ;
        return buf.toString() ;
    }

}
