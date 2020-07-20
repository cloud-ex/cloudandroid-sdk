package com.sl.bymex.data;

import com.alibaba.fastjson.JSON;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HttpData<T> {
    /** 返回码 */
    private String errno;
    /** 提示语 */
    private String message;
    /** 数据 */
    private T data;


    private JSONObject mJSONObject;
    public String getErrno() {
        return errno;
    }

    public void setErrno(String errno) {
        this.errno = errno;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public <T> T getSingleBean(Class<T> clazz) {
        try {
            return JSON.parseObject(data.toString(), clazz);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public <T> T getSingleBean(Class<T> clazz,String key) {
        try {
            if(mJSONObject == null){
                mJSONObject = new JSONObject(data.toString());
            }
            JSONObject keyObject =  mJSONObject.optJSONObject(key);
            return JSON.parseObject(keyObject.toString(), clazz);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public <T> T getSingleBean(Class<T> clazz,String key1,String key2) {
        try {
            if(mJSONObject == null){
                mJSONObject = new JSONObject(data.toString());
            }
            JSONObject key1Object =  mJSONObject.optJSONObject(key1);
            JSONObject key2Object =  key1Object.optJSONObject(key2);
            return JSON.parseObject(key2Object.toString(), clazz);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public  <T> List<T> getBeanList(Class<T> clazz)
    {
        try {
            return JSON.parseArray(data.toString(), clazz);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<T>();
    }

    public  <T> List<T> getBeanList( Class<T> clazz,String key)
    {
        try {
            if(mJSONObject == null){
                mJSONObject = new JSONObject(data.toString());
            }
            JSONArray array =  mJSONObject.optJSONArray(key);
            return JSON.parseArray(array.toString(), clazz);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<T>();
    }

    public  <T> List<T> getBeanList( Class<T> clazz,String key1,String key2)
    {
        try {
            if(mJSONObject == null){
                mJSONObject = new JSONObject(data.toString());
            }
            JSONObject js1 = mJSONObject.optJSONObject(key1);
            JSONArray array =  js1.optJSONArray(key2);
            return JSON.parseArray(array.toString(), clazz);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<T>();
    }

    public  <T> List<T> getBeanList( Class<T> clazz,String key1,String key2,String key3)
    {
        try {
            if(mJSONObject == null){
                mJSONObject = new JSONObject(data.toString());
            }
            JSONObject js1 = mJSONObject.optJSONObject(key1);
            JSONObject js2 = js1.optJSONObject(key2);
            JSONArray array =  js2.optJSONArray(key3);
            return JSON.parseArray(array.toString(), clazz);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<T>();
    }

    public String getStringDataByKey(String key){
        String values = "";
        try {
            if(mJSONObject == null){
                mJSONObject = new JSONObject(data.toString());
            }
            values = mJSONObject.optString(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return values;
    }

    public Boolean getBooleanDataByKey(String key){
        boolean values = false;
        try {
            if(mJSONObject == null){
                mJSONObject = new JSONObject(data.toString());
            }
            values = mJSONObject.optBoolean(key,false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return values;
    }


    public String getStringDataByKey(String key1,String key2){
        String values = "";
        try {
            if(mJSONObject == null){
                mJSONObject = new JSONObject(data.toString());
            }
            JSONObject js1 = mJSONObject.optJSONObject(key1);
            values = js1.optString(key2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return values;
    }

    public String getStringDataByKey(String key1,String key2,String key3){
        String values = "";
        try {
            if(mJSONObject == null){
                mJSONObject = new JSONObject(data.toString());
            }
            JSONObject js1 = mJSONObject.optJSONObject(key1);
            JSONObject js2 = js1.optJSONObject(key2);
            values = js2.optString(key3);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return values;
    }

    public String getStringDataByKey(String key1,String key2,String key3,String key4){
        String values = "";
        try {
            if(mJSONObject == null){
                mJSONObject = new JSONObject(data.toString());
            }
            JSONObject js1 = mJSONObject.optJSONObject(key1);
            JSONObject js2 = js1.optJSONObject(key2);
            JSONObject js3 = js2.optJSONObject(key3);
            values = js3.optString(key4);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return values;
    }

    public int getIntDataByKey(String key1,String key2){
        int values = 0;
        try {
            if(mJSONObject == null){
                mJSONObject = new JSONObject(data.toString());
            }
            JSONObject js1 = mJSONObject.optJSONObject(key1);
            values = js1.optInt(key2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return values;
    }

    public int getIntDataByKey(String key1,String key2,String key3){
        int values = 0;
        try {
            if(mJSONObject == null){
                mJSONObject = new JSONObject(data.toString());
            }
            JSONObject js1 = mJSONObject.optJSONObject(key1);
            JSONObject js2 = js1.optJSONObject(key2);
            values = js2.optInt(key3);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return values;
    }


    public int getIntDataByKey(String key){
        int values  = 0;
        try {
            if(mJSONObject == null){
                mJSONObject = new JSONObject(data.toString());
            }
            values = mJSONObject.optInt(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return values;
    }

    public double getDoubleDataByKey(String key1){
        double values = 0.0;
        try {
            if(mJSONObject == null){
                mJSONObject = new JSONObject(data.toString());
            }
            values = mJSONObject.optDouble(key1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return values;
    }
    public long getLongDataByKey(String key1){
        long values = 0;
        try {
            if(mJSONObject == null){
                mJSONObject = new JSONObject(data.toString());
            }
            values = mJSONObject.optLong(key1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return values;
    }
    public double getDoubleDataByKey(String key1,String key2){
        double values = 0.0;
        try {
            if(mJSONObject == null){
                mJSONObject = new JSONObject(data.toString());
            }
            JSONObject js1 = mJSONObject.optJSONObject(key1);
            values = js1.optDouble(key2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return values;
    }

    public double getDoubleDataByKey(String key1,String key2,String key3){
        double values = 0.00;
        try {
            if(mJSONObject == null){
                mJSONObject = new JSONObject(data.toString());
            }
            JSONObject js1 = mJSONObject.optJSONObject(key1);
            JSONObject js2 = js1.optJSONObject(key2);
            values = js2.optDouble(key3);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return values;
    }

    public double getDoubleDataByKey(String key1,String key2,String key3,String key4){
        double values = 0.00;
        try {
            if(mJSONObject == null){
                mJSONObject = new JSONObject(data.toString());
            }
            JSONObject js1 = mJSONObject.optJSONObject(key1);
            JSONObject js2 = js1.optJSONObject(key2);
            JSONObject js3 = js2.optJSONObject(key3);
            values = js3.optDouble(key4,0.00);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return values;
    }
}
