package com.example.be_qltv.dto;

import java.util.List;
import java.util.Map;

public class DashboardStats {
    private long totalBooks;
    private long totalAuthors;
    private long totalCategories;
    private long totalPublishers;
    private long totalLoans;
    private long activeLoans;
    private long overdueBooks;
    private Map<String, Long> monthlyBorrowings;
    private List<BookDTO> mostBorrowedBooks;
    private List<AuthorDTO> mostPopularAuthors;

    // Getters and setters
    public long getTotalBooks() {
        return totalBooks;
    }

    public void setTotalBooks(long totalBooks) {
        this.totalBooks = totalBooks;
    }

    public long getTotalAuthors() {
        return totalAuthors;
    }

    public void setTotalAuthors(long totalAuthors) {
        this.totalAuthors = totalAuthors;
    }

    public long getTotalCategories() {
        return totalCategories;
    }

    public void setTotalCategories(long totalCategories) {
        this.totalCategories = totalCategories;
    }

    public long getTotalPublishers() {
        return totalPublishers;
    }

    public void setTotalPublishers(long totalPublishers) {
        this.totalPublishers = totalPublishers;
    }

    public long getTotalLoans() {
        return totalLoans;
    }

    public void setTotalLoans(long totalLoans) {
        this.totalLoans = totalLoans;
    }

    public long getActiveLoans() {
        return activeLoans;
    }

    public void setActiveLoans(long activeLoans) {
        this.activeLoans = activeLoans;
    }

    public long getOverdueBooks() {
        return overdueBooks;
    }

    public void setOverdueBooks(long overdueBooks) {
        this.overdueBooks = overdueBooks;
    }

    public Map<String, Long> getMonthlyBorrowings() {
        return monthlyBorrowings;
    }

    public void setMonthlyBorrowings(Map<String, Long> monthlyBorrowings) {
        this.monthlyBorrowings = monthlyBorrowings;
    }

    public List<BookDTO> getMostBorrowedBooks() {
        return mostBorrowedBooks;
    }

    public void setMostBorrowedBooks(List<BookDTO> mostBorrowedBooks) {
        this.mostBorrowedBooks = mostBorrowedBooks;
    }

    public List<AuthorDTO> getMostPopularAuthors() {
        return mostPopularAuthors;
    }

    public void setMostPopularAuthors(List<AuthorDTO> mostPopularAuthors) {
        this.mostPopularAuthors = mostPopularAuthors;
    }
}
