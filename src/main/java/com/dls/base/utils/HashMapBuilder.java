package com.dls.base.utils;

import java.util.HashMap;

public class HashMapBuilder {

    private HashMap<String, Object> hashMap;

    public HashMapBuilder(){
        hashMap = new HashMap<String, Object>();
    }

    public HashMapBuilder put(String key, Object o){
        hashMap.put(key, o);
        return this;
    }

    public HashMap<String, Object> getHashMap(){
        return hashMap;
    }

}
