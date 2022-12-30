import http.RetrofitUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MessageIdLoader {

    private Logger logger = LoggerFactory.getLogger(MessageIdLoader.class);
    private RetrofitUtil retrofitUtil = RetrofitUtil.getInstance();
    private DataLoadedListener dataLoadedListener;

    /**
     * 서버에 GET요청으로 카운팅해야하는 메세지 id 목록 가져옴
     * @return set
     */
    public void getMessageIdSet(){

        Set<String> result = new HashSet<>();
        retrofitUtil.getRetrofitAPI().getMessageIdList().enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                if(response.isSuccessful()){
                    logger.info("메세지id 리스트 수신");

                    List<String> list = response.body();
                    for(String str: list){
                        logger.info("불러온 데이터: " + str);
                        result.add(str);
                    }

                    dataLoadedListener.dataLoaded(result);
                }
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                logger.info("getMessageId api 오류");
            }
        });
    }

    public void setOnListener(DataLoadedListener dataLoadedListener){
        this.dataLoadedListener = dataLoadedListener;
    }

    interface DataLoadedListener {
        void dataLoaded(Set<String> set);
    }
}
