package http;

import okhttp3.OkHttpClient;
import prop.SecretProp;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitUtil {

    private static RetrofitUtil retrofitUtil;
    private Retrofit retrofit;
    private RetrofitAPI retrofitAPI;
    private OkHttpClient.Builder httpBuilder;
    private OkHttpClient httpClient;

    private String BASE_URL;

    private RetrofitUtil(){

        SecretProp secretProp = SecretProp.getInstance();
        BASE_URL = (String) secretProp.getBASE_URL();

        this.httpBuilder = new OkHttpClient.Builder();
        this.httpClient = httpBuilder.build();
        this.retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        this.retrofitAPI = this.retrofit.create(RetrofitAPI.class);
    }

    public static RetrofitUtil getInstance() {
        if(retrofitUtil == null){
            retrofitUtil = new RetrofitUtil();
        }
        return retrofitUtil;
    }

    public RetrofitAPI getRetrofitAPI(){
        return retrofitAPI;
    }


}
