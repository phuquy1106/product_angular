package com.example.demo_springboot.controller;

import com.example.demo_springboot.Exception.ResourceNotFoundException;
import com.example.demo_springboot.model.Product;
import com.example.demo_springboot.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping("/api/product/")
@RestController

public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @CrossOrigin("*")
    @GetMapping("/")
    public ResponseEntity<List<Product>> getAllProduct(){
        List<Product> productList = productRepository.findAll();
        return ResponseEntity.ok(productList);
    }

    @CrossOrigin("*")
    @PostMapping("/")
    public ResponseEntity<Product> createProduct(@RequestBody Product product){
        Product productNew = productRepository.save(product);
        return ResponseEntity.ok(productNew);
    }
    @CrossOrigin("*")
    @DeleteMapping("{id}")
    public  ResponseEntity<Map<String, Boolean>> deleteProduct(@PathVariable Long id){
        Product product =  productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product not exist with id :" + id));
        if (product.getImg() != null && !product.getImg().isEmpty()) {
            Path imagePath = Paths.get("E:/angular/img").resolve(product.getImg()).normalize();
            try {
                Files.deleteIfExists(imagePath);
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("delete", false));
            }
        }
        productRepository.delete(product);
        Map<String, Boolean> result =new HashMap<>();
        result.put("delete",true);
        return ResponseEntity.ok(result);
    }

}
