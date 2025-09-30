package com.trong.Computer_sell.service;

import com.trong.Computer_sell.exception.ResourceNotFoundException;
import com.trong.Computer_sell.model.Role;
import com.trong.Computer_sell.model.UserEntity;
import com.trong.Computer_sell.model.UserHasRole;
import com.trong.Computer_sell.repository.RoleRepository;
import com.trong.Computer_sell.repository.UserHasRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j(topic = "ROLE-SERVICE")
public class UserRoleService {

    private final RoleRepository roleRepository;
    private final UserHasRoleRepository userHasRoleRepository;

    @Transactional
    public void assignRoleToUser(UserEntity user, Integer role_Id){
        Role role = roleRepository.findById(role_Id).orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        UserHasRole userHasRole = new UserHasRole();
        userHasRole.setUser(user);
        userHasRole.setRole(role);
        userHasRoleRepository.save(userHasRole);
    }

}
