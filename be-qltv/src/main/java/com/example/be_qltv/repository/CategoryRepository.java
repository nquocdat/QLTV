package com.example.be_qltv.repository;

import com.example.be_qltv.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    Optional<Category> findByName(String name);
    
    List<Category> findByNameContainingIgnoreCase(String name);
    
    List<Category> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description);
    
    @Query("SELECT c FROM Category c ORDER BY c.name ASC")
    List<Category> findAllOrderByName();
    
    @Query("SELECT c FROM Category c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Category> searchByName(@Param("query") String query, Pageable pageable);
    
    @Query("SELECT c, COUNT(b) as bookCount " +
           "FROM Category c " +
           "LEFT JOIN c.books b " +
           "GROUP BY c " +
           "ORDER BY COUNT(b) DESC")
    Page<Object[]> findMostPopularCategories(Pageable pageable);
    
    @Query("SELECT c, COUNT(b) as bookCount, COUNT(l) as loanCount " +
           "FROM Category c " +
           "LEFT JOIN c.books b " +
           "LEFT JOIN b.loans l " +
           "WHERE l.returnDate >= :startDate AND l.returnDate <= :endDate " +
           "GROUP BY c " +
           "ORDER BY COUNT(l) DESC")
    Page<Object[]> findMostBorrowedCategories(
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate, 
            Pageable pageable);
    
    boolean existsByName(String name);
}
