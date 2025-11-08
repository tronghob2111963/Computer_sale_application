package com.trong.Computer_sell.repository;

import com.trong.Computer_sell.model.ImportReceiptDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ImportReceiptDetailRepository extends JpaRepository<ImportReceiptDetailEntity, UUID> {
}
