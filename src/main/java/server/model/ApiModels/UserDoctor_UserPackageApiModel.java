package server.model.ApiModels;

import java.util.UUID;

public class UserDoctor_UserPackageApiModel {

    private Long userDoctorId;

    private Long userPackageId;

    public UserDoctor_UserPackageApiModel(Long userDoctorId, Long userPackageId) {
        this.userDoctorId = userDoctorId;
        this.userPackageId = userPackageId;
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
}
