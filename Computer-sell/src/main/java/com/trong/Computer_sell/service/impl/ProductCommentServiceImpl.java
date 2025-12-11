package com.trong.Computer_sell.service.impl;

import com.trong.Computer_sell.DTO.request.comment.CreateCommentRequest;
import com.trong.Computer_sell.DTO.response.comment.ProductCommentResponse;
import com.trong.Computer_sell.DTO.response.common.PageResponse;
import com.trong.Computer_sell.common.CommentStatus;
import com.trong.Computer_sell.model.ProductCommentEntity;
import com.trong.Computer_sell.model.ProductEntity;
import com.trong.Computer_sell.model.UserEntity;
import com.trong.Computer_sell.repository.ProductCommentRepository;
import com.trong.Computer_sell.repository.ProductRepository;
import com.trong.Computer_sell.repository.UserRepository;
import com.trong.Computer_sell.service.NotificationService;
import com.trong.Computer_sell.service.ProductCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductCommentServiceImpl implements ProductCommentService {

    private final ProductCommentRepository commentRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public ProductCommentResponse addComment(CreateCommentRequest request, String username) {
        if (request.getProductId() == null) {
            throw new IllegalArgumentException("productId is required");
        }
        if (!StringUtils.hasText(request.getContent())) {
            throw new IllegalArgumentException("content is required");
        }

        ProductEntity product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        UserEntity user = userRepository.findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        ProductCommentEntity entity = new ProductCommentEntity();
        entity.setProduct(product);
        entity.setUser(user);
        entity.setContent(request.getContent().trim());
        entity.setStatus(CommentStatus.APPROVED);

        if (request.getParentId() != null) {
            ProductCommentEntity parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("Parent comment not found"));
            if (parent.getProduct() != null && !Objects.equals(parent.getProduct().getId(), product.getId())) {
                throw new IllegalArgumentException("Parent comment does not belong to product");
            }
            entity.setParent(parent);

            // Gửi thông báo cho người viết comment gốc khi có reply
            if (parent.getUser() != null && !parent.getUser().getId().equals(user.getId())) {
                String replierName = user.getFirstName() + " " + user.getLastName();
                notificationService.notifyCommentReplied(
                    parent.getUser().getId(),
                    parent.getId(),
                    product.getName(),
                    replierName.trim().isEmpty() ? user.getUsername() : replierName
                );
            }
        } else {
            // Comment mới (không phải reply) - thông báo cho Admin
            String userName = user.getFirstName() + " " + user.getLastName();
            notificationService.notifyNewComment(
                entity.getId(),
                product.getName(),
                userName.trim().isEmpty() ? user.getUsername() : userName
            );
        }

        ProductCommentEntity saved = commentRepository.save(entity);
        return ProductCommentResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductCommentResponse> getApprovedComments(UUID productId) {
        List<ProductCommentEntity> comments = commentRepository
                .findByProduct_IdAndStatusOrderByCreatedAtAsc(productId, CommentStatus.APPROVED);

        Map<UUID, ProductCommentResponse> map = new LinkedHashMap<>();
        List<ProductCommentResponse> roots = new ArrayList<>();

        for (ProductCommentEntity entity : comments) {
            ProductCommentResponse response = ProductCommentResponse.fromEntity(entity, new ArrayList<>());
            map.put(entity.getId(), response);

            UUID parentId = entity.getParent() != null ? entity.getParent().getId() : null;
            if (parentId == null) {
                roots.add(response);
            } else {
                ProductCommentResponse parent = map.get(parentId);
                if (parent != null) {
                    parent.getReplies().add(response);
                } else {
                    roots.add(response);
                }
            }
        }

        // Sort replies by createdAt ascending for readability
        roots.forEach(this::sortReplies);
        return roots;
    }

    @Override
    public PageResponse<List<ProductCommentResponse>> searchComments(
            String keyword,
            CommentStatus status,
            UUID productId,
            int pageNo,
            int pageSize,
            String sortBy
    ) {
        Pageable pageable = PageRequest.of(
                pageNo > 0 ? pageNo - 1 : 0,
                pageSize,
                Sort.by(Sort.Direction.DESC, StringUtils.hasText(sortBy) ? sortBy : "createdAt")
        );

        String keywordPattern = (!StringUtils.hasText(keyword)) ? null : "%" + keyword.trim().toLowerCase() + "%";
        Page<ProductCommentEntity> page = commentRepository.search(productId, status, keywordPattern, pageable);

        List<ProductCommentResponse> items = page.getContent().stream()
                .map(ProductCommentResponse::fromEntity)
                .collect(Collectors.toList());

        return new PageResponse<>(
                items,
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    @Override
    @Transactional
    public ProductCommentResponse updateStatus(UUID commentId, CommentStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status is required");
        }
        ProductCommentEntity entity = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        entity.setStatus(status);
        return ProductCommentResponse.fromEntity(commentRepository.save(entity));
    }

    private void sortReplies(ProductCommentResponse response) {
        if (response.getReplies() == null || response.getReplies().isEmpty()) {
            return;
        }
        response.getReplies().sort(Comparator.comparing(ProductCommentResponse::getCreatedAt));
        response.getReplies().forEach(this::sortReplies);
    }
}
