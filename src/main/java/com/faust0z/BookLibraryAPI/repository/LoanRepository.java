package com.faust0z.BookLibraryAPI.repository;

import com.faust0z.BookLibraryAPI.entity.LoanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LoanRepository extends JpaRepository<LoanEntity, UUID> {

    List<LoanEntity> findByUserId(UUID userId);

    int countByUserIdAndReturnDateIsNull(UUID userId);
}