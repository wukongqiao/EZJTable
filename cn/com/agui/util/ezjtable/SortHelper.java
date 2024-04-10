package cn.com.agui.util.ezjtable;

public class SortHelper implements Comparable{

    private Comparable data ;
    private Pointer p ;

    SortHelper(){
    }

    public Object getData() {
        return data;
    }

    public void setData(Comparable data) {
        this.data = data;
    }

    public Pointer getP() {
        return p;
    }

    public void setP(Pointer p) {
        this.p = p;
    }


    @Override
    public int compareTo(Object o) {
        if( o == null || !(o instanceof SortHelper)){
            return Integer.MIN_VALUE;
        }
        SortHelper c = (SortHelper) o ;
        return this.data.compareTo( c.data );
    }
}
