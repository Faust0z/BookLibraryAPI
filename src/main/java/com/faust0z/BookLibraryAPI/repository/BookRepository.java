package com.faust0z.BookLibraryAPI.repository;

import com.faust0z.BookLibraryAPI.entity.BookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
public interface BookRepository extends JpaRepository<BookEntity, UUID> {

    @Transactional
    @Modifying
    @Query("UPDATE BookEntity b SET b.copies = b.copies + 1 WHERE b.id = :bookId")
    void incrementCopies(UUID bookId);
}