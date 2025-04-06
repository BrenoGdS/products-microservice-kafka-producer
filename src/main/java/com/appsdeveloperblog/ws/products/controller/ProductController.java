package com.appsdeveloperblog.ws.products.controller;

import com.appsdeveloperblog.ws.products.model.ProductModel;
import com.appsdeveloperblog.ws.products.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products") //localhost:8080/products
public class ProductController {

    ProductService productService;

    public ProductController(ProductService productService){
        this.productService = productService;
    }

    /**
     * curl -X POST -H "Content-Type: application/json" -d '{"title": "Product A", "price": 100, "quantity": 19}' http://localhost:8080/products/create
     * @param product
     * @return
     */
    @PostMapping("/create")
    public ResponseEntity<String> createProduct(@RequestBody ProductModel product){
        String productId = productService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(productId);
    }
}
