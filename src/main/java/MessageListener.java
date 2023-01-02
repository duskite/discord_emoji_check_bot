import dto.UserClickInfo;
import http.RetrofitUtil;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.HashSet;
import java.util.Set;

public class MessageListener extends ListenerAdapter {

    private final String fixedPrefix = "&Bubbly";
    private final String[] announceChId = {
            "996128946003382272",
            "1035137415813271552",
            "1056188382855823420",
    };

    private final String POINT_CH_ID = "1056761637211738112";
    private TextChannel pointCh;

    private Logger logger = LoggerFactory.getLogger(MessageListener.class);

    private Set<String> chkMessageIdSet = new HashSet<>();
    private RetrofitUtil retrofitUtil = RetrofitUtil.getInstance();


    /**
     * 메세지 처리 (기본적으로 모든 메세지를 수신하기 때문에 내부에서 필요한 메세지인지 판단함)
     * @param event
     */
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        MessageChannel messageChannel = event.getChannel();
        String chId = messageChannel.getId();

        if(!isAnnounceCh(chId)){
            return;
        }

        String rawMessage = event.getMessage().getContentRaw();
        if(!isCheckEmojiMessage(rawMessage)){
            return;
        }

        String messageId = event.getMessageId();
        chkMessageIdSet.add(messageId);
        sendMessageIdToServer(messageId);
    }

    /**
     * 공지 채널인지 아닌지 판단함
     * @param chId
     * @return 공지 채널일때 true
     */
    public boolean isAnnounceCh(String chId){
        for(String ch: announceChId){
            if(ch.equals(chId))
                return true;
        }
        return false;
    }

    /**
     * 이모지를 카운팅 체크해야 하는 메세지인지 판단
     * @param rawMessage
     * @return 카운팅해야 하는 메세지일 경우, true
     */
    public boolean isCheckEmojiMessage(String rawMessage){
        try{
            if(rawMessage.length() < 7){
                return false;
            }else {
                String prefix = rawMessage.substring(0, 7);
                if (prefix.equals(fixedPrefix)) {
                    logger.info("이모지 이벤트 체크해야하는 메세지");
                    return true;
                } else {
                    logger.info("이벤트 무관 메세지");
                    return false;
                }
            }
        }catch (StringIndexOutOfBoundsException e){
            // 글자 수가 작은 애들 예) ㅋㅋ
            // 공지 채널 기준으로 적용하면 여기 예외는 탈 일이 없음
            logger.info("메세지 글자 수가 작음(이벤트 무관)");
            return false;
        }
    }

    /**
     * 이모지 체크해야하는 메세지 id를 서버로 전송함
     * @param messageId
     */
    private void sendMessageIdToServer(String messageId){

        retrofitUtil.getRetrofitAPI().sendMessageId(messageId).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                if(response.isSuccessful()){
                    logger.info("메세지 id 전송 성공");
                }
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                logger.info("메세지 id 전송 실패!!!");
            }
        });

    }

    /**
     * 메세지에 리액션이 발생할 경우 동작 (기본적으로 모든 메세지 리액션에 동작하기 때문에
     *  이모지 카운팅해야하는 메세지인지 판단 필요)
     * @param event
     */
    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        String nowChId = event.getChannel().getId();
        if(!isAnnounceCh(nowChId)){
            return;
        }

        String messageId = event.getMessageId();
        if(!chkMessageIdSet.contains(messageId)){
            return;
        }

        //요구사항 변경으로 주석 처리, 이모지 종류와 상관없이 클릭 읽고서 중복은 서버에서 판단
//        MessageReaction.ReactionEmote reactionEmote = event.getReactionEmote();
//        String emoji = reactionEmote.getEmoji();
//        if(!checkEmoji(emoji)){
//            return;
//        }

        String userId = event.getMember().getId();
        String userTag = event.getUser().getAsTag();

        UserClickInfo userClickInfo = new UserClickInfo();
        userClickInfo.setUserId(userId);
        userClickInfo.setUserTag(userTag);
        userClickInfo.setMessageId(messageId);

//        포인트 자동적립은 우선 주석 처리. 안됨
        // 취합과 동시에 포인트 적립도 가능하도록 함
        // 이벤트가 있어야 텍스트 채널을 읽을 수 있는 거 같음
        // 어짜피 계속 같은 텍스트 채널이라 final로 고정하면 좋을듯
//        pointCh = event.getGuild().getTextChannelById(POINT_CH_ID);
        sendClickInfoToServer(userClickInfo);
    }

    /**
     * 유저가 이모지를 취소했을때 서버에 전송하여 db에서 삭제함
     * @param event
     */
    @Override
    public void onMessageReactionRemove(@NotNull MessageReactionRemoveEvent event) {
        String nowChId = event.getChannel().getId();
        if(!isAnnounceCh(nowChId)){
            return;
        }

        String messageId = event.getMessageId();
        if(!chkMessageIdSet.contains(messageId)){
            return;
        }

        try{
            String userId = event.getMember().getId();
            deleteClickInfo(messageId, userId);
        }catch (NullPointerException e){
            logger.info("유저가 이모지 취소했는데 읽어온 ID가 null 임");
        }
    }

    /**
     * 서버에 이모지 클릭 정보 삭제 요청
     * @param messageId
     * @param userId
     */
    public void deleteClickInfo(String messageId, String userId){
        retrofitUtil.getRetrofitAPI().deleteUserClickInfo(messageId, userId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if(response.isSuccessful()){
                    logger.info("유저 이모지 클릭 삭제");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                logger.info("유저 이모지 클릭 삭제 실패!!!");
            }
        });
    }

    /**
     * 이모지 클릭 정보 서버로 전송 (카운팅은 서버 db로 처리)
     * @param userClickInfo
     */
    private void sendClickInfoToServer(UserClickInfo userClickInfo){
        retrofitUtil.getRetrofitAPI().sendUserClickInfo(userClickInfo).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if(response.isSuccessful()){
                    if(Boolean.TRUE.equals(response.body())){
                        logger.info("유저 이모지 클릭 정보 전송 성공");

                        requestIncreasePoint(userClickInfo);
                    }else {
                        logger.info("동일한 게시글에 중복된 이모지 클릭임");
                    }
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                logger.info("유저 이모지 클릭 정보 전송 실패!!!");
            }
        });
    }

//    여기는 안돼서 우선 주석 처리함
    /**
     * 버블리 봇에 포인트 적립 요청
     * @param userClickInfo
     */
    private void requestIncreasePoint(UserClickInfo userClickInfo){
        String userTag = userClickInfo.getUserTag();
        String query = "/give-coins member:@" + userTag + " amount:1";

        pointCh.sendMessage(query).queue();
    }


    /**
     * 이벤트 이모지인지 판단
     * @param emoji
     * @return 이벤트 해당하는 이모지일 경우 true
     */
    private boolean checkEmoji(String emoji){
        if(emoji.equals("\uD83C\uDF89")){
            logger.info("이벤트 이모지에 해당함");
            return true;
        }else {
            logger.info("올바른 이모지가 아님");
            return false;
        }
    }

    /**
     * 카운팅해야하는 메세지들의 id 목록 세팅
     * @param messageIdSet
     */
    public void setMessageIdSet(Set<String> messageIdSet){
        this.chkMessageIdSet = messageIdSet;
    }

}
