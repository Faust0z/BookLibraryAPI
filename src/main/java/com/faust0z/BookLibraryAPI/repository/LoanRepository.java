package com.faust0z.BookLibraryAPI.repository;

import com.faust0z.BookLibraryAPI.entity.LoanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LoanRepository extends JpaRepository<LoanEntity, UUID> {

    @Query("SELECT l FROM LoanEntity l JOIN FETCH l.user JOIN FETCH l.book WHERE l.user.id = :userId")
    List<LoanEntity> findByUserIdWithUserAndBook(UUID userId);

    @Query("SELECT l FROM LoanEntity l JOIN FETCH l.user JOIN FETCH l.book")
    List<LoanEntity> findAllWithUserAndBook();

    int countByUserIdAndReturnDateIsNull(UUID userId);
}