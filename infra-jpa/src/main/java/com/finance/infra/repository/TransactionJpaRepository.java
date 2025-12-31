package com.finance.infra.repository;

import com.finance.domain.model.e.TransactionType;
import com.finance.infra.entity.TransactionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionJpaRepository extends JpaRepository<TransactionEntity, Long> {
    List<TransactionEntity> findByAccountIdOrderByCreatedAtDesc(Long accountId);

    @Query("select sum(t.amount) from TransactionEntity t " +
            "where t.accountId = :accountId " +
            "and t.type = :type " +
            "and t.createdAt >= :today")
    BigDecimal findDailySumByType(@Param("accountId") Long accountId,
                                    @Param("today") LocalDateTime today,
                                    @Param("type") TransactionType type);

    // 특정 계좌의 거래 내역을 최신순으로 페이징 조회
    Page<TransactionEntity> findByAccountIdOrderByCreatedAtDesc(Long accountId, Pageable pageable);
}
