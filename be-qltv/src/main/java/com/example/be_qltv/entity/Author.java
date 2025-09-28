package com.example.be_qltv.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "authors")
public class Author {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 255)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String biography;
    
    @Column(name = "birth_date")
    private LocalDate birthDate;
    
    @Column(length = 100)
    private String nationality;
    
    @Column(name = "created_date")
    private LocalDateTime createdDate;
    
    @ManyToMany(mappedBy = "authors")
    private Set<Book> books = new HashSet<>();
    
    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
    }
    
    // Constructors
    public Author() {}
    
    public Author(String name) {
        this.name = name;
    }
    
    public Author(String name, String biography, String nationality) {
        this.name = name;
        this.biography = biography;
        this.nationality = nationality;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getBiography() {
        return biography;
    }
    
    public void setBiography(String biography) {
        this.biography = biography;
    }
    
    public LocalDate getBirthDate() {
        return birthDate;
    }
    
    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }
    
    public String getNationality() {
        return nationality;
    }
    
    public void setNationality(String nationality) {
        this.nationality = nationality;
    }
    
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
    
    public Set<Book> getBooks() {
        return books;
    }
    
    public void setBooks(Set<Book> books) {
        this.books = books;
    }
    
    // Helper methods
    public void addBook(Book book) {
        books.add(book);
        book.getAuthors().add(this);
    }
    
    public void removeBook(Book book) {
        books.remove(book);
        book.getAuthors().remove(this);
    }
}
