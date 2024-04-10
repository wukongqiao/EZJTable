package cn.com.agui.util.ezjtable;

public class Index {

    private DuplicatableHashMap dhm = new DuplicatableHashMap(true);

    public String toString(){
        StringBuffer buf = new StringBuffer() ;
        buf.append("Index:{") ;
        buf.append( dhm.toString() ) ;
        buf.append( "}") ;
        return buf.toString() ;
    }

    public void addIndex( int hashCode , Pointer p ){
        dhm.put(hashCode,p) ;
    }

    public Pointer[] getPointers( int hashCode ){
        Pointer[] ps = (Pointer[]) dhm.get( hashCode , new Pointer[0])  ;
        return ( ps == null ) ? new Pointer[0] : ps ;
    }

    public Pointer[] getPointersNot( int hashCode ){
        Pointer[] ps = (Pointer[]) dhm.getNot( hashCode , new Pointer[0]) ;
        return ( ps == null ) ? new Pointer[0] : ps ;
    }

    public int removePonter( Pointer p ){
        Object[] hashCodes = dhm.removeValue( p ) ;
        return hashCodes.length ;
    }

    public Pointer[] getAllPointer(){
        return (Pointer[]) dhm.getAllValue( new Pointer[0] ) ;
    }

}
