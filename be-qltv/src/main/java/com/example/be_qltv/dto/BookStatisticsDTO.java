package com.example.be_qltv.dto;

import java.util.List;
import java.util.Map;

public class BookStatisticsDTO {
    private Long totalBooks;
    private Long availableBooks;
    private Long borrowedBooks;
    private Long overdueBooks;
    private Long lostBooks;
    private Long damagedBooks;
    private Long totalBorrows;
    private Long activePatrons;
    private Long totalAuthors;
    private Long totalCategories;
    private Long totalPublishers;

    // Top statistics
    private List<Map<String, Object>> mostBorrowedBooks;
    private List<Map<String, Object>> mostPopularAuthors;
    private List<Map<String, Object>> mostPopularCategories;
    private List<Map<String, Object>> mostActivePatrons;

    // Time-based statistics
    private Map<String, Long> borrowsByMonth;
    private Map<String, Long> returnsByMonth;
    private Map<String, Long> newBooksByMonth;

    // Getters and Setters
    public Long getTotalBooks() {
        return totalBooks;
    }

    public void setTotalBooks(Long totalBooks) {
        this.totalBooks = totalBooks;
    }

    public Long getAvailableBooks() {
        return availableBooks;
    }

    public void setAvailableBooks(Long availableBooks) {
        this.availableBooks = availableBooks;
    }

    public Long getBorrowedBooks() {
        return borrowedBooks;
    }

    public void setBorrowedBooks(Long borrowedBooks) {
        this.borrowedBooks = borrowedBooks;
    }

    public Long getOverdueBooks() {
        return overdueBooks;
    }

    public void setOverdueBooks(Long overdueBooks) {
        this.overdueBooks = overdueBooks;
    }

    public Long getLostBooks() {
        return lostBooks;
    }

    public void setLostBooks(Long lostBooks) {
        this.lostBooks = lostBooks;
    }

    public Long getDamagedBooks() {
        return damagedBooks;
    }

    public void setDamagedBooks(Long damagedBooks) {
        this.damagedBooks = damagedBooks;
    }

    public Long getTotalBorrows() {
        return totalBorrows;
    }

    public void setTotalBorrows(Long totalBorrows) {
        this.totalBorrows = totalBorrows;
    }

    public Long getActivePatrons() {
        return activePatrons;
    }

    public void setActivePatrons(Long activePatrons) {
        this.activePatrons = activePatrons;
    }

    public Long getTotalAuthors() {
        return totalAuthors;
    }

    public void setTotalAuthors(Long totalAuthors) {
        this.totalAuthors = totalAuthors;
    }

    public Long getTotalCategories() {
        return totalCategories;
    }

    public void setTotalCategories(Long totalCategories) {
        this.totalCategories = totalCategories;
    }

    public Long getTotalPublishers() {
        return totalPublishers;
    }

    public void setTotalPublishers(Long totalPublishers) {
        this.totalPublishers = totalPublishers;
    }

    public List<Map<String, Object>> getMostBorrowedBooks() {
        return mostBorrowedBooks;
    }

    public void setMostBorrowedBooks(List<Map<String, Object>> mostBorrowedBooks) {
        this.mostBorrowedBooks = mostBorrowedBooks;
    }

    public List<Map<String, Object>> getMostPopularAuthors() {
        return mostPopularAuthors;
    }

    public void setMostPopularAuthors(List<Map<String, Object>> mostPopularAuthors) {
        this.mostPopularAuthors = mostPopularAuthors;
    }

    public List<Map<String, Object>> getMostPopularCategories() {
        return mostPopularCategories;
    }

    public void setMostPopularCategories(List<Map<String, Object>> mostPopularCategories) {
        this.mostPopularCategories = mostPopularCategories;
    }

    public List<Map<String, Object>> getMostActivePatrons() {
        return mostActivePatrons;
    }

    public void setMostActivePatrons(List<Map<String, Object>> mostActivePatrons) {
        this.mostActivePatrons = mostActivePatrons;
    }

    public Map<String, Long> getBorrowsByMonth() {
        return borrowsByMonth;
    }

    public void setBorrowsByMonth(Map<String, Long> borrowsByMonth) {
        this.borrowsByMonth = borrowsByMonth;
    }

    public Map<String, Long> getReturnsByMonth() {
        return returnsByMonth;
    }

    public void setReturnsByMonth(Map<String, Long> returnsByMonth) {
        this.returnsByMonth = returnsByMonth;
    }

    public Map<String, Long> getNewBooksByMonth() {
        return newBooksByMonth;
    }

    public void setNewBooksByMonth(Map<String, Long> newBooksByMonth) {
        this.newBooksByMonth = newBooksByMonth;
    }
}
