package server.lib.model;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;
import org.springframework.http.MediaType;
import server.lib.utils.TokenUtil;
import server.model.Admin;
import server.model.Doctor;
import server.model.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.util.UUID;

import static server.Application.GSON;

public class Request extends HttpServletRequestWrapper {

    public static final String HACK_MESSAGE = "ورودی ارسال شده شامل کاراکترهای نامعتبر میباشد.";

    private String authorization;
    private String userAgent;
    private String language;
    private String route;
    private String body;

    private Doctor auth_doctor;
    private Admin auth_admin;
    private User auth_user;

    private TokenUtil.TokenType auth_type;
    private Long auth_version;
    private UUID auth;

    private Boolean block;

    public Request(HttpServletRequest request) {
        super(request);
        try {
            route = String.valueOf(request.getRequestURL());
            authorization = request.getHeader(TokenUtil.AUTHORIZATION_HEADER);
            language = request.getHeader(TokenUtil.ACCEPT_LANGUAGE);
            userAgent = request.getHeader(TokenUtil.USER_AGENT);
            try {
                String contentType = request.getContentType();
                if (contentType == null || !contentType.contains(MediaType.MULTIPART_FORM_DATA_VALUE)) {
                    BufferedReader reader = request.getReader();
                    if (reader != null) {
                        body = IOUtils.toString(reader);
                    }
                }
            } catch (Throwable ignored) {
            }
            TokenUtil.secure(this);
        } catch (Throwable ignored) {
        }
    }

    public void setAuth(UUID auth) {
        this.auth = auth;
    }

    public Long getAuth_version() {
        return auth_version;
    }

    public void setAuth_version(Long auth_version) {
        this.auth_version = auth_version;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getAuthorization() {
        return authorization;
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public TokenUtil.TokenType getAuth_type() {
        return auth_type;
    }

    public void setAuth_type(TokenUtil.TokenType auth_type) {
        this.auth_type = auth_type;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public Admin getAuth_admin() {
        return auth_admin;
    }

    public void setAuth_admin(Admin auth_admin) {
        this.auth_admin = auth_admin;
    }

    public User getAuth_user() {
        return auth_user;
    }

    public UUID getAuth() {
        return auth;
    }

    public void setAuth_user(User auth_user) {
        this.auth_user = auth_user;
    }

    public void setBlock(Boolean block) {
        this.block = block;
    }

    public Doctor getAuth_doctor() {
        return auth_doctor;
    }

    public void setAuth_doctor(Doctor auth_doctor) {
        this.auth_doctor = auth_doctor;
    }

    public void setToken(TokenModel tokenModel) {
        try {
            if (tokenModel != null && tokenModel.getAuth_version() != null && tokenModel.getAuth_id() != null) {
                this.auth = UUID.fromString(tokenModel.getAuth_id());
                this.auth_version = tokenModel.getAuth_version();
                switch (tokenModel.getAuth_type()) {
                    case 0:
                        this.auth_type = TokenUtil.TokenType.ADMIN;
                        break;
                    case 1:
                        this.auth_type = TokenUtil.TokenType.USER;
                        break;
                    case 2:
                        this.auth_type = TokenUtil.TokenType.DOCTOR;
                        break;
                }
            }
        } catch (Throwable ignored) {
        }
    }

    public boolean isHack() {
        return block != null && block;
    }

    public String redirect(String replacement) {
        return route.replace(getServletPath(), replacement);
    }

    public JsonObject toJsonObject() {
        return JsonParser.parseString(body).getAsJsonObject();
    }

    public FilterModel toFilter() {
        FilterModel filterModel = GSON.fromJson(body, FilterModel.class);
        if (filterModel == null) {
            filterModel = new FilterModel();
        }
        return filterModel;
    }
}
