package com.trong.Computer_sell.service;

import com.trong.Computer_sell.DTO.response.User.UserBuildResponse;

import java.util.List;
import java.util.UUID;

public interface UserBuildService {

    UserBuildResponse createBuild(UUID userId, String name);
    UUID addProductToBuild(UUID buildId, UUID productId, int quantity);
    UserBuildResponse removeProductFromBuild(UUID buildId, UUID productId);
    UserBuildResponse updateBuild(UUID buildId, String name);
    void deleteBuild(UUID buildId);
    List<UserBuildResponse> getUserBuilds(UUID userId);
    UserBuildResponse getBuild(UUID buildId);
}
