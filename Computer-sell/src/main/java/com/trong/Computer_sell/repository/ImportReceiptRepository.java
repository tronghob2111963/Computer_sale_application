package com.trong.Computer_sell.repository;

import com.trong.Computer_sell.model.ImportReceiptEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ImportReceiptRepository extends JpaRepository<ImportReceiptEntity, UUID> {
}
