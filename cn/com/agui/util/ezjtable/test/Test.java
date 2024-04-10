package cn.com.agui.util.ezjtable.test;

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

public class Test {

    private static Table t = null;
    private static int pageSize = 1000;
    private static int maxPage = 10 ;

    public static void main( String[] args ) throws Exception{
        initTable();
        //select() ;
        //sort() ;
        //unionselect();
        //delete();
        //update() ;
        readonlySelect();
    }

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


}
