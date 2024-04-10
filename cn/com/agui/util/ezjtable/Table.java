package cn.com.agui.util.ezjtable;

import cn.com.agui.util.ezjtable.exception.*;
import cn.com.infosec.netsign.eztable.exception.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Table {

    public static final String version = "1.0.0.0" ;

    private MetaData meta;

    private int pageSize = 1;

    private int maxPage = 1;

    private int freePage = 1;

    private Vector<Pointer> freePointers = new Vector<Pointer>();

    //private Vector<Pointer> pointers = new Vector<Pointer>();

    private ConcurrentHashMap<String, Index> indexes;

    private ConcurrentHashMap<Integer, Page> pages;

    private long sequence = 0L;

    private static final int HASHCODE_NULL = 0;

    private boolean readOnly = false;

    private Row[] cachedRows;

    public String summary() {
        StringBuffer buf = new StringBuffer();
        buf.append("Table:{\n");
        buf.append(meta.toString()).append("\n");
        buf.append("pageSize=").append(pageSize).append(",");
        buf.append("maxPage=").append(maxPage).append(",");
        buf.append("freePage=").append(freePage).append(",");
        buf.append("freePointers=").append(freePointers.size()).append(",");
        buf.append("sequence=").append(sequence).append(",");
        buf.append("readOnly=").append(readOnly).append(",");
        if (cachedRows != null) {
            buf.append("cachedRows=").append(cachedRows.length).append(",");
        }
        buf.append("\n");
        buf.append("indexes=").append(showMap(indexes)).append(",\n");
        buf.append("pages=").append(showMap(pages));
        buf.append("}");
        return buf.toString();
    }

    private static String showMap(Map map) {
        StringBuffer buf = new StringBuffer();
        buf.append("HashMap:{");
        Iterator keys = map.keySet().iterator();
        while (keys.hasNext()) {
            Object key = keys.next();
            Object value = map.get(key);
            buf.append("\n").append(key).append("=").append(value).append(",");
        }
        buf.append("\n}");
        return buf.toString();
    }

    Table() {
    }

    public static Table createTable(int pageSize, int maxPage, final MetaData meta) {
        Table t = new Table();
        if (pageSize > t.pageSize) {
            t.pageSize = pageSize;
        }
        if (maxPage > t.maxPage) {
            t.maxPage = maxPage;
            t.freePage = maxPage;
        }
        t.meta = meta;
        Column[] cols = meta.getColumns();
        t.indexes = new ConcurrentHashMap<String, Index>();
        for (int i = 0; i < cols.length; i++) {
            t.indexes.put(cols[i].getName(), new Index());
        }
        t.pages = new ConcurrentHashMap<Integer, Page>(maxPage);
        try {
            t.newPage();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return t;
    }

    private void newPage() throws NoMorePageException {
        if (freePage == 0) {
            throw new NoMorePageException("");
        }
        int pageNum = Random.nextInt();
        while (pages.get(pageNum) != null) {
            pageNum = Random.nextInt();
        }
        Page p = new Page(pageSize);
        ArrayList<Pointer> newPointers = new ArrayList<Pointer>(pageSize);
        for (int i = 0; i < pageSize; i++) {
            newPointers.add(new Pointer(pageNum, i));
        }
        freePointers.addAll(newPointers);
        freePage -= 1;
        pages.put(pageNum, p);
    }

    public int size() {
        return pages.size() * pageSize - freePointers.size();
    }

    public synchronized void insert(Row row) throws IncorrectColumnCountException, NoMorePageException, NotNullException, NoSuchColumnException, NotUniqueException, NoSuchOptionException, ReadOnlyTableException {
        if (readOnly) {
            throw new ReadOnlyTableException("");
        }
        Column[] cols = meta.getColumns();
        if (cols.length != row.getColumnCount()) {
            throw new IncorrectColumnCountException("Table column count:" + cols.length + " but row column count:" + row.getColumnCount());
        }
        sequence += 1;
        row.setSequence(sequence);
        for (int i = 1; i < cols.length; i++) {
            Column col = cols[i];
            checkConstraint(col, row);
        }
        Pointer p = getPointer(sequence);
        insert(p, row);
    }

    private Pointer getPointer(long sequence) throws NoMorePageException {
        if (freePointers.size() == 0) {
            newPage();
        }
        Pointer p = freePointers.remove(freePointers.size() - 1);
        p.setSequence(sequence);
        return p;
    }

    private void checkConstraint(Column c, Row r) throws NotNullException, NoSuchColumnException, NoSuchOptionException, NotUniqueException {
        Constraint con = c.getConstraint();
        Object data = r.getData(c.getIndex());
        if (con.isNotNull()) {
            if ((data == null) && (con.getDefaultValue() == null)) {
                throw new NotNullException(c.getName() + "(" + c.getIndex() + ")");
            } else if ((data == null) && (con.getDefaultValue() != null)) {
                r.setData(c.getIndex(), con.getDefaultValue());
            }
        }
        if (con.isUnique()) {
            if (data == null) {
                throw new NotNullException(c.getName() + "(" + c.getIndex() + ")");
            }
            Condition ctmp = new Condition();
            ctmp.setColumnName(c.getName());
            ctmp.setValue(data);
            Pointer[] ps = getPointers(this, null, ctmp);
            if ((ps != null) && (ps.length > 0)) {
                throw new NotUniqueException(c.getName() + "(" + c.getIndex() + ")");
            }
        }
    }

    public Row[] select(Condition c, Sort s) throws NoSuchColumnException, NoSuchOptionException {
        Pointer[] ps = getPointers(this, null, c);
        if (ps == null) {
            return null;
        }
        Row[] rs = getRows(ps);
        sort(ps, rs, s);
        return rs;
    }

    private static Pointer[] getAllPointers( Table t ){
        return t.indexes.get( MetaData.COLUMN_NAME_SEQ ).getAllPointer() ;
    }

    public static Pointer[] getPointers(Table t, Pointer[] ps, Condition c) throws NoSuchColumnException, NoSuchOptionException {
        if (c == null) {
            if (ps != null) {
                return ps;
            } else {
                return getAllPointers( t );
            }
        }
        if (c.getColumnName() == null) {
            throw new NoSuchColumnException(c.getColumnName());
        }
        int hashCode = hashCode(c.getValue());
        Pointer[] gp = null;
        if (ps == null) {
            Index index = t.indexes.get(c.getColumnName());
            if (index == null) {
                throw new NoSuchColumnException(c.getColumnName());
            }
            Column col = t.meta.getColumn(c.getColumnName());
            if (c.getThisOption() == Condition.THIS_OPTION_EQUAL) {
                ps = col.isIndex() ? index.getPointers(hashCode) : getAllPointers( t ) ;
                gp = filterPointer(t, ps, c.getColumnName(), c.getValue(), c.getThisOption());
            } else if (c.getThisOption() == Condition.THIS_OPTION_NOT) {
                if (col.isIndex()) {
                    Pointer[] psNot = index.getPointersNot(hashCode);
                    //System.out.println( "psNot:"+ psNot.length ) ;
                    Pointer[] psEq = index.getPointers(hashCode);
                    //System.out.println( "psEq:"+ psEq.length ) ;
                    Pointer[] psNotInEq = filterPointer(t, psEq, c.getColumnName(), c.getValue(), c.getThisOption());
                    //System.out.println( "psNotInEq:"+ psNotInEq.length ) ;
                    gp = new Pointer[psNot.length + psNotInEq.length];
                    System.arraycopy(psNot, 0, gp, 0, psNot.length);
                    System.arraycopy(psNotInEq, 0, gp, psNot.length, psNotInEq.length);
                } else {
                    ps = col.isIndex() ? index.getPointers(hashCode) : getAllPointers(t);
                    gp = filterPointer(t, ps, c.getColumnName(), c.getValue(), c.getThisOption());
                }
            } else {
                ps = getAllPointers(t);
                gp = filterPointer(t, ps, c.getColumnName(), c.getValue(), c.getThisOption());
            }
        } else {
            gp = filterPointer(t, ps, c.getColumnName(), c.getValue(), c.getThisOption());
        }
        if (c.getNextCon() != null) {
            if (c.getNextOption() == Condition.NEXT_OPTION_AND) {
                gp = getPointers(t, gp, c.getNextCon());
            } else if (c.getNextOption() == Condition.NEXT_OPTION_OR) {
                Pointer[] gpn = getPointers(t, null, c.getNextCon());
                gp = mergePointer(gp, gpn, c.getNextOption());
            }
        }
        return gp;
    }

    private static Pointer[] mergePointer(Pointer[] ps1, Pointer[] ps2, int option) throws NoSuchOptionException {
        ArrayList<Pointer> mpsList = new ArrayList<Pointer>();
        Pointer[] psLong = (ps1.length >= ps2.length) ? ps1 : ps2;
        Pointer[] psShort = (ps1.length >= ps2.length) ? ps2 : ps1;
        ArrayList<Pointer> pslist = new ArrayList<Pointer>();
        for (int i = 0; i < psShort.length; i++) {
            pslist.add(psShort[i]);
        }
        if (option == Condition.NEXT_OPTION_AND) {
            for (int i = 0; i < psLong.length; i++) {
                if (pslist.contains(psLong[i])) {
                    mpsList.add(psLong[i]);
                }
            }
        } else if (option == Condition.NEXT_OPTION_OR) {
            mpsList = pslist;
            for (int i = 0; i < psLong.length; i++) {
                if (!mpsList.contains(psLong[i])) {
                    mpsList.add(psLong[i]);
                }
            }
        } else {
            throw new NoSuchOptionException(option + "");
        }
        return mpsList.toArray(new Pointer[0]);
    }

    private static Pointer[] filterPointer(Table t, Pointer[] ps, String columnName, Object value, int option) throws NoSuchOptionException {
        ArrayList<Pointer> ret = new ArrayList<Pointer>();
        for (int i = 0; i < ps.length; i++) {
            Row r = t.pages.get(ps[i].getPage()).getRow(ps[i].getRow());
            Object data = t.getDataByColumnName(columnName, r);
            if (option == Condition.THIS_OPTION_EQUAL) {
                if (value == null) {
                    if (data == null) {
                        ret.add(ps[i]);
                    }
                } else {
                    if (value.equals(data)) {
                        ret.add(ps[i]);
                    }
                }
            } else if (option == Condition.THIS_OPTION_NOT) {
                if (value == null) {
                    if (data != null) {
                        ret.add(ps[i]);
                    }
                } else {
                    if (!value.equals(data)) {
                        ret.add(ps[i]);
                    }
                }
            } else if (option == Condition.THIS_OPTION_GT) {
                if (value == null) {
                    if (data != null) {
                        ret.add(ps[i]);
                    }
                } else {
                    if (((Comparable) value).compareTo(data) < 0) {
                        ret.add(ps[i]);
                    }
                }
            } else if (option == Condition.THIS_OPTION_LT) {
                if (value != null) {
                    if (((Comparable) value).compareTo(data) > 0) {
                        ret.add(ps[i]);
                    }
                }
            } else if (option == Condition.THIS_OPTION_NOT_GT) {
                if (value == null) {
                    if (data == null) {
                        ret.add(ps[i]);
                    }
                } else {
                    if (((Comparable) value).compareTo(data) >= 0) {
                        ret.add(ps[i]);
                    }
                }
            } else if (option == Condition.THIS_OPTION_NOT_LT) {
                if (value == null) {
                    ret.add(ps[i]);
                } else {
                    if (((Comparable) value).compareTo(data) <= 0) {
                        ret.add(ps[i]);
                    }
                }
            } else if (option == Condition.THIS_OPTION_LIKE) {
                if ((value != null) && (data != null)) {
                    if (data.toString().indexOf(value.toString()) > -1) {
                        ret.add(ps[i]);
                    }
                }
            } else if (option == Condition.THIS_OPTION_IGNORECASE_EQUAL) {
                if (value == null) {
                    if (data == null) {
                        ret.add(ps[i]);
                    }
                } else {
                    if (data != null) {
                        if (value.toString().toUpperCase().equals(data.toString().toUpperCase())) {
                            ret.add(ps[i]);
                        }
                    }
                }
            } else if (option == Condition.THIS_OPTION_IGNORECASE_NOT) {
                if (value == null) {
                    if (data != null) {
                        ret.add(ps[i]);
                    }
                } else {
                    if (data == null) {
                        ret.add(ps[i]);
                    } else {
                        if (!value.toString().toUpperCase().equals(data.toString().toUpperCase())) {
                            ret.add(ps[i]);
                        }
                    }
                }
            } else if (option == Condition.THIS_OPTION_IGNORECASE_LIKE) {
                if (value != null && data != null) {
                    if (data.toString().toUpperCase().indexOf(value.toString().toUpperCase()) > -1) {
                        ret.add(ps[i]);
                    }
                }
            } else {
                throw new NoSuchOptionException(option + "");
            }
        }
        return ret.toArray(new Pointer[0]);
    }

    private synchronized void insert(Pointer p, Row r) {
        Column[] cols = meta.getColumns();
        for (int i = 0; i < cols.length; i++) {
            if (cols[i].isIndex()) {
                indexes.get(cols[i].getName()).addIndex(hashCode(r.getData(i)), p);
            }
        }
        //System.out.println( p.toString() ) ;
        pages.get(p.getPage()).addRow(p.getRow(), r);
    }

    private Object getDataByColumnName(String columnName, Row r) {
        Column c = meta.getColumn(columnName);
        return r.getData(c.getIndex());
    }

    private static int hashCode(Object o) {
        return (o == null) ? HASHCODE_NULL : o.hashCode();
    }

    public synchronized int delete(Condition c) throws NoSuchColumnException, NoSuchOptionException, ReadOnlyTableException {
        if (readOnly) {
            throw new ReadOnlyTableException("");
        }
        Pointer[] ps = getPointers(this, null, c);
        if (ps == null || ps.length == 0) {
            return 0;
        }
        Column[] cols = meta.getColumns();
        for (int i = 0; i < cols.length; i++) {
            if (cols[i].isIndex()) {
                Index index = indexes.get(cols[i].getName());
                for (int j = 0; j < ps.length; j++) {
                    index.removePonter(ps[j]);
                }
            }
        }
        for (int i = 0; i < ps.length; i++) {
            pages.get(ps[i].getPage()).removeRow(ps[i].getRow());
        }
        for (int j = 0; j < ps.length; j++) {
            freePointer(ps[j]);
        }
        return ps.length;
    }

    private void freePointer(Pointer p) {
        freePointers.add(p);
    }

    private void sort(Pointer[] ps, Row[] rs, Sort s) throws NoSuchColumnException {
        if (s == null || s.getColumnName() == null) {
            return;
        }
        if (rs == null) {
            return;
        }
        Column c = meta.getColumn(s.getColumnName());
        if (c == null) {
            throw new NoSuchColumnException("sort by " + s.getColumnName());
        }
        SortHelper[] cs = new SortHelper[rs.length];
        for (int i = 0; i < rs.length; i++) {
            cs[i] = new SortHelper();
            cs[i].setP(ps[i]);
            cs[i].setData((Comparable) rs[i].getData(c.getIndex()));
        }
        Arrays.sort(cs);
        if (s.isDesc()) {
            if (cs.length > 1) {
                for (int i = 0; i < ps.length / 2; i++) {
                    SortHelper tmpc = cs[i];
                    cs[i] = cs[cs.length - 1 - i];
                    cs[cs.length - 1 - i] = tmpc;
                }
            }
        }
        for (int i = 0; i < cs.length; i++) {
            rs[i] = pages.get(cs[i].getP().getPage()).getRow(cs[i].getP().getRow());
        }
    }

    private Row[] getRows(Pointer[] ps) {
        Row[] rs = new Row[ps.length];
        for (int i = 0; i < ps.length; i++) {
            rs[i] = pages.get(ps[i].getPage()).getRow(ps[i].getRow());
        }
        return rs;
    }

    public synchronized int update(Condition where, Condition[] set) throws NoSuchColumnException, NoSuchOptionException, NotUniqueException, NotNullException, ReadOnlyTableException {
        if (readOnly) {
            throw new ReadOnlyTableException("");
        }
        if (set == null || set.length == 0) {
            return 0;
        }
        Pointer[] ps = getPointers(this, null, where);
        if (ps != null && ps.length > 0) {
            for (int i = 0; i < ps.length; i++) {
                Row r = pages.get(ps[i].getPage()).getRow(ps[i].getRow());
                for (int j = 0; j < set.length; j++) {
                    Column c = meta.getColumn(set[j].getColumnName());
                    if (c == null) {
                        throw new NoSuchColumnException(set[j].getColumnName());
                    }
                    checkConstraint(c, r);
                    r.setData(c.getIndex(), set[j].getValue());
                    if (c.isIndex()) {
                        Index index = indexes.get(c.getName());
                        index.removePonter(ps[i]);
                        index.addIndex(hashCode(r.getData(c.getIndex())), ps[i]);
                    }
                }
            }
            return ps.length;
        }
        return 0;
    }

    public synchronized int initReadOnlySelect(Condition c, Sort s) throws NoSuchColumnException, NoSuchOptionException {
        readOnly = true;
        cachedRows = select(c, s);
        return cachedRows.length;
    }

    public synchronized Row[] readOnlySelect(int pos, int length) {
        if (cachedRows == null) {
            return null;
        }
        if (length <= 0) {
            return new Row[0];
        }
        Row[] rs = new Row[Math.min(length, cachedRows.length - pos)];
        System.arraycopy(cachedRows, pos, rs, 0, rs.length);
        return rs;
    }

    public synchronized void finishReadOnlySelect() {
        cachedRows = null;
        readOnly = false;
    }

    public boolean isReadOnly() {
        return readOnly;
    }


}
