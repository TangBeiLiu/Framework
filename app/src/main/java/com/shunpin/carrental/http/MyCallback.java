package com.shunping.shifou.http;


import com.shunping.shifou.app.MApplication;
import com.shunping.shifou.login.LoginActivity;
import com.shunping.shifou.uitl.PreferencesUtil;
import com.socks.library.KLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created Data:  2017/4/17.
 * author: tsliu
 * email: liutangbei@gmail.com
 * 统一错误处理
 */

public abstract class MyCallback<T> implements Callback<T> {

    /**
     * 服务器响应成功
     *
     * @param response response
     */
    public abstract void OnSuccess(Response<T> response);

    /**
     * 响应失败
     * @param msg 失败信息
     */
    public abstract void onResponseError(String msg);

    @Override
    public void onResponse(Call<T> call, Response<T> response) {

        switch (response.code()) {
            case 200://服务器响应成功
                OnSuccess(response);
                break;
            case 204:
//                OnSuccess(null);
                break;
            case 401:
            case 403://需要重登录
                OnLogin();
                onResponseError("you need onlogin");
                break;
            case 404://找不到服务器资源
                onResponseError("找不到服务器资源");
                break;

            case 500://服务器出现了故障
            case 501:
                onResponseError("服务器出现了故障");
                break;
            case 400:

                String errorBody="";
                String errorMsg= "";

                try {
                    errorBody = response.errorBody().string();
                    KLog.d(errorBody);

                } catch (IOException e) {
                    e.printStackTrace();
                    errorMsg = "系统错误";
                }


                try {
                    JSONObject jErrorObj = new JSONObject(errorBody);
//                    errorMsg = jsonObject.getString(jsonObject.keys().next().);
                    errorMsg = (String) jErrorObj.getJSONArray(jErrorObj.keys().next()).get(0);

                } catch (JSONException e) {
                    errorMsg = errorBody;

                }

                onResponseError(errorMsg);
                break;
        }
    }

    private void OnLogin() {
        //服务器返回401错误的时候需要更新token
        LoginActivity.start(MApplication.getContext());
        //清空sp数据,
        PreferencesUtil.clearData();
    }


    @Override
    public void onFailure(Call<T> call, Throwable t) {
        onResponseError(t.getMessage());
    }
}
