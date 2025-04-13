package com.appsdeveloperblog.ws.products.repository;

import com.appsdeveloperblog.ws.products.model.ProductModel;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends CrudRepository<ProductModel, String> {
}