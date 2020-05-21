package com.emis.test;

public class emisSynctest
{

    private static emisSynctest _sync;

    public static emisSynctest get()
    {
        return _sync;
    }

    public static void testStaticNoSync()
    {
        int i=0;
        while( i< 10000)
        {
            i++;
        }
    }

    public static synchronized void testStaticSync()
    {
        int i=0;
        while( i< 10000)
        {
            i++;
        }
    }

    public synchronized void testSync()
    {
        int i=0;
        while( i< 10000)
        {
            i++;
        }
    }

    public void testNonSync()
    {
        int i=0;
        while( i< 10000)
        {
            i++;
        }
    }



    public static void main(String []argv) throws Exception
    {
        _sync = new emisSynctest();

        long t1 = 0;
        long t2 = 0;

        t1 = System.currentTimeMillis();
        for(int i = 0 ; i < 1000 ; i++)
        {
            emisSynctest.testStaticSync();
        }
        t2 = System.currentTimeMillis();
        System.out.println("Static,synchronized:" + (t2-t1));
//-----------------------------------------------------
        t1 = System.currentTimeMillis();
        for(int i = 0 ; i < 1000 ; i++)
        {
            emisSynctest.testStaticNoSync();
        }
        t2 = System.currentTimeMillis();
        System.out.println("Static,Non synchronized:" + (t2-t1));
//-----------------------------------------------------
        t1 = System.currentTimeMillis();

        emisSynctest _oSync = emisSynctest.get();
        for(int i = 0 ; i < 1000 ; i++)
        {
            _oSync.testNonSync();
        }
        t2 = System.currentTimeMillis();
        System.out.println("Non synchronized:" + (t2-t1));
//-----------------------------------------------------
        t1 = System.currentTimeMillis();
        for(int i = 0 ; i < 1000 ; i++)
        {
            emisSynctest.get().testSync();
        }
        t2 = System.currentTimeMillis();
        System.out.println("synchronized:" + (t2-t1));
    }


}