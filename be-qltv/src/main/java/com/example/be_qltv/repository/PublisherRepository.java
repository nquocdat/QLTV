package com.example.be_qltv.repository;

import com.example.be_qltv.entity.Publisher;
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
public interface PublisherRepository extends JpaRepository<Publisher, Long> {
    
    Optional<Publisher> findByName(String name);

    @Query("SELECT p FROM Publisher p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Publisher> findByNameContainingIgnoreCase(@Param("name") String name);
    
    @Query("SELECT p FROM Publisher p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Publisher> searchByName(@Param("query") String query, Pageable pageable);
    
    List<Publisher> findByCountry(String country);
    
    @Query("SELECT p FROM Publisher p WHERE p.establishedYear BETWEEN :startYear AND :endYear")
    List<Publisher> findByEstablishedYearBetween(@Param("startYear") Integer startYear, 
                                                @Param("endYear") Integer endYear);
    
    @Query("SELECT p FROM Publisher p WHERE SIZE(p.books) > 0 ORDER BY SIZE(p.books) DESC")
    List<Publisher> findPublishersWithBooks();
    
    @Query("SELECT p FROM Publisher p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(p.country) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Publisher> searchPublishers(@Param("query") String query);
    
    @Query("SELECT p, COUNT(b) as bookCount " +
           "FROM Publisher p " +
           "LEFT JOIN p.books b " +
           "GROUP BY p " +
           "ORDER BY COUNT(b) DESC")
    Page<Object[]> findMostProductivePublishers(Pageable pageable);
    
    @Query("SELECT p, COUNT(b) as bookCount, COUNT(l) as loanCount " +
           "FROM Publisher p " +
           "LEFT JOIN p.books b " +
           "LEFT JOIN b.loans l " +
           "WHERE l.returnDate >= :startDate AND l.returnDate <= :endDate " +
           "GROUP BY p " +
           "ORDER BY COUNT(l) DESC")
    Page<Object[]> findMostBorrowedPublishers(
            @Param("startDate") String startDate, 
            @Param("endDate") String endDate, 
            Pageable pageable);
    
    @Query(value = "SELECT p.country, COUNT(p.id) as publisher_count, COUNT(b.id) as book_count " +
           "FROM publishers p " +
           "LEFT JOIN books b ON p.id = b.publisher_id " +
           "GROUP BY p.country " +
           "ORDER BY publisher_count DESC", nativeQuery = true)
    List<Map<String, Object>> getPublisherStatisticsByCountry();
}
