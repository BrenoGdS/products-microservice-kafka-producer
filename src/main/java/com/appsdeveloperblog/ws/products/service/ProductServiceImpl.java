package com.appsdeveloperblog.ws.products.service;

import com.appsdeveloperblog.ws.coreblog.event.ProductCreatedEvent;
import com.appsdeveloperblog.ws.products.model.ProductModel;
import com.appsdeveloperblog.ws.products.repository.ProductRepository;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class ProductServiceImpl implements ProductService {

    KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate;
    ProductRepository productRepository;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public ProductServiceImpl(KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate, ProductRepository productRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.productRepository = productRepository;
    }

    @Override
    public String createProduct(ProductModel productRequest) {

        Optional<ProductModel> productO = productRepository.findByTitle(productRequest.getTitle());
        UUID productId;
        if (productO.isPresent()) {
            productId = ((ProductModel) productRepository.save(productO.get())).getId();
        }else{
            productId = ((ProductModel) productRepository.save(productRequest)).getId();
        }
        
        ProductCreatedEvent productCreatedEvent = new ProductCreatedEvent(productId,
                productRequest.getTitle(), productRequest.getPrice(), productRequest.getQuantity());

        // Create a ProducerRecord to include the message header "messageId"
        ProducerRecord<String, ProductCreatedEvent> record = new ProducerRecord<>("product-created-events-topic",
                productId.toString(), productCreatedEvent);
        record.headers().add(new RecordHeader("messageId", productId.toString().getBytes(StandardCharsets.UTF_8)));

        // Produce the message asynchronously (non-blocking)
        CompletableFuture<SendResult<String, ProductCreatedEvent>> asyncFuture = kafkaTemplate.send(record);

        handleKafkaSendResult(asyncFuture, productId);

        // To turn the asynchronous call into synchronous, simply add:
        // asyncFuture.join();
        // Alternatively, you can produce the message synchronously (blocking) like this:
        //producesMessageSynchronously(newProduct, productCreatedEvent);

        return productId.toString();
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

    private void handleKafkaSendResult(CompletableFuture<SendResult<String, ProductCreatedEvent>> future, UUID productID) {
        future.whenComplete((result, exception) -> {
            if(exception != null) {
             LOGGER.error("Failed to sent message: " + exception.getMessage());
            } else {
             LOGGER.info("Message sent successfully: " + result.getRecordMetadata());
            }
        });
        LOGGER.info("******* resulting product id: " + productID.toString());
    }
}
