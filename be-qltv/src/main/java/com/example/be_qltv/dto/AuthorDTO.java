package com.example.be_qltv.dto;

import java.time.LocalDate;

public class AuthorDTO {
    private Long id;
    private String name;
    private String biography;
    private LocalDate birthDate;
    private String nationality;
    private Integer bookCount;
    
    // Constructors
    public AuthorDTO() {}
    
    public AuthorDTO(String name) {
        this.name = name;
    }
    
    public AuthorDTO(Long id, String name, String biography, LocalDate birthDate, String nationality) {
        this.id = id;
        this.name = name;
        this.biography = biography;
        this.birthDate = birthDate;
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

    public Integer getBookCount() {
        return bookCount;
    }

    public void setBookCount(Integer bookCount) {
        this.bookCount = bookCount;
    }
}
