package com.dls.base.utils;

import java.util.ArrayList;

public class ArrayListBuilder {

    private ArrayList arrayList;

    public ArrayListBuilder (){
        arrayList = new ArrayList();
    }

    public ArrayListBuilder add(Object o){
        arrayList.add(o);
        return this;
    }

    public ArrayList getArrayList(){
        return arrayList;
    }

}
