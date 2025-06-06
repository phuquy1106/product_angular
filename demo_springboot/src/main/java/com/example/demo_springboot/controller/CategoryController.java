package com.example.demo_springboot.controller;

import com.example.demo_springboot.Exception.ResourceNotFoundException;
import com.example.demo_springboot.model.Category;
import com.example.demo_springboot.model.Product;
import com.example.demo_springboot.repository.CategoryRepository;
import com.example.demo_springboot.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping("/api/category/")
@RestController

public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    private final String uploadDir = "E:/angular/img";
    @CrossOrigin("*")
    @PostMapping("/upload-image")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File is empty");
        }

        try {
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

            String filenameWithoutExt = originalFilename.contains(".")
                    ? originalFilename.substring(0, originalFilename.lastIndexOf("."))
                    : originalFilename;
            String extension = originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";

            String timestamp = String.valueOf(System.currentTimeMillis());
            String newFilename = filenameWithoutExt + "_" + timestamp + extension;

            Path destinationPath = Paths.get(uploadDir).resolve(newFilename).normalize();

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationPath, StandardCopyOption.REPLACE_EXISTING);
            }
            return ResponseEntity.ok(newFilename);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not save file: " + e.getMessage());
        }
    }

    @CrossOrigin("*")
    @GetMapping("/image")
    public ResponseEntity<byte[]> getImage(@RequestParam String filename) {
        try {
            Path imagePath = Paths.get(uploadDir, filename);
            if (!Files.exists(imagePath)) {
                return ResponseEntity.notFound().build();
            }

            byte[] imageBytes = Files.readAllBytes(imagePath);
            String contentType = Files.probeContentType(imagePath);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(imageBytes);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @CrossOrigin("*")
    @DeleteMapping("/image/delete")
        public ResponseEntity<String> deleteImage(@RequestParam("filename") String filename) {
        File file = new File( uploadDir+ File.separator + filename);

        if (!file.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found: " + filename);
        }

        boolean deleted = file.delete();

        if (deleted) {
            return ResponseEntity.ok("File deleted: " + filename);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete file: " + filename);
        }
    }
    @CrossOrigin("*")
    @GetMapping("/")
    public ResponseEntity<List<Category>> getAllCategory(){
        List<Category> categoryList = categoryRepository.findAll();
        return ResponseEntity.ok(categoryList);
    }
    @CrossOrigin("*")
    @GetMapping("{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id){
        Category category =  categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category not exist with id :" + id));

        return ResponseEntity.ok(category);
    }
    @CrossOrigin("*")
    @PostMapping("/")
    public ResponseEntity<Category> createCategory(@RequestBody Category category){
        Category categoryNew = categoryRepository.save(category);
        return ResponseEntity.ok(categoryNew);
    }
    @CrossOrigin("*")
    @DeleteMapping("{id}")
    public  ResponseEntity< Map<String, Boolean>> deleteCategory(@PathVariable Long id){
        Category category =  categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category not exist with id :" + id));

        List<Product> productsToDelete = productRepository.findByCategoryId(id);
        if(productsToDelete.size()>0){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("delete", false));
        }
//        productRepository.deleteAll(productsToDelete);

        if (category.getImg() != null && !category.getImg().isEmpty()) {
            Path imagePath = Paths.get(uploadDir).resolve(category.getImg()).normalize();
            try {
                Files.deleteIfExists(imagePath);
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("delete", false));
            }
        }
        categoryRepository.delete(category);
        Map<String, Boolean> result =new HashMap<>();
        result.put("delete",true);
        return ResponseEntity.ok(result);
    }

}
