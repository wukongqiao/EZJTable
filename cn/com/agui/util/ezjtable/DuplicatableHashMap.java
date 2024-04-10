package cn.com.agui.util.ezjtable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This is a HashMap, witch value is a ArrayList. It constains a help for find a key by value or remove a value from
 * all keys fast. Also all method is synchronized.
 */
public class DuplicatableHashMap {

    private Map<Object,ArrayList<Object>> coreMap = new HashMap<Object, ArrayList<Object>>() ;

    private DuplicatableHashMap valueHelper = null ;

    public Object[] getAllValue( Object[] t ){
        if( valueHelper != null ) {
            return valueHelper.coreMap.keySet().toArray(t);
        }else{
            return null ;
        }
    }

    public String toString(){
        StringBuffer buf = new StringBuffer() ;
        buf.append( "DuplicatableHashMap:{" ) ;
        buf.append( "coreMapSize=").append( coreMap.size() ).append(",") ;
        if( valueHelper != null ) {
            buf.append("valueHelperSize=").append(valueHelper.coreMap.size());
        }
        buf.append( "}") ;
        return buf.toString() ;
    }

    public DuplicatableHashMap( boolean withValueHelper ) {
        if( withValueHelper ){
            valueHelper = new DuplicatableHashMap( false ) ;
        }
    }

    public synchronized boolean put( Object key , Object value ){
        if( coreMap.containsKey( key ) ){
            ArrayList<Object> values = coreMap.get(key) ;
            if( values.contains(value)){
                return false ;
            }else{
                values.add(value) ;
                if( valueHelper != null ) {
                    valueHelper.put(value, key);
                }
                return true ;
            }
        }else{
            ArrayList<Object> values = new ArrayList<Object>();
            values.add(value) ;
            coreMap.put( key , values ) ;
            if( valueHelper != null ) {
                valueHelper.put(value, key);
            }
            return true ;
        }
    }

    public synchronized Object[] get( Object key , Object[] type ){
        ArrayList<Object> values = coreMap.get(key) ;
        if( values == null ){
            return null ;
        }else{
            return values.toArray( type ) ;
        }
    }

    public synchronized Object[] getNot( Object key , Object[] type ){
        Iterator keys = coreMap.keySet().iterator() ;
        if( keys == null ){
            return null ;
        }
        ArrayList< Object > ret = new ArrayList<Object>() ;
        while( keys.hasNext() ){
            Object k = keys.next() ;
            if( !key.equals( k ) ){
                ret.addAll( coreMap.get( k ) ) ;
            }
        }
        return ret.toArray( type ) ;
    }

    public synchronized Object[] getAll( Object[] type ){
        Iterator keys = coreMap.keySet().iterator() ;
        ArrayList< Object > ret = new ArrayList<Object>() ;
        while( keys.hasNext() ){
            Object k = keys.next() ;
            ret.addAll( coreMap.get( k ) ) ;
        }
        return ret.toArray( type ) ;
    }

    public synchronized boolean remove( Object key , Object value ) {
        ArrayList<Object> values = coreMap.remove(key);
        if (values == null) {
            return false ;
        } else {
            if( values.remove( value ) ){
                if( values.size() == 0 ){
                    coreMap.remove( key ) ;
                }
                return true ;
            }else{
                return false ;
            }
        }
    }

    public synchronized Object[] remove( Object key , Object[] type ){
         ArrayList<Object> values = coreMap.remove( key ) ;
         if( values == null ){
             return null ;
         }else{
             return values.toArray( type ) ;
         }
    }

    public synchronized Object[] removeValue( Object value ) {
        if( valueHelper != null ) {
            Object[] keys = valueHelper.remove(value , new Object[0]);
            if (keys == null) {
                return null;
            } else {
                for (int i = 0; i < keys.length; i++) {
                    ArrayList values = coreMap.get( keys[i] ) ;
                    if( values.contains( value ) ){
                        values.remove( value ) ;
                        if( values.size() == 0 ){
                            coreMap.remove( keys[i] ) ;
                        }
                        break ;
                    }
                }
                return keys;
            }
        }
        return null ;
    }

    public synchronized Object[] getByValue( Object value ){
        if( valueHelper != null ){
            return valueHelper.get( value , new Object[0]) ;
        }
        return null ;
    }

    public synchronized boolean constains( Object key , Object value ){
        ArrayList<Object> values = coreMap.get(key) ;
        return values.contains( value ) ;
    }

    public synchronized int cleanValueHelper(){
        int removed = 0 ;
        if( valueHelper != null ) {
            Iterator values = valueHelper.coreMap.keySet().iterator();
            while( values.hasNext() ){
                Object value = values.next() ;
                Object[] keys = valueHelper.get(value , new Object[0]) ;
                for( int i = 0 ; i < keys.length ; i ++ ){
                    if( !constains( keys[i] , value ) ){
                        valueHelper.remove( value , keys[i] ) ;
                        ++removed ;
                    }
                }
            }
        }
        return removed;
    }

}
