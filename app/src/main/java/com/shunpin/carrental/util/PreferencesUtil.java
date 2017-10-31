package com.shunping.shifou.uitl;

import android.content.Context;
import android.content.SharedPreferences;


/**
 * Created Data:  2017/4/13.
 * author: tsliu
 * email: liutangbei@gmail.com
 */

public class PreferencesUtil {

    private static String PreferenceName = "Constant";
    private static Context mContext;

    public static void init(Context context) {
        mContext = context;
    }


    /**
     * 获取sp里面String类型的值
     * @param name 获取的key
     * @return String value
     */
    public static String getStringPreferences( String name) {
        SharedPreferences sp = mContext.getSharedPreferences(PreferenceName,
                Context.MODE_PRIVATE);

        // 获取数据
        return sp.getString(name, "");
    }

    /**
     * 将String信息存入Preferences
     *
     * @param key 存取的key
     * @param value 存取的值
     */
    public static void setPreferences(String key, String value) {
        SharedPreferences sp = mContext.getSharedPreferences(PreferenceName,
                Context.MODE_PRIVATE);

        // 存入数据
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * 清空sp
     */
    public static void clearData(){
        SharedPreferences sp = mContext.getSharedPreferences(PreferenceName,
                Context.MODE_PRIVATE);

         sp.edit().clear().apply();
    }

    /**
     * 将Boolean信息存入sp
     * @param key 存储的key
     * @param statu 存储的value
     */
    public static void setPreFerence(String key,Boolean statu){
        SharedPreferences sp = mContext.getSharedPreferences(PreferenceName,
                Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(key, statu);
        editor.apply();
    }

    /**
     * 获取sp Boolean类型的值
     * @param key 获取的key
     * @return boolean value
     */
    public static boolean getBooleanPreferences(String key) {

        SharedPreferences sp = mContext.getSharedPreferences(PreferenceName,
                Context.MODE_PRIVATE);

        return sp.getBoolean(key, true);
    }

    public static void setIntPreFerence(String key, int value) {
        SharedPreferences sp = mContext.getSharedPreferences(PreferenceName, Context.MODE_PRIVATE);

        SharedPreferences.Editor edit = sp.edit();
        edit.putInt(key, value);
        edit.apply();
    }

    public static int getIntPreferences(String key) {
        SharedPreferences sp = mContext.getSharedPreferences(PreferenceName, Context.MODE_PRIVATE);

        return sp.getInt(key, -1);
    }

}
