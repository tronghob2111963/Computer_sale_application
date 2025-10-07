package com.trong.Computer_sell.service.impl;

import com.trong.Computer_sell.DTO.request.BrandRequestDTO;
import com.trong.Computer_sell.DTO.response.BrandResponseDTO;
import com.trong.Computer_sell.DTO.response.PageResponse;
import com.trong.Computer_sell.model.BrandEntity;
import com.trong.Computer_sell.repository.BrandRepository;
import com.trong.Computer_sell.service.BrandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Service
@Slf4j(topic = "BRAND-SERVICE")
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;


    @Override
    public UUID saveBrand(BrandRequestDTO brandRequest) {
        log.info("Saving brand: {}", brandRequest.getName());
        BrandEntity brand = new BrandEntity();
        brand.setName(brandRequest.getName());
        brand.setCountry(brandRequest.getCountry());
        brand.setDescription(brandRequest.getDescription());
        brandRepository.save(brand);
        return brand.getId();
    }

    @Override
    public BrandResponseDTO getBrandById(UUID id) {
        log.info("Get brand by id");
        if(id == null){
            throw new RuntimeException("Brand id is null");
        }

        try{
           BrandEntity brand = brandRepository.findBrandById(id);
           return BrandResponseDTO.builder()
                   .name(brand.getName())
                   .country(brand.getCountry())
                   .description(brand.getDescription())
                   .build();

        }catch (Exception e){
            throw new RuntimeException("Brand not found");
        }

    }

    @Override
    public PageResponse getAllBrands(String keyword, int pageNo, int pageSize, String sortBy) {
        log.info("Find all brands with keyword: {}", keyword);
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

        Page<BrandEntity> brandPage;
        //search user by keyword
        if(StringUtils.hasLength(keyword)){
            keyword = "%" + keyword.toLowerCase() + "%";
            brandPage = brandRepository.searchBranchByKeyword(keyword ,pageable);

        }else{
            brandPage = brandRepository.findAll(pageable);
        }
        List<BrandResponseDTO> brandResponseDTOList = brandPage.getContent().stream().map(brand -> BrandResponseDTO.builder()
                .name(brand.getName())
                .country(brand.getCountry())
                .description(brand.getDescription())
                .build()).collect(Collectors.toList());

        return PageResponse.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalElements(brandPage.getTotalElements())
                .totalPages(brandPage.getTotalPages())
                .items(brandResponseDTOList)
                .build();
    }
}
