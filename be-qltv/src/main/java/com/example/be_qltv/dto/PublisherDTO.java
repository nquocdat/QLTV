package com.example.be_qltv.dto;

import jakarta.validation.constraints.NotBlank;

public class PublisherDTO {
 private Long id;

 @NotBlank(message = "Publisher name is required")
 private String name;

 private String address;
 private String phone;
 private String email;
 private String website;
 private String country;
 private Integer establishedYear;
 private String description;

 public PublisherDTO() {
 }

 public PublisherDTO(Long id, String name, String address, String description) {
     this.id = id;
     this.name = name;
     this.address = address;
     this.description = description;
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

 public String getAddress() {
     return address;
 }

 public void setAddress(String address) {
     this.address = address;
 }

 public String getPhone() {
     return phone;
 }

 public void setPhone(String phone) {
     this.phone = phone;
 }

 public String getEmail() {
     return email;
 }

 public void setEmail(String email) {
     this.email = email;
 }

 public String getWebsite() {
     return website;
 }

 public void setWebsite(String website) {
     this.website = website;
 }

 public String getCountry() {
     return country;
 }

 public void setCountry(String country) {
     this.country = country;
 }

 public Integer getEstablishedYear() {
     return establishedYear;
 }

 public void setEstablishedYear(Integer establishedYear) {
     this.establishedYear = establishedYear;
 }

 public String getDescription() {
     return description;
 }

 public void setDescription(String description) {
     this.description = description;
 }

 @Override
 public String toString() {
     return "PublisherDTO{" +
             "id=" + id +
             ", name='" + name + '\'' +
             ", address='" + address + '\'' +
             ", phone='" + phone + '\'' +
             ", email='" + email + '\'' +
             ", website='" + website + '\'' +
             ", country='" + country + '\'' +
             ", establishedYear=" + establishedYear +
             ", description='" + description + '\'' +
             '}';
 }
}
