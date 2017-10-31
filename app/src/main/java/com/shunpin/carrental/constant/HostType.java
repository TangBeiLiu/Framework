package com.shunping.shifou.constant;

import com.shunping.shifou.R;
import com.shunping.shifou.app.MApplication;

/**
 * Created Date:  2017/7/17.
 * author: tsliu
 * email: liutangbei@gmail.com
 * baseUrl常量类,由这个枚举的长度创建retrofitManager单列
 */

public enum  HostType {
    //"http://shifou.co:3001/"
    /**
     *  MApplication.getContext().getResources().getString(R.string.base_url)
     */
    DEFAULT(0, MApplication.getContext().getResources().getString(R.string.base_url)),
    TEST(1,"http://192.168.1.14:5001/");

    private int mType;
    private String mHost;

    HostType(int type,String host) {
        this.mType = type;
        this.mHost = host;
    }


    public static String host(HostType hostType){
        return hostType.mHost;
    }

    public static int type(HostType hostType){
        return hostType.mType;
    }
}
