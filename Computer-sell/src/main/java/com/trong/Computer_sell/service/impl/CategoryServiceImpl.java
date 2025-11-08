package com.trong.Computer_sell.service.impl;

import com.trong.Computer_sell.DTO.request.product.BrandRequestDTO;
import com.trong.Computer_sell.DTO.response.category.CategoryResponseDTO;
import com.trong.Computer_sell.DTO.response.common.PageResponse;
import com.trong.Computer_sell.model.CategoryEntity;
import com.trong.Computer_sell.repository.CategoryRepository;
import com.trong.Computer_sell.service.CategoryService;
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
@Slf4j(topic = "CATEGORY_SERVICE")
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    @Override
    public UUID saveCategory(BrandRequestDTO brand) {
        log.info("Saving category: {}", brand.getName());
        CategoryEntity category = new CategoryEntity();
        category.setName(brand.getName());
        category.setDescription(brand.getDescription());
        categoryRepository.save(category);
        return category.getId();
    }

    @Override
    public CategoryResponseDTO getCategoryById(UUID id) {
        log.info("Get category by id: {}", id);
        CategoryEntity category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        return CategoryResponseDTO.builder()
                .name(category.getName())
                .build();
    }

    @Override
    public PageResponse getAllCategories(String keyword, int pageNo, int pageSize, String sortBy) {
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
        Page<CategoryEntity> categoriesPage;
        if(StringUtils.hasLength(keyword)){
            keyword = "%" + keyword.toLowerCase() + "%";
            categoriesPage = categoryRepository.searchCategoryByKeyword(keyword ,pageable);
        }else{
            categoriesPage = categoryRepository.findAll(pageable);
        }
        List<CategoryResponseDTO> categories = categoriesPage.stream().map(category -> {
            return CategoryResponseDTO.builder()
                    .id(category.getId())
                    .name(category.getName())
                    .build();
        }).collect(Collectors.toList());
        return PageResponse.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalElements(categoriesPage.getTotalElements())
                .totalPages(categoriesPage.getTotalPages())
                .items(categories)
                .build();
    }
}
