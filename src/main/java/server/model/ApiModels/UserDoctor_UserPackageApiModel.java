package server.model.ApiModels;

public class UserDoctor_UserPackageApiModel {

    private Long userDoctorId;

    private Long userPackageId;

    private String settleAccount;

    private String settleName;


    private long settleType;

    public UserDoctor_UserPackageApiModel(Long userDoctorId, Long userPackageId, String settleAccount, String settleName, long settleType) {
        this.userDoctorId = userDoctorId;
        this.userPackageId = userPackageId;
        this.settleAccount = settleAccount;
        this.settleName = settleName;
        this.settleType = settleType;
    }

    public Long getUserDoctorId() {
        return userDoctorId;
    }

    public void setUserDoctorId(Long userDoctorId) {
        this.userDoctorId = userDoctorId;
    }

    public Long getUserPackageId() {
        return userPackageId;
    }

    public void setUserPackageId(Long userPackageId) {
        this.userPackageId = userPackageId;
    }

    public long getSettleType() {
        return settleType;
    }

    public void setSettleType(long settleType) {
        this.settleType = settleType;
    }

    public String getSettleAccount() {
        return settleAccount;
    }

    public void setSettleAccount(String settleAccount) {
        this.settleAccount = settleAccount;
    }

    public String getSettleName() {
        return settleName;
    }

    public void setSettleName(String settleName) {
        this.settleName = settleName;
    }
}
