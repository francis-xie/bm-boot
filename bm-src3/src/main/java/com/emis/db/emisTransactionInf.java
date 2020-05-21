package com.emis.db;



public interface emisTransactionInf
{
    //public emisDb getConnection();
    public void setConnection();
    public void putSQL();
    public String getSQL();
    public void close();
    

}
