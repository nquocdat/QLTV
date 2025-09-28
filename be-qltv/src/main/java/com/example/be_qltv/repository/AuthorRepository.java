package com.example.be_qltv.repository;

import com.example.be_qltv.entity.Author;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {
    
    Optional<Author> findByName(String name);
    
    List<Author> findByNameContainingIgnoreCase(String name);
    
    Page<Author> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    List<Author> findByNationality(String nationality);
    
    @Query("SELECT a FROM Author a ORDER BY a.name ASC")
    List<Author> findAllOrderByName();
    
    @Query("SELECT a FROM Author a WHERE a.name LIKE %:keyword% OR a.nationality LIKE %:keyword%")
    Page<Author> searchAuthors(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT DISTINCT a.nationality FROM Author a WHERE a.nationality IS NOT NULL ORDER BY a.nationality")
    List<String> findDistinctNationalities();
    
    @Query("SELECT a, COUNT(DISTINCT b) as bookCount " +
           "FROM Author a " +
           "LEFT JOIN a.books b " +
           "GROUP BY a " +
           "ORDER BY COUNT(DISTINCT b) DESC")
    Page<Object[]> findMostProductiveAuthors(Pageable pageable);
    
    @Query("SELECT a, COUNT(DISTINCT b) as bookCount, COUNT(l) as loanCount " +
           "FROM Author a " +
           "LEFT JOIN a.books b " +
           "LEFT JOIN b.loans l " +
           "WHERE l.returnDate >= :startDate AND l.returnDate <= :endDate " +
           "GROUP BY a " +
           "ORDER BY COUNT(l) DESC")
    Page<Object[]> findMostBorrowedAuthors(
            @Param("startDate") String startDate, 
            @Param("endDate") String endDate, 
            Pageable pageable);
    
    @Query(value = "SELECT a.nationality, COUNT(DISTINCT a.id) as author_count, " +
           "COUNT(DISTINCT b.id) as book_count " +
           "FROM authors a " +
           "LEFT JOIN book_authors ba ON a.id = ba.author_id " +
           "LEFT JOIN books b ON ba.book_id = b.id " +
           "WHERE a.nationality IS NOT NULL " +
           "GROUP BY a.nationality " +
           "ORDER BY author_count DESC", nativeQuery = true)
    List<Map<String, Object>> getAuthorStatisticsByNationality();
    
    boolean existsByName(String name);

    @Query("SELECT a " +
           "FROM Author a " +
           "JOIN a.books b " +
           "JOIN b.loans l " +
           "GROUP BY a " +
           "ORDER BY COUNT(l) DESC")
    Page<Author> findMostPopularAuthorsWithBorrowCount(Pageable pageable);
}
