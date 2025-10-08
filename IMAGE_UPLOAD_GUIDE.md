# 📸 HƯỚNG DẪN UPLOAD ẢNH TỪ MÁY TÍNH

## 📊 ĐÁNH GIÁ MỨC ĐỘ SỬA ĐỔI

### Mức độ: ⭐⭐⭐ (Trung bình - Vừa phải)

**Cần sửa**:
- ✅ Backend: 3-4 files
- ✅ Frontend: 2-3 files
- ✅ Config: 1-2 files
- ✅ Database: Không cần sửa (vẫn dùng `imageUrl` String)

**Thời gian ước tính**: 1-2 giờ

---

## 🏗️ KIẾN TRÚC HIỆN TẠI

### Backend (❌ Chỉ hỗ trợ URL)
```java
// Book.java
@Column(name = "image_url", length = 500)
private String imageUrl;  // Chỉ lưu URL string

// BookController.java
@PostMapping
public ResponseEntity<BookDTO> createBook(@RequestBody BookDTO bookDTO) {
    // ❌ Chỉ nhận JSON, không nhận file
}
```

### Frontend (❌ Chỉ nhập URL)
```html
<!-- book-management.html -->
<input type="text" formControlName="coverImage" 
       placeholder="https://example.com/image.jpg">
<!-- ❌ Không có input file -->
```

---

## ✅ KIẾN TRÚC MỚI (Upload File)

### 1. Backend: File Upload Service

```
Client upload file → Spring Boot Controller (MultipartFile)
                  → Save to disk (/uploads/books/)
                  → Generate filename (UUID + extension)
                  → Save path to database (imageUrl)
                  → Return URL to frontend
```

### 2. Frontend: File Picker

```
User chọn file → <input type="file">
              → Preview image
              → Upload via FormData
              → Nhận URL từ backend
              → Hiển thị ảnh đã upload
```

---

## 🔧 IMPLEMENTATION GUIDE

### STEP 1: Backend - Tạo FileUploadService

**File mới**: `be-qltv/src/main/java/com/example/be_qltv/service/FileUploadService.java`

```java
package com.example.be_qltv.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileUploadService {

    @Value("${file.upload.dir:uploads/books}")
    private String uploadDir;

    /**
     * Upload file và trả về relative path
     */
    public String uploadFile(MultipartFile file) throws IOException {
        // Validate file
        if (file.isEmpty()) {
            throw new IOException("Cannot upload empty file");
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IOException("Only image files are allowed");
        }

        // Create upload directory if not exists
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = UUID.randomUUID().toString() + extension;

        // Save file
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Return relative path (will be served by static resource handler)
        return "/uploads/books/" + filename;
    }

    /**
     * Delete file
     */
    public void deleteFile(String filePath) {
        try {
            if (filePath != null && filePath.startsWith("/uploads/")) {
                Path path = Paths.get(uploadDir, 
                    filePath.substring(filePath.lastIndexOf("/") + 1));
                Files.deleteIfExists(path);
            }
        } catch (IOException e) {
            // Log error but don't throw exception
            System.err.println("Error deleting file: " + e.getMessage());
        }
    }
}
```

---

### STEP 2: Backend - Cập nhật BookController

**File**: `be-qltv/src/main/java/com/example/be_qltv/controller/BookController.java`

```java
// Thêm imports
import org.springframework.web.multipart.MultipartFile;
import com.example.be_qltv.service.FileUploadService;

@RestController
@RequestMapping("/api/books")
public class BookController {
    
    @Autowired
    private BookService bookService;
    
    @Autowired
    private FileUploadService fileUploadService;  // ← THÊM MỚI
    
    // ... existing code ...
    
    /**
     * Upload book cover image
     * POST /api/books/upload-cover
     */
    @PostMapping("/upload-cover")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<?> uploadCoverImage(@RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = fileUploadService.uploadFile(file);
            return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
        } catch (IOException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Failed to upload image: " + e.getMessage()));
        }
    }
    
    /**
     * Create book with file upload support
     * POST /api/books/with-image
     */
    @PostMapping("/with-image")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<?> createBookWithImage(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("isbn") String isbn,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "publishedDate", required = false) String publishedDate,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "authorId", required = false) Long authorId,
            @RequestParam(value = "publisherId", required = false) Long publisherId,
            @RequestParam(value = "totalCopies", defaultValue = "1") Integer totalCopies) {
        
        try {
            BookDTO bookDTO = new BookDTO();
            bookDTO.setTitle(title);
            bookDTO.setIsbn(isbn);
            bookDTO.setDescription(description);
            // ... set other fields ...
            
            // Upload image if provided
            if (file != null && !file.isEmpty()) {
                String imageUrl = fileUploadService.uploadFile(file);
                bookDTO.setCoverImage(imageUrl);
            }
            
            BookDTO createdBook = bookService.createBook(bookDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdBook);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Failed to create book: " + e.getMessage()));
        }
    }
}
```

---

### STEP 3: Backend - Configure Static Resources

**File**: `be-qltv/src/main/java/com/example/be_qltv/config/WebConfig.java`

```java
package com.example.be_qltv.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.dir:uploads/books}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve uploaded files
        registry.addResourceHandler("/uploads/books/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }
}
```

**File**: `be-qltv/src/main/resources/application.properties`

```properties
# File Upload Configuration
file.upload.dir=uploads/books
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

---

### STEP 4: Frontend - Cập nhật BookService

**File**: `fe-qltv/src/app/services/book.service.ts`

```typescript
// Thêm method upload image
uploadCoverImage(file: File): Observable<{ imageUrl: string }> {
  const formData = new FormData();
  formData.append('file', file);
  
  return this.http.post<{ imageUrl: string }>(
    `${this.apiUrl}/upload-cover`,
    formData
  );
}

// Method tạo book với image upload
createBookWithImage(bookData: any, file?: File): Observable<BookDTO> {
  const formData = new FormData();
  
  // Append book data
  formData.append('title', bookData.title);
  formData.append('isbn', bookData.isbn);
  if (bookData.description) formData.append('description', bookData.description);
  if (bookData.categoryId) formData.append('categoryId', bookData.categoryId.toString());
  // ... append other fields ...
  
  // Append file if exists
  if (file) {
    formData.append('file', file);
  }
  
  return this.http.post<BookDTO>(
    `${this.apiUrl}/with-image`,
    formData
  );
}
```

---

### STEP 5: Frontend - Cập nhật Book Management Component

**File**: `fe-qltv/src/app/components/admin/book-management/book-management.html`

```html
<form [formGroup]="bookForm" (ngSubmit)="onSubmit()">
  <!-- ... existing fields ... -->
  
  <!-- Cover Image Upload -->
  <div class="form-group">
    <label>Cover Image</label>
    
    <!-- Option 1: Upload File -->
    <div class="upload-option">
      <input 
        type="file" 
        #fileInput
        accept="image/*"
        (change)="onFileSelected($event)"
        class="file-input"
      />
      <button 
        type="button" 
        (click)="fileInput.click()"
        class="upload-button"
      >
        📁 Choose File
      </button>
      <span *ngIf="selectedFile">{{ selectedFile.name }}</span>
    </div>
    
    <!-- Option 2: Enter URL -->
    <div class="url-option">
      <input 
        type="text" 
        formControlName="coverImage"
        placeholder="Or enter image URL"
        class="form-control"
      />
    </div>
    
    <!-- Image Preview -->
    <div *ngIf="imagePreview" class="image-preview">
      <img [src]="imagePreview" alt="Preview" />
      <button type="button" (click)="clearImage()">✕ Remove</button>
    </div>
  </div>
  
  <!-- ... submit button ... -->
</form>
```

**File**: `fe-qltv/src/app/components/admin/book-management/book-management.ts`

```typescript
export class BookManagement {
  selectedFile: File | null = null;
  imagePreview: string | null = null;
  
  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      this.selectedFile = file;
      
      // Validate file type
      if (!file.type.startsWith('image/')) {
        alert('Please select an image file');
        this.selectedFile = null;
        return;
      }
      
      // Validate file size (max 10MB)
      if (file.size > 10 * 1024 * 1024) {
        alert('File size must be less than 10MB');
        this.selectedFile = null;
        return;
      }
      
      // Preview image
      const reader = new FileReader();
      reader.onload = (e: any) => {
        this.imagePreview = e.target.result;
      };
      reader.readAsDataURL(file);
    }
  }
  
  clearImage(): void {
    this.selectedFile = null;
    this.imagePreview = null;
    this.bookForm.patchValue({ coverImage: '' });
  }
  
  onSubmit(): void {
    if (this.bookForm.valid) {
      const formValue = this.bookForm.value;
      
      if (this.selectedFile) {
        // Upload file first
        this.bookService.uploadCoverImage(this.selectedFile).subscribe({
          next: (response) => {
            // Set uploaded image URL
            formValue.coverImage = response.imageUrl;
            this.createOrUpdateBook(formValue);
          },
          error: (error) => {
            console.error('Error uploading image:', error);
            alert('Failed to upload image');
          }
        });
      } else {
        // No file, just create book with URL (if provided)
        this.createOrUpdateBook(formValue);
      }
    }
  }
  
  private createOrUpdateBook(bookData: any): void {
    if (this.modalMode === 'add') {
      this.bookService.createBook(bookData).subscribe({
        next: (book) => {
          this.loadBooks();
          this.closeModal();
        },
        error: (error) => {
          console.error('Error creating book:', error);
          alert('Failed to create book');
        }
      });
    } else {
      // Update logic...
    }
  }
}
```

---

## 🎨 CSS Styling

**File**: `fe-qltv/src/app/components/admin/book-management/book-management.css`

```css
.upload-option {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.file-input {
  display: none;
}

.upload-button {
  padding: 8px 16px;
  background: #4F46E5;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}

.upload-button:hover {
  background: #4338CA;
}

.image-preview {
  margin-top: 15px;
  position: relative;
  display: inline-block;
}

.image-preview img {
  max-width: 200px;
  max-height: 200px;
  border-radius: 8px;
  border: 2px solid #E5E7EB;
}

.image-preview button {
  position: absolute;
  top: -10px;
  right: -10px;
  background: #EF4444;
  color: white;
  border: none;
  border-radius: 50%;
  width: 30px;
  height: 30px;
  cursor: pointer;
  font-size: 16px;
}

.url-option {
  margin-top: 10px;
}

.url-option input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #D1D5DB;
  border-radius: 6px;
}
```

---

## 📂 Cấu trúc thư mục Upload

```
be-qltv/
├── uploads/
│   └── books/
│       ├── a3f8d9e2-4b5c-11ef-8e12-0242ac120002.jpg
│       ├── b7c1a5f3-4b5c-11ef-8e12-0242ac120003.png
│       └── c9d2e4a6-4b5c-11ef-8e12-0242ac120004.webp
└── src/
    └── main/
        └── resources/
            └── application.properties
```

**Lưu ý**: Thêm `uploads/` vào `.gitignore`:

```gitignore
# Uploaded files
uploads/
be-qltv/uploads/
```

---

## 🔒 Security Considerations

### 1. File Type Validation
```java
// Backend
String contentType = file.getContentType();
if (!contentType.startsWith("image/")) {
    throw new IOException("Only images allowed");
}
```

### 2. File Size Limit
```properties
# application.properties
spring.servlet.multipart.max-file-size=10MB
```

### 3. Filename Sanitization
```java
// Use UUID instead of original filename
String filename = UUID.randomUUID().toString() + extension;
```

### 4. Access Control
```java
@PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
public ResponseEntity<?> uploadCoverImage(...) {
    // Only ADMIN and LIBRARIAN can upload
}
```

---

## 🧪 TESTING

### Test 1: Upload Image via Postman

**Request**:
```
POST http://localhost:8081/api/books/upload-cover
Authorization: Bearer <token>
Content-Type: multipart/form-data

Body:
  file: [Select image file]
```

**Expected Response**:
```json
{
  "imageUrl": "/uploads/books/a3f8d9e2-4b5c-11ef-8e12-0242ac120002.jpg"
}
```

### Test 2: Access Uploaded Image

**URL**: `http://localhost:8081/uploads/books/a3f8d9e2-4b5c-11ef-8e12-0242ac120002.jpg`

**Expected**: Image displays in browser

### Test 3: Create Book with Image

1. Open Book Management
2. Click "Add Book"
3. Click "Choose File" → Select image
4. Preview shows
5. Fill other fields
6. Submit
7. Book created with uploaded image

---

## 📊 SO SÁNH TRƯỚC/SAU

### TRƯỚC (URL only)
```
Admin nhập URL → Backend lưu URL string → Frontend hiển thị từ URL
```

**Vấn đề**:
- ❌ Phụ thuộc external URLs (broken links)
- ❌ Không kiểm soát được images
- ❌ Không có backup

### SAU (File Upload)
```
Admin upload file → Backend lưu file + URL → Frontend hiển thị từ server
```

**Ưu điểm**:
- ✅ Kiểm soát hoàn toàn images
- ✅ Không phụ thuộc external sources
- ✅ Có thể backup dễ dàng
- ✅ Tốc độ load nhanh hơn (local server)

---

## 🎯 CHECKLIST IMPLEMENTATION

### Backend
- [ ] Tạo `FileUploadService.java`
- [ ] Thêm `uploadCoverImage()` vào `BookController`
- [ ] Tạo `WebConfig.java` (static resource handler)
- [ ] Cập nhật `application.properties`
- [ ] Tạo thư mục `uploads/books/`
- [ ] Test upload API với Postman

### Frontend
- [ ] Thêm `uploadCoverImage()` vào `book.service.ts`
- [ ] Thêm `<input type="file">` vào form
- [ ] Implement `onFileSelected()` method
- [ ] Implement image preview
- [ ] Implement clear image function
- [ ] Update submit logic
- [ ] Add CSS styling
- [ ] Test upload qua UI

### Testing
- [ ] Upload image < 10MB
- [ ] Upload image > 10MB (should fail)
- [ ] Upload non-image file (should fail)
- [ ] Access uploaded image via URL
- [ ] Create book with uploaded image
- [ ] Delete book (optional: delete image file)

### Deployment
- [ ] Thêm `uploads/` vào `.gitignore`
- [ ] Backup `uploads/` folder trước khi deploy
- [ ] Configure Nginx/Apache để serve static files

---

## 🚀 DEPLOY TO PRODUCTION

### Option 1: Local Storage (Simple)
```nginx
# Nginx config
location /uploads/ {
    alias /var/www/qltv/uploads/;
    expires 30d;
    add_header Cache-Control "public, immutable";
}
```

### Option 2: Cloud Storage (Advanced)
- AWS S3
- Google Cloud Storage
- Cloudinary
- ImgBB

**Lợi ích**:
- ✅ CDN support
- ✅ Auto backup
- ✅ Scalable
- ✅ Free tier available

---

## 💡 OPTIONAL FEATURES

### 1. Image Resize/Optimization
```java
// Use Thumbnailator library
Thumbnails.of(file.getInputStream())
    .size(800, 600)
    .outputQuality(0.8)
    .toFile(filePath.toFile());
```

### 2. Multiple Images per Book
```java
// Support gallery
private List<String> imageUrls; // Instead of single imageUrl
```

### 3. Drag & Drop Upload
```html
<div 
  (drop)="onDrop($event)" 
  (dragover)="onDragOver($event)"
  class="dropzone"
>
  Drag image here or click to browse
</div>
```

---

## 📝 TÓM TẮT

**Cần sửa**:
1. Backend: 3 files mới + 1 file config
2. Frontend: 2 files update (service + component)
3. Total: ~6-7 files

**Thời gian**: 1-2 giờ

**Mức độ khó**: ⭐⭐⭐ (Trung bình)

**Lợi ích**:
- ✅ Kiểm soát hoàn toàn images
- ✅ UX tốt hơn (file picker + preview)
- ✅ Không phụ thuộc external URLs
- ✅ Professional hơn

**Nhược điểm**:
- ⚠️ Tốn disk space
- ⚠️ Cần backup định kỳ
- ⚠️ Cần config server để serve files

---

**Khuyến nghị**: NÊN IMPLEMENT! Feature này làm app professional hơn rất nhiều.

---

**Ngày tạo**: 08/10/2025  
**Status**: 📘 Ready to implement
