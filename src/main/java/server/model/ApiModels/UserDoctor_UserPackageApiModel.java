package server.model.ApiModels;

public class UserDoctor_UserPackageApiModel {

    private long userDoctorId;

    private long userPackageId;

    public UserDoctor_UserPackageApiModel(long userDoctorId, long userPackageId) {
        this.userDoctorId = userDoctorId;
        this.userPackageId = userPackageId;
    }

    public long getUserDoctorId() {
        return userDoctorId;
    }

    public void setUserDoctorId(long userDoctorId) {
        this.userDoctorId = userDoctorId;
    }

    public long getUserPackageId() {
        return userPackageId;
    }

    public void setUserPackageId(long userPackageId) {
        this.userPackageId = userPackageId;
    }
}
