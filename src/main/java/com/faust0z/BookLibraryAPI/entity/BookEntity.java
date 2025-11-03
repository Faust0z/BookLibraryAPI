package com.faust0z.BookLibraryAPI.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "books")
public class BookEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "author", nullable = false)
    private String author;

    @Column(name = "publication_date", nullable = false)
    private LocalDate publicationDate;

    @Column(name = "copies", nullable = false)
    private Integer copies;

    @OneToMany(mappedBy = "book", fetch = FetchType.LAZY)
    private List<LoanEntity> loans;
}