package com.shunping.shifou.http;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * Created Data:  2017/4/18.
 * author: tsliu
 * email: liutangbei@gmail.com
 * 用来拦截服务器返回为空的错误 @see < https://github.com/square/retrofit/issues/1554></>
 *
 */

public class NullOnEmptyConverterFactory extends Converter.Factory {

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        final Converter<ResponseBody, ?> delegate = retrofit.nextResponseBodyConverter(this, type, annotations);
        return new Converter<ResponseBody, Object>() {
            @Override
            public Object convert(ResponseBody body) throws IOException {
                if (body.contentLength() == 0){

                    return null;
                }
                return delegate.convert(body);
            }
        };
    }
}
