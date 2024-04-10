  This is a table class create by original java, you can use is with jdk1.6 and above. I do this for 
make a class like a table in DB but very light, no sql, no union search, no store( maybe later, very). 
  This table built by pages, and pages built by rows. You must specific how match pages in table and 
how match rows one page, when the table init.Each row have some columns, which you must specific at 
first as well. You can specific some of the columns as index, also the index can be uniq or not. But 
the index is single.You can search on one or more columns, use some condition.

Here is specification in chinese:
类设计
类路径
cn.com.agui.ezjtable.*					       主类
cn.com.agui.ezjtable.exception.*			 异常定义
cn.com.agui.ezjtable.test.Test			   测试代码

DuplicatableHashMap：
本类用Map保存数据，在本项目中用于保存索引，key为索引值，value为数据存储位置。
类中保存Map<Object,ArrayList<Object>>，用于存储key和value，相同的key，可以保存多个value，value存储在ArrayList中，不重复存储。
类中包含一个DuplicatableHashMap，用于存储value对key的对应关系，用于快速用value查询对应的key

Constraint：
用于设定列约束，包含not null，unique两个约束。unique包含not null。

Column：
存放一个列的描述，包括列序号，列名，列约束。

MetaData：
用LinkedHashMap保存column对象，保存顺序与构造MetaData时添加column的顺序相同。保存时，key为类名。
MetaData中的第一列为自动添加的类“sequence”，该列的数据类型为Long，添加行时，自动添加自增数据。删除数据时不会回收已分配的数值。

Row：
用于存放一行数据，构造时需要按顺序填入与MetaData中column顺序对应的数据。由于需要实现排序功能，所以数据必须实现Comparable接口。

Page：
用于存放表数据，包含一个Row数组用于保存多行数据。
Page中用于保存Row的数组不支持扩容，数据存放位置不足时，需要新建Page。

Pointer：
用于存放Row的位置信息，包括：Page对象的序号，在Page中数组的下标。

Sort：
用于指定需要排序的列名，正反序。

SortHelper：
用于加速排序操作，保存要排序的数据和该数据属于的Pointer。对数据排序后，按Pointer顺序获取Row，即可完成排序。

Condition：
查询条件，包括：列名，值，本条件的处理（等于，不等于，大于，小于，不大于，不小于，包含，不区分大小写的等于，不区分大小写不等于，不区分大小写包含），下一个查询条件，与下一个条件的逻辑关系（与，或）。
除：非和等于外，其他本条件处理都会触发全遍历，索引不起作用。

Table：
表数据类，包含：
MetaData——各列的描述
pageSize——Page大小
maxPage——最大Page数
freePage——空闲Page个数
freePointers——保存未分配的Pointer
indexes——保存Index
pages——保存Page


接口设计：
NSTable的主要操作在Table类中完成

public static Table createTable( int pageSize , int maxPage , final MetaData meta )
构造一个Table对象，构造时需要指定Page大小，最大的Page个数，MetaData。构造后不能修改。
构造时：
1、	Table中创建与每个列对应的索引，保存在ConcurrentHashMap<String,Index> indexes中。Key为列名。
2、	创建一个新的Page
3、	创建Page时：
a)	检查已有page是否已超过最大page个数
b)	按Page大小，构造Pointer对象，每个Pointer指向该page的一个下标
c)	将新Pointer保存在freePointer中
d)	Page索引为int类型的随机数
e)	将Page保存在pages中
f)	freePage-1

public int size()：
返回已保存的row个数
总Page个数*Page大小 – 空闲Pointer个数

public synchronized void insert( Row row )
插入一行数据。
1、	检查row数据的列数
2、	获取空闲Pointer
3、	计算行序号，设置到第一列和Pointer中
4、	检查每列的约束
a)	检查失败收回Pointer，报错退出函数
b)	检查通过，添加索引，索引key为该列数据的hashCode，null的hashCode为0，值为Pointer
5、	按Pointer指定的位置，在Page中保存Row值

public Row[] select( Condition c , Sort s )
在一列中查询数据
1、	c==null 时，获取所有行
2、	获取Condition中的查询条件包括columnName、value、thisOption
a)	若该列存在索引
i.	thisOption为等于或不等于时在索引上查询Pointer
1.	取出该列的index
2.	计算value的hashCode
3.	从index中获取全部Pointer
4.	遍历Pointer指向的row
a)	保留columnName指向的数据与value相同的Pointer
b)	遍历该列所有数据，与value按thisOption指定的动作进行比较，保留符合的Pointer
c)	递归Condition中的下一个Condition
i.	若下一个Condition与本Condition关系为And
1.	在上一个条件取得的Pointer中遍历比较
ii.	若下一个Condition与本Condition关系为And
1.	在所有数据中比较，比较方式与上述2.a相同
2.	将结果与上一个条件取得的Pointer合并	
3、	遍历Pointer，在Page中获取Row[]
4、	对Row[]排序
a)	遍历所有Row
i.	用Row中columnName对应的数据和Pointer构造SortHelper
b)	对SortHelper排序
c)	如Sort中指定需要反序，通过原地置换实现SortHelper反序
d)	遍历SortHelper
i.	获取SortHelper中的Pointer
ii.	按新顺序对Row[]重新赋值

public synchronized int delete( Condition c )
删除行
1、	按Condition查询符合的所有Pointer
2、	遍历所有index
a)	删除Pointer
3、	在pages中，删除所有row
4、	释放Pointer到freePointer中

public synchronized int update(Condition where, Condition[] set)
1、	按where查询，得到对应的行
2、	遍历set，替换行中的数据
a)	检查该列的约束
b)	更新该列的索引
c)	修改该列数据
3、	返回修改总行数


public synchronized int initReadOnlySelect(Condition c, Sort s)
1、	将Table置为只读
2、	按Condition和Sort查询数据
3、	缓存查询结果

public synchronized Row[] readOnlySelect(int pos, int length)
1、	返回缓存的子集
2、	length超过最大长度时，只返回可用的数据

public synchronized void finishReadOnlySelect()
1、	清除缓存
2、	恢复只读状态
3、	应在readonlyselect后调用

public boolean isReadOnly()
1、返回当前表是否为只读状态


Here is some examples：

import cn.com.agui.util.ezjtable.*;
import cn.com.agui.util.ezjtable.exception.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;

    private static Table t = null;
    private static int pageSize = 1000;
    private static int maxPage = 10 ;

private static void readonlySelect() throws NoSuchColumnException, NoSuchOptionException {
       Sort s = Sort.createSort( "lable" , false ) ;
       try {
           int allrow = t.initReadOnlySelect(null, s);
           System.out.println("all rows: " + allrow);
           int pagesize = 25;
           int leftrow = allrow;
           while (leftrow > 0) {
               Row[] rows = t.readOnlySelect(allrow - leftrow, pagesize);
               leftrow -= pagesize;
               showResult(rows);
           }
       }finally {
           t.finishReadOnlySelect();
       }
    }

    private static void update() throws NoSuchColumnException, NoSuchOptionException, ReadOnlyTableException, NotUniqueException, NotNullException {
        System.out.println( "update table set issuer='CN=not a ca' notafter='2023-11-16 00:00:00' where lable='Z402581090008'") ;
        Condition c = new Condition();
        c.setColumnName( "lable" );
        c.setValue( "Z402581090008" );
        c.setThisOption( Condition.THIS_OPTION_EQUAL );
        Condition cset1 = new Condition();
        cset1.setColumnName( "notafter" );
        cset1.setValue( "2023-11-16 00:00:00");
        Condition cset2 = new Condition();
        cset2.setColumnName( "issuer" );
        cset2.setValue( "cn=not a ca" );
        int count = t.update( c , new Condition[]{ cset1 , cset2 }) ;
        System.out.println( "update " + count + " rows") ;
        Row[] rs = t.select( c , null ) ;
        showResult( rs );
        c.setColumnName( "issuer");
        c.setValue("cn=not a ca");
        rs = t.select( c , null ) ;
        showResult( rs );

        System.out.println( "update table set issuer='CN=not a ca' notafter='2023-11-16 00:00:00' where dn like '%7313%''") ;
        c = new Condition();
        c.setColumnName( "dn" );
        c.setValue( "7313" );
        c.setThisOption( Condition.THIS_OPTION_LIKE );
        count = t.update( c , new Condition[]{ cset1, cset2 } ) ;
        System.out.println( "update " + count + " rows") ;
        System.out.println( t.summary() );
        rs = t.select( c , null ) ;
        showResult( rs );
    }

    private static void delete() throws NoSuchColumnException, NoSuchOptionException, ReadOnlyTableException {
        System.out.println( "delete table where lable='Z402581090008'") ;
        Condition c = new Condition();
        c.setColumnName( "lable" );
        c.setValue( "Z402581090008" );
        c.setThisOption( Condition.THIS_OPTION_EQUAL );
        //int count = t.delete( c  ) ;
        //System.out.println( "delete " + count + " rows") ;
        //System.out.println( t.summary() );
        //Row[] rs = t.select( c , null ) ;
        //showResult( rs );

        System.out.println( "delete table where dn like '%7313%'") ;
        c = new Condition();
        c.setColumnName( "dn" );
        c.setValue( "7313" );
        c.setThisOption( Condition.THIS_OPTION_LIKE );
        int count = t.delete( c  ) ;
        System.out.println( "delete " + count + " rows") ;
        System.out.println( t.summary() );
        Row[] rs = t.select( c , null ) ;
        showResult( rs );
    }

    private static void unionselect() throws NoSuchColumnException, NoSuchOptionException {
        System.out.println( "select * from table where notbefore>'2016-05-11 00:00:00'' and notbefore<'2016-08-11 00:00:00'") ;
        Condition c1 = new Condition() ;
        c1.setColumnName( "notbefore" );
        c1.setValue( "2016-05-11 00:00:00" );
        c1.setThisOption( Condition.THIS_OPTION_GT );
        Condition c2 = new Condition();
        c2.setColumnName( "notbefore" );
        c2.setValue( "2016-08-11 00:00:00" );
        c2.setThisOption( Condition.THIS_OPTION_LT );
        c1.setNextCon( c2 );
        c1.setNextOption( Condition.NEXT_OPTION_AND );
        Sort s = Sort.createSort( "notbefore" , false ) ;
        Row[] rs= t.select( c1 , s ) ;
        //showResult( rs );

        System.out.println( "select * from table where notbefore<'2016-05-11 00:00:00'' or notbefore>'2016-08-11 00:00:00'") ;
        c1 = new Condition() ;
        c1.setColumnName( "notbefore" );
        c1.setValue( "2016-05-11 00:00:00" );
        c1.setThisOption( Condition.THIS_OPTION_LT );
        c2 = new Condition();
        c2.setColumnName( "notbefore" );
        c2.setValue( "2016-08-11 00:00:00" );
        c2.setThisOption( Condition.THIS_OPTION_GT );
        c1.setNextCon( c2 );
        c1.setNextOption( Condition.NEXT_OPTION_OR );
        rs= t.select( c1 , s ) ;
        //showResult( rs );

        System.out.println( "select * from table where notbefore<'2016-08-11 00:00:00'' or notbefore>'2016-05-11 00:00:00'") ;
        c1 = new Condition() ;
        c1.setColumnName( "notbefore" );
        c1.setValue( "2016-08-11 00:00:00" );
        c1.setThisOption( Condition.THIS_OPTION_LT );
        c2 = new Condition();
        c2.setColumnName( "notbefore" );
        c2.setValue( "2016-05-11 00:00:00" );
        c2.setThisOption( Condition.THIS_OPTION_GT );
        c1.setNextCon( c2 );
        c1.setNextOption( Condition.NEXT_OPTION_OR );
        rs= t.select( c1 , s ) ;
        showResult( rs );
    }

    private static void sort() throws NoSuchColumnException, NoSuchOptionException {
        System.out.println( "select * from table where notbefore<'2016-08-11 00:00:00' sort by notbefore") ;
        Condition c = new Condition();
        c.setColumnName( "notbefore" );
        c.setValue( "2016-08-11 00:00:00" );
        c.setThisOption( Condition.THIS_OPTION_LT );
        Sort s = Sort.createSort( "notbefore" , false ) ;
        Row[] rs= t.select( c , s ) ;
        //showResult( rs );

        System.out.println( "select * from table where notbefore<'2016-08-11 00:00:00' sort by notbefore desc") ;
        c = new Condition();
        c.setColumnName( "notbefore" );
        c.setValue( "2016-08-11 00:00:00" );
        c.setThisOption( Condition.THIS_OPTION_LT );
        s = Sort.createSort( "notbefore" , true ) ;
        rs= t.select( c , s ) ;
        //showResult( rs );
    }

    private static void select() throws NoSuchColumnException, NoSuchOptionException {
        //select *
        System.out.println( "selsec * from table where lable='Z402581090008'") ;
        Condition c = new Condition();
        c.setColumnName( "lable" );
        c.setValue( "Z402581090008" );
        c.setThisOption( Condition.THIS_OPTION_EQUAL );
        Row[] rs= t.select( c , null ) ;
        //showResult( rs ); ;

        System.out.println( "select * from table where lable!='Z402581090008'") ;
        c = new Condition();
        c.setColumnName( "lable" );
        c.setValue( "Z402581090008" );
        c.setThisOption( Condition.THIS_OPTION_NOT );
        rs= t.select( c , null ) ;
        //showResult( rs );

        System.out.println( "select * from table where notbefore<'2016-08-11 00:00:00'") ;
        c = new Condition();
        c.setColumnName( "notbefore" );
        c.setValue( "2016-08-11 00:00:00" );
        c.setThisOption( Condition.THIS_OPTION_LT );
        rs= t.select( c , null ) ;
        //showResult( rs );

        System.out.println( "select * from table where notbefore>'2016-08-11 00:00:00'") ;
        c = new Condition();
        c.setColumnName( "notbefore" );
        c.setValue( "2016-08-11 00:00:00" );
        c.setThisOption( Condition.THIS_OPTION_GT );
        rs= t.select( c , null ) ;
        //showResult( rs );

        System.out.println( "select * from table where notbefore>='2016-08-11 00:00:00'") ;
        c = new Condition();
        c.setColumnName( "notbefore" );
        c.setValue( "2016-08-11 00:00:00" );
        c.setThisOption( Condition.THIS_OPTION_NOT_LT );
        rs= t.select( c , null ) ;
        //showResult( rs );

        System.out.println( "select * from table where notbefore<='2016-08-11 00:00:00'") ;
        c = new Condition();
        c.setColumnName( "notbefore" );
        c.setValue( "2016-08-11 00:00:00" );
        c.setThisOption( Condition.THIS_OPTION_NOT_GT );
        rs= t.select( c , null ) ;
        //showResult( rs );

        System.out.println( "select * from table where dn like '%NX%'") ;
        c = new Condition();
        c.setColumnName( "dn" );
        c.setValue( "NX" );
        c.setThisOption( Condition.THIS_OPTION_LIKE );
        rs= t.select( c , null ) ;
        //showResult( rs );

        System.out.println( "select * from table where upper(sn)=upper(‘67825E632141fe52045a5962Cf67076b’)") ;
        c = new Condition();
        c.setColumnName( "sn" );
        c.setValue( "67825E632141fe52045a5962Cf67076b" );
        c.setThisOption( Condition.THIS_OPTION_IGNORECASE_EQUAL );
        rs= t.select( c , null ) ;
        //showResult( rs );

        System.out.println( "select * from table where upper(sn)!=upper(‘67825E632141fe52045a5962Cf67076b’)") ;
        c = new Condition();
        c.setColumnName( "sn" );
        c.setValue( "67825E632141fe52045a5962Cf67076b" );
        c.setThisOption( Condition.THIS_OPTION_IGNORECASE_NOT );
        rs= t.select( c , null ) ;
        //showResult( rs );

        System.out.println( "select * from table where upper(sn) like upper(‘%62Cf6707%’)") ;
        c = new Condition();
        c.setColumnName( "sn" );
        c.setValue( "62Cf6707" );
        c.setThisOption( Condition.THIS_OPTION_IGNORECASE_LIKE );
        rs= t.select( c , null ) ;
        //showResult( rs );
    }

    private static void showResult( Row[] rs ){
        if( rs != null ){
            System.out.println( "Result:" + rs.length + " rows")  ;
            for( int i = 0 ; i < rs.length ; i++ ){
                StringBuffer buf = new StringBuffer() ;
                buf.append("[" + rs[i].getData( 0 )+ "]") ;
                buf.append("[" + rs[i].getData( 1 )+ "]") ;
                buf.append("[" + rs[i].getData( 2 )+ "]") ;
                buf.append("[" + rs[i].getData( 3 )+ "]");
                buf.append("[" + rs[i].getData( 4 )+ "]");
                buf.append("[" + rs[i].getData( 5 )+ "]");
                buf.append("[" + rs[i].getData( 6 )+ "]");
                System.out.println( buf.toString() ) ;
            }
            System.out.println( "Result:" + rs.length + " rows")  ;
        }else{
            System.out.println( "null" ) ;
        }
    }

    private static void initTable() throws NSTableException, CertificateException, IOException {
        //create table
        Column[] cols = new Column[7] ;
        Constraint conLable = Constraint.createContraint( true , true , null ) ;
        cols[0] = Column.createColumn( "lable" , String.class , conLable , true ) ;
        Constraint conDN = Constraint.createContraint( false , false , null ) ;
        cols[1] = Column.createColumn( "dn" , String.class , conDN , true ) ;
        Constraint conSN = Constraint.createContraint( false , false , null ) ;
        cols[2] = Column.createColumn( "sn" , String.class , conSN , true ) ;
        Constraint conIssuer = Constraint.createContraint( false , false , null ) ;
        cols[3] = Column.createColumn( "issuer" , String.class , conIssuer , true ) ;
        Constraint conNotbefore = Constraint.createContraint( false , false , null ) ;
        cols[4] = Column.createColumn( "notbefore" , Date.class , conNotbefore , false ) ;
        Constraint conNotafter = Constraint.createContraint( false , false , null ) ;
        cols[5] = Column.createColumn( "notafter" , Date.class , conNotafter , false ) ;
        Constraint conCert = Constraint.createContraint( false , false , null ) ;
        cols[6] = Column.createColumn( "cert" , X509Certificate.class , conCert , false ) ;
        MetaData meta = MetaData.createMetaData( cols ) ;
        t = Table.createTable( pageSize , maxPage , meta ) ;

        String certdir = "E:\\DOCUMENTS\\infosec\\技术支持记录\\支持文档\\四川农信\\20161210\\签名服务器配置\\主\\cert\\rawcert\\pbc" ;
        File dir = new File( certdir ) ;
        File[] files = dir.listFiles() ;
        CertificateFactory cf = CertificateFactory.getInstance( "X.509" ) ;
        SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss") ;
        for( int i = 0 ; i < files.length ; i ++ ) {
            if( files[i].getName().endsWith( "cer") ) {
                FileInputStream in = null ;
                X509Certificate cert = null ;
                try {
                    in = new FileInputStream(files[i]) ;
                    cert = (X509Certificate) cf.generateCertificate(in);
                    if( cert == null ) {
                        continue;
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    if( in != null )
                        in.close();
                }
                String dn = cert.getSubjectDN().getName() ;
                String[] pieces = dn.split( "@" ) ;
                Object[] datas = new Object[ 7 ] ;
                datas[0] = pieces[1] ;
                datas[1] = dn ;
                datas[2] = cert.getSerialNumber().toString(16) ;
                datas[3] = cert.getIssuerDN().getName() ;
                datas[4] = sdf.format( cert.getNotBefore() ) ;
                datas[5] = sdf.format( cert.getNotAfter() ) ;
                datas[6] = cert ;
                Row r = Row.createRow( datas ) ;
                t.insert( r );
            }
        }
        System.out.println( t.summary() ) ;

    }
