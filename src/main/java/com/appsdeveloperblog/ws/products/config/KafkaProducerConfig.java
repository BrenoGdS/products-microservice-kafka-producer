package com.appsdeveloperblog.ws.products.config;

import com.appsdeveloperblog.ws.coreblog.event.ProductCreatedEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.producer.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.producer.key-serializer}")
    private String keySerializer;

    @Value("${spring.kafka.producer.value-serializer}")
    private String valueSerializer;

    @Value("${spring.kafka.producer.enable.idempotence}")
    private boolean enableIdempotence;

    @Value("${spring.kafka.producer.acks}")
    private String acks;

    @Value("${spring.kafka.producer.retries}")
    private int retries;

    @Value("${spring.kafka.producer.max.in.flight.requests.per.connection}")
    private int maxInFlightRequestsPerConnection;

    @Value("${spring.kafka.producer.properties.retry.backoff.ms}")
    private int retryBackoffMs;

    @Value("${spring.kafka.producer.properties.delivery.timeout.ms}")
    private int deliveryTimeoutMs;

    @Value("${spring.kafka.producer.properties.linger.ms}")
    private int lingerMs;

    @Value("${spring.kafka.producer.properties.request.timeout.ms}")
    private int requestTimeoutMs;

    @Bean
    public KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate(ProducerFactory<String, ProductCreatedEvent> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }


    @Bean
    public ProducerFactory<String, ProductCreatedEvent> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer);  // Serializer for keys (converts keys from String to byte array)
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);  // Serializer for values (converts JSON to byte array)

        // Enable idempotence and strong durability
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, enableIdempotence);  // Prevents duplicate messages during retries.
        props.put(ProducerConfig.ACKS_CONFIG, acks);  // Wait for acknowledgements from all in-sync replicas for strong durability
        props.put(ProducerConfig.RETRIES_CONFIG, retries);  // Number of retry attempts if message sending fails (adjust as needed)
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, maxInFlightRequestsPerConnection);  // Maximum number of unacknowledged requests per connection (must be ≤ 5 for idempotence)

        // Additional properties:
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, retryBackoffMs);  // Wait 1 second between retries
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, deliveryTimeoutMs);  // 2 minutes total delivery timeout
        props.put(ProducerConfig.LINGER_MS_CONFIG, lingerMs);  // Send immediately (no delay)
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, requestTimeoutMs);  // Wait up to 30 seconds for a broker response

        return props;
    }
}