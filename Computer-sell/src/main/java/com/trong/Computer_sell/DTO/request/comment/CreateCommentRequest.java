package com.trong.Computer_sell.DTO.request.comment;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateCommentRequest {
    private UUID productId;
    private UUID parentId;
    private String content;
}
