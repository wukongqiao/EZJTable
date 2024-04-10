package cn.com.agui.util.ezjtable;

public class Column {

    private String name ;
    private Class type ;
    private int index ;
    private Constraint cons ;
    private boolean isIndex ;

    public String toString(){
        StringBuffer buf = new StringBuffer() ;
        buf.append( "Column:{") ;
        buf.append( "index=").append(index).append(",") ;
        buf.append( "name=").append(name).append(",") ;
        buf.append( "type=" ).append( type.getName() ).append(",") ;
        buf.append( "isIndex=").append( isIndex ).append( "," ) ;
        buf.append( cons.toString() ) ;
        buf.append( "}" ) ;
        return buf.toString();
    }

    static Column createSeqColumn(){
        Column seq = new Column();
        seq.name = MetaData.COLUMN_NAME_SEQ;
        seq.type = Long.class ;
        seq.isIndex = true ;
        Constraint con = new Constraint();
        seq.setIndex( 0 ) ;
        con.setNotNull( true );
        con.setUnique( true );
        seq.cons = con ;
        return seq ;
    }

    public static Column createColumn( String name , Class type , Constraint con , boolean isIndex ){
        Column col = new Column();
        col.setName( name );
        col.setType( type );
        col.setConstraint( con );
        col.setIsIndex( isIndex);
        return col ;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class getType() {
        return type;
    }

    public void setType(Class type) {
        this.type = type;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setIsIndex( boolean isIndex ){
        this.isIndex = isIndex ;
    }

    public boolean isIndex(){
        return isIndex ;
    }

    public Constraint getConstraint() {
        return cons;
    }

    public void setConstraint(Constraint cons) {
        this.cons = cons;
    }

}
