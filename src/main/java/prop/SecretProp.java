package prop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

public class SecretProp extends Properties {

    private static SecretProp secretProp;
    private Logger logger = LoggerFactory.getLogger(SecretProp.class);

    private String BASE_URL;
    private String BOT_TOKEN;

    private SecretProp(){

    }

    public static SecretProp getInstance() {
        if(secretProp == null){
            secretProp = new SecretProp();
            secretProp.initProp();
        }
        return secretProp;
    }

    private void initProp() {

        try {
            InputStream in = SecretProp.class.getClassLoader().getResourceAsStream("application.properties");
            if(in == null){
                logger.info("프로퍼티 로드 실패");
                throw new IOException();
            }
            secretProp.load(in);
            BASE_URL = (String) secretProp.get("BASE_URL");
            BOT_TOKEN = (String) secretProp.get("BOT_TOKEN");
        }catch (IOException e){

        }
    }


    public String getBASE_URL() {
        return BASE_URL;
    }

    public String getBOT_TOKEN() {
        return BOT_TOKEN;
    }
}
