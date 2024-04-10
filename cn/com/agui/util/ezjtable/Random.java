package cn.com.agui.util.ezjtable;

class Random {

    private static java.util.Random ran = new java.util.Random();

    public static synchronized int nextInt(){
        return ran.nextInt() ;
    }
}
