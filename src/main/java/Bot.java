import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import prop.SecretProp;

import javax.security.auth.login.LoginException;
import java.util.Set;

public class Bot {

    /*
        java discord api로 토큰을 이용한 연결만 맺고, 나머지는 MessageListener에서 처리함
     */

    private static String token;
    private static JDA jda = null;
    private static Logger logger = LoggerFactory.getLogger(Bot.class);
    private static MessageListener messageListener;
    private static MessageIdLoader messageIdLoader;

    /**
     * 서버에서 이모지 카운팅해야하는 메세지id 정보 가져오고 그걸로 세팅함
     * @param args
     */
    public static void main(String[] args) {

        SecretProp secretProp = SecretProp.getInstance();
        token = (String) secretProp.getBOT_TOKEN();

        try{
            jda = JDABuilder.createDefault(token).build();
            messageListener = new MessageListener();
            messageIdLoader = new MessageIdLoader();
            messageIdLoader.setOnListener(new MessageIdLoader.DataLoadedListener() {
                @Override
                public void dataLoaded(Set<String> set) {
                    logger.info("관리해야하는 메세지 목록 로드 완료");
                    messageListener.setMessageIdSet(set);
                }
            });
            messageIdLoader.getMessageIdSet();
            jda.addEventListener(messageListener);

        }catch (LoginException e){
            logger.info("로그인 오류(토큰 관련)");
        }
    }

}
