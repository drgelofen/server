package server.lib.model;

public class TokenModel {

    private Long auth_version;
    private Integer auth_type;
    private String auth_id;

    public TokenModel(String auth_id, Long auth_version, Integer auth_type) {
        this.auth_version = auth_version;
        this.auth_type = auth_type;
        this.auth_id = auth_id;
    }

    public Integer getAuth_type() {
        return auth_type;
    }

    public void setAuth_type(Integer auth_type) {
        this.auth_type = auth_type;
    }

    public Long getAuth_version() {
        return auth_version;
    }

    public void setAuth_version(Long auth_version) {
        this.auth_version = auth_version;
    }

    public String getAuth_id() {
        return auth_id;
    }

    public void setAuth_id(String auth_id) {
        this.auth_id = auth_id;
    }
}
