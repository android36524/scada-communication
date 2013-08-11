package com.ht.scada.communication;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: 薄成文 13-8-7 下午6:18
 * To change this template use File | Settings | File Templates.
 */
public class VarGroupTable {
    private final List<String> ycVarList = new ArrayList<>();
    private final List<String> ycArrayVarList = new ArrayList<>();
    private final List<String> ymVarList = new ArrayList<>();
    private final List<String> yxVarList = new ArrayList<>();
    private final List<String> asciiTagVarList = new ArrayList<>();

    public List<String> getYcVarList() {
        return ycVarList;
    }

    public List<String> getYcArrayVarList() {
        return ycArrayVarList;
    }

    public List<String> getYmVarList() {
        return ymVarList;
    }

    public List<String> getYxVarList() {
        return yxVarList;
    }

    public List<String> getAsciiTagVarList() {
        return asciiTagVarList;
    }
}
