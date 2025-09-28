package com.example.be_qltv.config;

import com.example.be_qltv.entity.Author;
import com.example.be_qltv.entity.Book;        
import com.example.be_qltv.entity.Loan;
import com.example.be_qltv.entity.Patron;
import com.example.be_qltv.entity.Publisher;
import com.example.be_qltv.repository.AuthorRepository;
import com.example.be_qltv.repository.BookRepository;
import com.example.be_qltv.repository.LoanRepository;
import com.example.be_qltv.repository.PatronRepository;
import com.example.be_qltv.repository.PublisherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private PatronRepository patronRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private PublisherRepository publisherRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Chỉ tạo dữ liệu nếu database trống
        if (patronRepository.count() == 0) {
            seedData();
        }
    }

    private void seedData() {
        // Tạo dữ liệu Patrons
        createPatrons();
        
        // Tạo dữ liệu Authors và Publishers
        createAuthorsAndPublishers();
        
        // Tạo dữ liệu Books
        createBooks();
        
        // Tạo dữ liệu Loans
        createLoans();
    }

    private void createPatrons() {
        List<Patron> patrons = List.of(
            createPatron("Admin User", "admin@library.com", "0123456789", "123 Admin Street", Patron.Role.ADMIN),
            createPatron("Librarian One", "librarian@library.com", "0123456788", "456 Library Ave", Patron.Role.LIBRARIAN),
            createPatron("John Doe", "john@example.com", "0123456787", "789 User Road", Patron.Role.USER),
            createPatron("Jane Smith", "jane@example.com", "0123456786", "321 Reader Lane", Patron.Role.USER),
            createPatron("Bob Wilson", "bob@example.com", "0123456785", "654 Student Street", Patron.Role.USER)
        );
        
        patronRepository.saveAll(patrons);
        System.out.println("Created " + patrons.size() + " patrons");
    }

    private Patron createPatron(String name, String email, String phone, String address, Patron.Role role) {
        Patron patron = new Patron();
        patron.setName(name);
        patron.setEmail(email);
        patron.setPhoneNumber(phone);
        patron.setAddress(address);
        patron.setRole(role);
        patron.setPassword(passwordEncoder.encode("secret")); // Mật khẩu mặc định: secret
        return patron;
    }

    private void createBooks() {
        List<Book> books = List.of(
            createBook("Java: The Complete Reference", "Herbert Schildt", 2020, "Programming", 5, 3),
            createBook("Spring Boot in Action", "Craig Walls", 2019, "Programming", 3, 2),
            createBook("Clean Code", "Robert C. Martin", 2008, "Software Development", 4, 4),
            createBook("Design Patterns", "Gang of Four", 1994, "Software Architecture", 2, 1),
            createBook("Database System Concepts", "Abraham Silberschatz", 2019, "Database", 3, 3),
            createBook("Algorithms", "Robert Sedgewick", 2011, "Computer Science", 4, 2),
            createBook("The Pragmatic Programmer", "David Thomas", 2019, "Software Development", 3, 3),
            createBook("Head First Design Patterns", "Eric Freeman", 2004, "Software Architecture", 2, 2),
            createBook("Effective Java", "Joshua Bloch", 2017, "Programming", 3, 1),
            createBook("Introduction to Algorithms", "Thomas H. Cormen", 2009, "Computer Science", 5, 4)
        );
        
        bookRepository.saveAll(books);
        System.out.println("Created " + books.size() + " books");
    }

    private Book createBook(String title, String authorName, Integer publicationYear, String genre, 
                           int totalCopies, int availableCopies) {
        Book book = new Book();
        book.setTitle(title);
        
        // Find and set author
        Author authorEntity = authorRepository.findByName(authorName).orElseGet(() -> {
            Author newAuthor = new Author();
            newAuthor.setName(authorName);
            return authorRepository.save(newAuthor);
        });
        book.setAuthors(Set.of(authorEntity));
        
        // Use publishedDate instead of publicationYear
        if (publicationYear != null) {
            book.setPublishedDate(java.time.LocalDate.of(publicationYear, 1, 1));
        }
        book.setGenre(genre);
        book.setTotalCopies(totalCopies);
        book.setAvailableCopies(availableCopies);
        return book;
    }

    private void createLoans() {
        // Lấy dữ liệu đã tạo
        List<Patron> patrons = patronRepository.findAll();
        List<Book> books = bookRepository.findAll();
        
        if (patrons.size() >= 3 && books.size() >= 6) {
            List<Loan> loans = List.of(
                createLoan(patrons.get(2), books.get(0), LocalDate.of(2024, 1, 15), LocalDate.of(2024, 2, 15), Loan.LoanStatus.BORROWED),
                createLoan(patrons.get(2), books.get(5), LocalDate.of(2024, 1, 20), LocalDate.of(2024, 2, 20), Loan.LoanStatus.BORROWED),
                createLoan(patrons.get(3), books.get(1), LocalDate.of(2024, 1, 10), LocalDate.of(2024, 2, 10), Loan.LoanStatus.RETURNED),
                createLoan(patrons.get(3), books.get(8), LocalDate.of(2024, 1, 25), LocalDate.of(2024, 2, 25), Loan.LoanStatus.BORROWED),
                createLoan(patrons.get(4), books.get(3), LocalDate.of(2024, 1, 18), LocalDate.of(2024, 2, 18), Loan.LoanStatus.BORROWED)
            );
            
            // Set return date for returned loans
            loans.get(2).setReturnDate(LocalDate.of(2024, 2, 8));
            
            loanRepository.saveAll(loans);
            System.out.println("Created " + loans.size() + " loans");
        }
    }

    private Loan createLoan(Patron patron, Book book, LocalDate loanDate, LocalDate dueDate, Loan.LoanStatus status) {
        Loan loan = new Loan();
        loan.setPatron(patron);
        loan.setBook(book);
        loan.setLoanDate(loanDate);
        loan.setDueDate(dueDate);
        loan.setStatus(status);
        return loan;
    }
    
    private void createAuthorsAndPublishers() {
        // Tạo tác giả
        if (authorRepository.count() == 0) {
            List<Author> authors = List.of(
                createAuthor("Herbert Schildt", "American author of books on programming languages", "American"),
                createAuthor("Craig Walls", "Spring Framework expert and author", "American"),
                createAuthor("Robert C. Martin", "Software engineer and author known as Uncle Bob", "American"),
                createAuthor("Gang of Four", "Erich Gamma, Richard Helm, Ralph Johnson, John Vlissides", "International"),
                createAuthor("Abraham Silberschatz", "Computer science professor and author", "American"),
                createAuthor("Robert Sedgewick", "Computer science professor at Princeton", "American"),
                createAuthor("David Thomas", "Pragmatic programmer and author", "British"),
                createAuthor("Eric Freeman", "Software architect and author", "American"),
                createAuthor("Joshua Bloch", "Software engineer at Google, formerly at Sun", "American"),
                createAuthor("Thomas H. Cormen", "Computer science professor at Dartmouth", "American")
            );
            
            authorRepository.saveAll(authors);
            System.out.println("Created " + authors.size() + " authors");
        }
        
        // Tạo nhà xuất bản
        if (publisherRepository.count() == 0) {
            List<Publisher> publishers = List.of(
                createPublisher("McGraw-Hill Education", "New York, USA", "Technology and Education"),
                createPublisher("Manning Publications", "Greenwich, USA", "Computer Science and Programming"),
                createPublisher("Prentice Hall", "New Jersey, USA", "Academic and Professional"),
                createPublisher("Addison-Wesley", "Massachusetts, USA", "Technology and Programming"),
                createPublisher("O'Reilly Media", "California, USA", "Technology and Programming"),
                createPublisher("MIT Press", "Massachusetts, USA", "Academic and Technical")
            );
            
            publisherRepository.saveAll(publishers);
            System.out.println("Created " + publishers.size() + " publishers");
        }
    }
    
    private Author createAuthor(String name, String biography, String nationality) {
        Author author = new Author();
        author.setName(name);
        author.setBiography(biography);
        author.setNationality(nationality);
        return author;
    }
    
    private Publisher createPublisher(String name, String address, String description) {
        Publisher publisher = new Publisher();
        publisher.setName(name);
        publisher.setAddress(address);
        publisher.setDescription(description);
        return publisher;
    }
}
