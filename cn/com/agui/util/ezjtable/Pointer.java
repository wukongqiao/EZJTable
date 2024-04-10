package cn.com.agui.util.ezjtable;

public class Pointer implements Comparable{

    private int page ;
    private int row ;
    private long sequence ;

    public String toString(){
        StringBuffer buf = new StringBuffer() ;
        buf.append( "Pointer:{") ;
        buf.append( "page=").append(page).append(",");
        buf.append( "row=").append(row).append(",");
        buf.append( "sequence=").append(sequence);
        buf.append("}") ;
        return buf.toString() ;
    }

    public Pointer( int page , int row ){
        this.page = page ;
        this.row = row ;
    }

    public void setSequence( long sequence ){
        this.sequence = sequence;
    }

    public long getSequence(){
        return sequence ;
    }

    public int getPage(){
        return page ;
    }

    public int getRow(){
        return row ;
    }

    public boolean equals( Pointer p ){
        if( p == null ){
            return false ;
        }
        return ( ( this.page == p.page ) && ( this.row == p.row )) ;
    }

    public int hashCode(){
        return page*100000+row ;
    }

    @Override
    public int compareTo(Object o) {
        if( o == null ){
            return Integer.MIN_VALUE ;
        }
        if( !( o instanceof  Pointer ) ){
            return Integer.MIN_VALUE ;
        }else{
            Pointer p = (Pointer) o ;
            if( this.sequence == p.sequence ){
                return 0 ;
            }
            return new Long( this.sequence ).compareTo( new Long( p.sequence ) ) ;
        }
    }
}
