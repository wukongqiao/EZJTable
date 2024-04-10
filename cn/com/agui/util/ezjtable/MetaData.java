package cn.com.agui.util.ezjtable;

import cn.com.agui.util.ezjtable.exception.IllegalNameException;

import java.util.LinkedHashMap;

public class MetaData {

    private LinkedHashMap< String,Column> columns = new LinkedHashMap<String,Column>() ;
    public static final String COLUMN_NAME_SEQ = "sequence" ;

    public String toString(){
        StringBuffer buf = new StringBuffer();
        buf.append( "MetaData:{") ;
        buf.append( "\ncolCount=").append( columns.size() ).append(",") ;
        String[] names = columns.keySet().toArray(new String[0]);
        for( int i = 0 ; i < names.length ; i ++ ){
            Column col = columns.get(names[i]) ;
            buf.append( "\n").append( col.toString() ).append( "," );
        }
        buf.append("\n}") ;
        return buf.toString() ;
    }

    public static MetaData createMetaData(){
        MetaData md = new MetaData();
        Column seq = Column.createSeqColumn();
        seq.setIndex( 0 );
        md.columns.put( COLUMN_NAME_SEQ , seq ) ;
        return md ;
    }

    public static MetaData createMetaData( Column[] cols ) throws IllegalNameException {
       MetaData meta = createMetaData() ;
       for( int i = 0 ; i < cols.length ; i ++ ){
           meta.addColumn( cols[i] );
       }
       return meta ;
    }

    public int getColCount() {
        return columns.size() ;
    }

    public void addColumn( Column col ) throws IllegalNameException{
        if( COLUMN_NAME_SEQ.equals( col.getName() ) ){
            throw new IllegalNameException( COLUMN_NAME_SEQ ) ;
        }
        col.setIndex( columns.size() );
        columns.put( col.getName() , col ) ;
    }

    public Column getColumn( String name ) {
        return columns.get( name ) ;
    }

    public Column[] getColumns(){
        String[] names = columns.keySet().toArray(new String[0]);
        Column[] cols = new Column[ names.length ] ;
        for( int i = 0 ; i < names.length ; i ++ ){
            cols[i] = columns.get(names[i]) ;
        }
        return cols ;
    }

}
