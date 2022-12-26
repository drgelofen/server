package server.model.ApiModels;

import java.util.UUID;

public class UserDoctor_UserPackageApiModel {

    private UUID userDoctorId;

    private UUID userPackageId;

    public UserDoctor_UserPackageApiModel(String userDoctorId, String userPackageId) {
        this.userDoctorId = UUID.fromString(userDoctorId);
        this.userPackageId = UUID.fromString(userPackageId);
    }

    public UUID getUserDoctorId() {
        return userDoctorId;
    }

    public void setUserDoctorId(UUID userDoctorId) {
        this.userDoctorId = userDoctorId;
    }

    public UUID getUserPackageId() {
        return userPackageId;
    }

    public void setUserPackageId(UUID userPackageId) {
        this.userPackageId = userPackageId;
    }
}
