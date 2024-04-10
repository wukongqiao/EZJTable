package cn.com.agui.util.ezjtable;

public class Row {

    private Object[] data ;

    public static Row createRow( Object[] data ){
        Row r = new Row();
        r.data = new Object[ data.length + 1 ] ;
        System.arraycopy( data , 0 , r.data , 1 , data.length );
        //System.out.println( r.toString() ) ;
        return r ;
    }

    public int getColumnCount(){
        return data.length ;
    }

    public Object getData( int colIndex ){
        return data[colIndex ] ;
    }

    void setData( int colIndex , Object data){
        this.data[colIndex] = data ;
    }

    void setSequence( long sequence ){
        data[0] = sequence ;
    }

    public String toString(){
        StringBuffer buf = new StringBuffer() ;
        buf.append( "Row:{ " ) ;
        for( int i = 0 ; i < data.length ; i ++ ){
            if( data[i] != null ) {
                buf.append("[" +data[i]+ "] ") ;
            }else{
                buf.append( "null " ) ;
            }
        }
        buf.append( "}") ;
        return buf.toString();
    }

}
