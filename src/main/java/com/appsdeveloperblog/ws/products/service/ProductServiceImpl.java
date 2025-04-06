package com.appsdeveloperblog.ws.products.service;

import com.appsdeveloperblog.ws.products.event.ProductCreatedEvent;
import com.appsdeveloperblog.ws.products.model.ProductModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class ProductServiceImpl implements ProductService {

    KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public ProductServiceImpl(KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public String createProduct(ProductModel productRestModel) {

        String productID = saveProduct();
        ProductCreatedEvent productCreatedEvent = new ProductCreatedEvent(productID,
                productRestModel.getTitle(), productRestModel.getPrice(), productRestModel.getQuantity());

        // Producing the message asynchronously (non-blocking):
        CompletableFuture<SendResult<String, ProductCreatedEvent>> asyncFuture =
                kafkaTemplate.send("product-created-events-topic", productID, productCreatedEvent);

        handleKafkaSendResult(asyncFuture, productID);

        // To turn the asynchronous call into synchronous, simply add:
        // asyncFuture.join();

        // Alternatively, you can produce the message synchronously (blocking) like this:
        //producesMessageSynchronously(productID, productCreatedEvent);

        return productID;
    }

    private void producesMessageSynchronously(String productID, ProductCreatedEvent productCreatedEvent) {
        try {
            SendResult<String, ProductCreatedEvent> result =
                    kafkaTemplate.send("product-created-events-topic", productID, productCreatedEvent).get(); //.get turns into synchronously
            System.out.println("Message sent successfully: " + result.getRecordMetadata());
        } catch (Exception e) {
            System.err.println("Failed to send message: " + e.getMessage());
        }
    }

    private static String saveProduct() {
        return UUID.randomUUID().toString();
    }

    private void handleKafkaSendResult(CompletableFuture<SendResult<String, ProductCreatedEvent>> future, String productID) {
        future.whenComplete((result, exception) -> {
            if(exception != null) {
             LOGGER.error("Failed to sent message: " + exception.getMessage());
            } else {
             LOGGER.info("Message sent successfully: " + result.getRecordMetadata());
            }
        });
        LOGGER.info("******* resulting product id: " + productID);
    }
}
