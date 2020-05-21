package com.emis.report;

public interface emisProviderEventListener
{
    /**
     * 只有 printTable( emisTr tr )
     * 才會觸發此 Event
     *
     * 請注意 !!!
     * 在 onBeforeEject 中不要使用 printTable
     * 不然有可能會觸發無窮迴圈
     *
     * 通常此 event 用來寫小計或累計
     */
    public void onBeforeEject();
    /**
     * 只有 printTable( emisTr tr )
     * 才會觸發此 Event
     * 請注意 !!!
     * 在 onAfterEject 中不要使用 printTable
     * 不然有可能會觸發無窮迴圈
     *
     * 通常用來輸出 title , header
     */
    public void onAfterEject();
}