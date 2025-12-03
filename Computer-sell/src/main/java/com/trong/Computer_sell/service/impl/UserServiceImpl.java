package com.trong.Computer_sell.service.impl;

import com.trong.Computer_sell.DTO.request.user.UserCreationRequestDTO;
import com.trong.Computer_sell.DTO.request.user.UserPasswordRequest;
import com.trong.Computer_sell.DTO.request.user.UserRequestDTO;
import com.trong.Computer_sell.DTO.request.user.UserUpdateRequestDTO;
import com.trong.Computer_sell.DTO.response.common.PageResponse;
import com.trong.Computer_sell.DTO.response.User.UserResponseDTO;
import com.trong.Computer_sell.common.UserStatus;
import com.trong.Computer_sell.common.UserType;
import com.trong.Computer_sell.exception.ResourceNotFoundException;
import com.trong.Computer_sell.model.AddressEntity;
import com.trong.Computer_sell.model.UserEntity;
import com.trong.Computer_sell.repository.AddressRepository;
import com.trong.Computer_sell.repository.UserHasRoleRepository;
import com.trong.Computer_sell.repository.UserRepository;
import com.trong.Computer_sell.service.EmailService;
import com.trong.Computer_sell.service.UserRoleService;
import com.trong.Computer_sell.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
@Slf4j(topic = "USER-SERVICE")
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final UserRoleService userRoleService;

    private final UserHasRoleRepository userHasRoleRepository;
    @Override
    public PageResponse<?> findAll(String keyword, int pageNo, int pageSize, String sortBy, Integer roleId) {
        log.info("Find all users with keyword: {}, roleId: {}", keyword, roleId);
        int p = pageNo > 0 ? pageNo - 1 : 0;
        List<Sort.Order> sorts = new ArrayList<>();
        // Sort by ID
        if (StringUtils.hasLength(sortBy)) {
            Pattern pattern = Pattern.compile("(\\w+?)(:)(.*)");
            Matcher matcher = pattern.matcher(sortBy);
            if (matcher.find()) {
                if (matcher.group(3).equalsIgnoreCase("asc")) {
                    sorts.add(new Sort.Order(Sort.Direction.ASC, matcher.group(1)));
                } else {
                    sorts.add(new Sort.Order(Sort.Direction.DESC, matcher.group(1)));
                }
            }
        }

        //pagging
        Pageable pageable = PageRequest.of(p, pageSize, Sort.by(sorts));
        Page<UserEntity> usersPage;

        //search user by keyword and roleId
        if(roleId != null && roleId > 0) {
            if(StringUtils.hasLength(keyword)){
                keyword = "%" + keyword.toLowerCase() + "%";
                usersPage = userRepository.searchUserByKeywordAndRole(keyword, roleId, pageable);
            } else {
                usersPage = userRepository.findAllByRoleId(roleId, pageable);
            }
        } else if(StringUtils.hasLength(keyword)){
            keyword = "%" + keyword.toLowerCase() + "%";
            usersPage = userRepository.searchUserByKeyword(keyword ,pageable);
        } else {
            usersPage = userRepository.findAll(pageable);
        }

        return getUserPageResponse(pageNo, pageSize, usersPage);
    }

    //find user by id
    @Override
    public UserResponseDTO findById(UUID id) {
        log.info("Find user with user id: {}", id);
        UserEntity user = getUserById(id);
        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .gender(user.getGender().toString())
                .dateOfBirth(user.getDateOfBirth())
                .phoneNumber(user.getPhone())
                .email(user.getEmail())
                .build();
    }

    //find user detail by id (including addresses)
    @Override
    public com.trong.Computer_sell.DTO.response.User.UserDetailResponseDTO findDetailById(UUID id) {
        log.info("Find user detail with user id: {}", id);
        UserEntity user = getUserById(id);
        
        // Map addresses
        List<com.trong.Computer_sell.DTO.response.User.AddressResponseDTO> addressDTOs = user.getAddresses().stream()
                .map(address -> com.trong.Computer_sell.DTO.response.User.AddressResponseDTO.builder()
                        .id(address.getId())
                        .apartmentNumber(address.getApartmentNumber())
                        .streetNumber(address.getStreetNumber())
                        .ward(address.getWard())
                        .city(address.getCity())
                        .addressType(address.getAddressType())
                        .build())
                .collect(java.util.stream.Collectors.toList());
        
        // Get role names
        List<String> roleNames = user.getRoles().stream()
                .map(userHasRole -> userHasRole.getRole().getName())
                .collect(java.util.stream.Collectors.toList());
        
        return com.trong.Computer_sell.DTO.response.User.UserDetailResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .gender(user.getGender() != null ? user.getGender().toString() : null)
                .dateOfBirth(user.getDateOfBirth())
                .phoneNumber(user.getPhone())
                .email(user.getEmail())
                .userType(user.getUserType() != null ? user.getUserType().toString() : null)
                .status(user.getStatus() != null ? user.getStatus().toString() : null)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .addresses(addressDTOs)
                .roles(roleNames)
                .build();
    }

    @Override
    public UserResponseDTO findByUsername(String username) {
        return null;
    }

    @Override
    public UserResponseDTO findByEmail(String email) {
        return null;
    }

    //Create user
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UUID RegisterUser(UserCreationRequestDTO req) {
        log.info("Saving user: {}", req);

        //kiem tra username da ton tai hay chua
        if(userRepository.findByUsername(req.getUsername()) != null){
            throw new ResourceNotFoundException("Username already exists");
        }
        if(userRepository.findByEmail(req.getEmail()) != null){
            throw new ResourceNotFoundException("Email already exists");
        }
        if(userRepository.findByPhone(req.getPhoneNumber()) != null){
            throw new ResourceNotFoundException("Phone already exists");
        }

        UserEntity user = new UserEntity();
        String password = passwordEncoder.encode(req.getPassword());
        user.setUsername(req.getUsername());
        user.setPassword(password);
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setGender(req.getGender());
        user.setDateOfBirth(req.getDateOfBirth());
        user.setPhone(req.getPhoneNumber());
        user.setEmail(req.getEmail());
        user.setUserType(UserType.CUSTOMER);
        user.setStatus(UserStatus.ACTIVE);

        userRepository.save(user);

        log.info("Saved user: {}", user);

        if(user.getId() != null){
            List<AddressEntity> addresses = new ArrayList<>();
            req.getAddresses().forEach(address -> {
                AddressEntity addressEntity = new AddressEntity();
                addressEntity.setUser(user);
                addressEntity.setApartmentNumber(address.getApartmentNumber());
                addressEntity.setStreetNumber(address.getStreetNumber());
                addressEntity.setWard(address.getWard());
                addressEntity.setCity(address.getCity());
                addressEntity.setAddressType(address.getAddressType());
                addresses.add(addressEntity);
            });
            addressRepository.saveAll(addresses);
            log.info("Saving addresses: {}", addresses);

        }

        //gan quyen cho CUSTOMER
        userRoleService.assignRoleToUser(user, 4);

        //send Email Confirm
        try{
            emailService.emailVerification(req.getEmail(), req.getFirstName());
        }catch (Exception e){
            throw new RuntimeException();
        }

        return user.getId();
    }

    //Update user
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update( UserUpdateRequestDTO req) {
        log.info("Updating user: {}", req);
        //get user by id
        UserEntity user = getUserById(req.getId());
        if(user == null){
            throw new ResourceNotFoundException("User not found");
        }
        //set data
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setGender(req.getGender());
        user.setDateOfBirth(req.getDateOfBirth());
        user.setPhone(req.getPhoneNumber());
        user.setEmail(req.getEmail());
        userRepository.save(user);

        List<AddressEntity> addresses = new ArrayList<>();

        req.getAddresses().forEach(address -> {
            AddressEntity addressEntity = addressRepository.findByUserIdAndAddressType(user.getId(), address.getAddressType());
            if(addressEntity == null) {
                addressEntity = new AddressEntity();
            }
            addressEntity.setApartmentNumber(address.getApartmentNumber());
            addressEntity.setStreetNumber(address.getStreetNumber());
            addressEntity.setWard(address.getWard());
            addressEntity.setCity(address.getCity());
            addressEntity.setAddressType(address.getAddressType());
            addresses.add(addressEntity);
        });

        //save address
        addressRepository.saveAll(addresses);

    }


    //Inactive user
    @Override
    public void delete(UUID id) {
        log.info("Deleting user with user id: {}", id);
        UserEntity user = getUserById(id);
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
        log.info("Deleted user with user id: {}", user);
    }


    //Change password
    @Override
    public void changePassword(UserPasswordRequest req) {
        log.info("Change password with user id: {}", req);
        UserEntity user = getUserById(req.getId());
        if(req.getPassword().equals(req.getConfirmPassword())){
            user.setPassword(passwordEncoder.encode(req.getPassword()));
        }
        userRepository.save(user);
        log.info("Changed password with user id: {}", user);
    }

    @Override
    public UUID saveUser(UserRequestDTO req) {
        log.info("Saving user: {}", req);
        UserEntity user = new UserEntity();
        String password = passwordEncoder.encode(req.getPassword());
        user.setUsername(req.getUsername());
        user.setPassword(password);
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setGender(req.getGender());
        user.setDateOfBirth(req.getDateOfBirth());
        user.setPhone(req.getPhoneNumber());
        user.setEmail(req.getEmail());
        user.setUserType(req.getUserType());
        user.setStatus(UserStatus.ACTIVE);

        userRepository.save(user);

        log.info("Saved user: {}", user);

        if(user.getId() != null){
            List<AddressEntity> addresses = new ArrayList<>();
            req.getAddresses().forEach(address -> {
                AddressEntity addressEntity = new AddressEntity();
                addressEntity.setUser(user);
                addressEntity.setApartmentNumber(address.getApartmentNumber());
                addressEntity.setStreetNumber(address.getStreetNumber());
                addressEntity.setWard(address.getWard());
                addressEntity.setCity(address.getCity());
                addressEntity.setAddressType(address.getAddressType());
                addresses.add(addressEntity);
            });
            addressRepository.saveAll(addresses);
            log.info("Saving addresses: {}", addresses);

        }

        //gan quyen cho CUSTOMER
        UserType userType = req.getUserType();
        if(userType == UserType.ADMIN){
            userRoleService.assignRoleToUser(user, 2);
        }else if(userType == UserType.STAFF){
            userRoleService.assignRoleToUser(user, 3);
        }else if(userType == UserType.CUSTOMER){
            userRoleService.assignRoleToUser(user, 4);
        }else{
            userRoleService.assignRoleToUser(user, 1);
        }

        return user.getId();
    }

    @Override
    public PageResponse<?> findAllCustomerUser(String keyword, int pageNo, int pageSize, String sortBy) {
        return null;
    }


    //method to get user by id
    private UserEntity getUserById(UUID id){
        try{
            return userRepository.findUserById(id);
        }catch (Exception e){
            throw new ResourceNotFoundException("User not found");
        }
    }

    //method to get user page response
    private static PageResponse<Object> getUserPageResponse(int pageNo, int pageSize, Page<UserEntity> users) {
        List<UserResponseDTO> userResponse = users.stream().map(user -> UserResponseDTO.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .gender(user.getGender().toString())
                        .dateOfBirth(user.getDateOfBirth())
                        .phoneNumber(user.getPhone())
                        .email(user.getEmail())
                        .build())
                .toList();

        return PageResponse.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalElements(users.getTotalElements())
                .totalPages(users.getTotalPages())
                .items(userResponse)
                .build();
    }
}

