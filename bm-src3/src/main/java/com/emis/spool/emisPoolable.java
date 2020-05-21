package com.emis.spool;

/**
 * 定義讓 emisComplexSpool 所 Pool 的 Object 所需有的 Interface
 */
public interface emisPoolable
{
    /**
     *  get last access time
     */
    long getTime();

    /**
     * set last access time to current's System time
     */
    void setTime();

    /**
     * set last access time to 'now'
     */
    void setTime( long now );

    /**
     * return the Pooled Object
     */
    Object getPooledObject();


    void setDescription(String sStr);


    /**
     * check the Pooled Object is alive ?
     * has Fetal Error ? or Data Lost ?
     */
    boolean validate();

    void setFatalError();
    boolean hasFatalError();
    Exception getFatalError();

    // free the resource, and orphan is the same , call expire
    void freeResource();

}