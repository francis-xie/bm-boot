package com.emis.schedule;

import java.io.File;
import java.io.FileFilter;

/**
 * Created by IntelliJ IDEA.
 * User: merlin
 * Date: 2003/7/8
 * Time: 上午 11:00:52
 * To change this template use Options | File Templates.
 */
public class ExtFilter implements FileFilter {
    protected String pattern;

    public ExtFilter(String pattern) {
        this.pattern = pattern.toUpperCase();
    }

    public boolean accept(File file) {
        String name = file.getName().toUpperCase();
        boolean flag = name.endsWith(pattern);
//        System.out.println(name + ":" + flag);
        return flag;
    }
}
