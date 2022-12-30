package http;

import dto.UserClickInfo;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

public interface RetrofitAPI {

    // 이모지 카운팅해야하는 메세지 id 목록 불러오기
    @GET("/api/message/list")
    Call<List<String>> getMessageIdList();

    // 이모지 카운팅해야하는 메세지 id를 서버로 전송(DB에 저장)
    @FormUrlEncoded
    @POST("/api/message/messageId")
    Call<Void> sendMessageId(@Field("messageId") String messageId);

    // 유저가 이모지 누른 내용 서버로 전송
    @POST("/api/message/userClickInfo")
    Call<Void> sendUserClickInfo(@Body UserClickInfo userClickInfo);

    // 유저가 이모지 취소했을 경우 서버에서 삭제
    @DELETE("/api/message/deleteUserClick/{messageId}/{userId}")
    Call<Void> deleteUserClickInfo(@Path("messageId") String messageId, @Path("userId") String userId);

}
