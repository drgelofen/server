package server.lib.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.security.crypto.bcrypt.BCrypt;
import server.Application;
import server.lib.model.Request;
import server.lib.model.TokenModel;

import java.security.MessageDigest;
import java.util.Calendar;
import java.util.UUID;

public class TokenUtil {

    private static final String SECRET = "Just a small-town girl, Livin' in a lonely world, She took the midnight train goin' anywhere";
    private static final String SECRET_BYCRYPT = "$2a$04$y1AzgLx0b4PE9TDt1BV2Ru";

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String ACCEPT_LANGUAGE = "Accept-Language";
    public static final String USER_AGENT = "User-Agent";
    public static final String CONNECTION = "Connection";
    public static final String KEEP_ALIVE = "keep-alive";

    public enum TokenType {
        ADMIN, USER, DOCTOR
    }

    public static String generateToken(UUID id, Long version, TokenType type) {
        return generateToken(id, version, type, 0);
    }

    public static String generateToken(UUID id, Long version, TokenType type, long expireAt) {
        JWTCreator.Builder builder = JWT.create().withIssuer("auth0")
                .withSubject(Application.GSON.toJson(new TokenModel(id.toString(), version, type.ordinal())));
        if (expireAt > 0) {
            Calendar instance = Calendar.getInstance();
            instance.setTimeInMillis(instance.getTimeInMillis() + expireAt);
            builder.withExpiresAt(instance.getTime());
        }
        return builder.sign(Algorithm.HMAC256(SECRET));
    }

    public static TokenModel verifyToken(Request request) {
        try {
            if (request != null && request.getAuthorization() != null && request.getAuthorization().startsWith("Bearer")) {
                return verifyToken(request.getAuthorization().substring(7));
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    public static TokenModel verifyToken(String request) {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(SECRET)).withIssuer("auth0").build();
            DecodedJWT decodedJWT = verifier.verify(request);
            if (decodedJWT != null) {
                return Application.GSON.fromJson(decodedJWT.getSubject(), TokenModel.class);
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    public static String toMD5(String pass) throws Throwable {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] array = md.digest(pass.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte anArray : array) {
            sb.append(Integer.toHexString((anArray & 0xFF) | 0x100).substring(1, 3));
        }
        return sb.toString().toLowerCase();
    }

    public static boolean isMD5(String input) {
        if (StringUtil.isEmpty(input)) {
            return false;
        }
        return input.matches("^[a-fA-F0-9]{32}$");
    }

    public static String bycrypt(String password) {
        if (StringUtil.isEmpty(password)) {
            return null;
        }
        return BCrypt.hashpw(password.toLowerCase(), SECRET_BYCRYPT);
    }

    public static boolean isMatchBycrypt(String dbPass, String reqPass) {
        return dbPass.equalsIgnoreCase(bycrypt(reqPass));
    }

    public static void secure(Request request) throws Throwable {
        if (isRisky(request.getBody())) {
            request.setBlock(true);
            return;
        }
        if (request.getParameterMap() != null) {
            for (String key : request.getParameterMap().keySet()) {
                String[] value = request.getParameterMap().get(key);
                if (value != null) {
                    for (String data : value) {
                        if (isRisky(data)) {
                            request.setBlock(true);
                            return;
                        }
                    }
                }
            }
        }
    }

    private static boolean isRisky(String data) {
        if (!StringUtil.isEmpty(data)) {
            data = data.toLowerCase();
            return data.contains("--")
                    || data.contains("psql")
                    || data.contains("database")
                    || data.contains("pg_sleep")
                    || data.contains("exec(");
        }
        return false;
    }

    public static String getIP(Request request) {
        final String[] HEADERS_TO_TRY = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "HTTP_VIA",
                "REMOTE_ADDR"};
        for (String header : HEADERS_TO_TRY) {
            String ip = request.getHeader(header);
            if (ip != null && ip.length() != 0 && !ip.equalsIgnoreCase("unknown")) {
                return ip;
            }
        }
        return request.getRemoteAddr();
    }
}
