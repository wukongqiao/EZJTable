package cn.com.agui.util.ezjtable;

public class Page {

    private Row[] rows ;

    public String toString(){
        StringBuffer buf = new StringBuffer() ;
        buf.append( "Page:{") ;
        buf.append( "rows=" ).append( rows.length ).append( ",") ;
        buf.append( "setted=" ).append( setted() );
        buf.append( "}" ) ;
        return buf.toString() ;
    }

    public Page( int size ){
        rows = new Row[size] ;
    }

    public void addRow( int index , Row row ){
        if( row != null ) {
            rows[index] = row;
        }
    }

    public void removeRow( int index ){
       rows[index] = null ;
    }

    public Row getRow( int index ){
        return rows[index] ;
    }

    public int setted(){
        int count = 0 ;
        for( int i = 0 ; i < rows.length ; i ++ ){
            if( rows[i] != null ){
                count += 1 ;
            }
        }
        return count ;
    }

}
