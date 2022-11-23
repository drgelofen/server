package server;

import com.google.gson.Gson;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import server.lib.utils.SchemaUtil;

import java.util.Calendar;

@SpringBootApplication
@EnableScheduling
public class Application {

    public static final Gson GSON = new Gson();

    public static final String FCM_KEY = "AAAAs3IidDY:APA91bG4BsCrv2mMyd2X-78av0x_XuSeLWxNRgciPJ8bpo76gOPIZJ42EhuIu9QaeXMsbecQzff7d8IBZuNlhJZNXUO1UcYM2jhJ3SK3NxmvkxR2P4Mz7hw-egisYCZFKTxWq89A2A52";
    public static final String OTP_SECRET = "thisismytokenforqewrth23werfg";
    public static final String OTP_KEY = "e8a42ccad56b67ddf9a0288b";

    public static final String SCHEMA_NAME = "gelofen";
    public static final String SCHEMA_CRED = "102017";
    public static final String SCHEMA_USER = "postgres";

    public static final boolean PRODUCTION_MODE = true;
    public static final boolean INIT_DATABASE = false;

    public static final boolean ENABLE_SSL = true;
    public static final boolean ENABLE_OTP = true;
    public static final boolean ENABLE_FCM = true;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        long now = Calendar.getInstance().getTimeInMillis();
        SchemaUtil.initDatabase();
        long then = Calendar.getInstance().getTimeInMillis();
        System.out.println("Server is Running: " + ((then - now)));
    }
//    {
//            "limit_by": 2000,
//            "offset_by": 0,
//            "order_by": "create_at",
//            "sort_by": "des",
//            "date_to": "2030-01-01",
//            "date_from": "2010-01-01",
//            "time_to": 100000000000,
//            "time_from": 1000,
//            "search_by": null,
//            "parent_by": null,
//            "filter_by": null
//    }
}
