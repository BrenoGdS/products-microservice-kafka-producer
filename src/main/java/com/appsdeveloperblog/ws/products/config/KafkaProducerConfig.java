package com.appsdeveloperblog.ws.products.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.producer.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<Object, Object> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class); // Serializer for keys (converts keys from String to byte array)
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class); // Serializer for values (converts JSON to byte array)

        // Enable idempotence and strong durability
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true); // Prevents duplicate messages during retries.
        props.put(ProducerConfig.ACKS_CONFIG, "all"); // Wait for acknowledgements from all in-sync replicas for strong durability
        props.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE); // Number of retry attempts if message sending fails (adjust as needed)
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5); // Maximum number of unacknowledged requests per connection (must be ≤ 5 for idempotence)

        // Additional properties:
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000);      // Wait 1 second between retries
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120000);   // 2 minutes total delivery timeout
        props.put(ProducerConfig.LINGER_MS_CONFIG, 0);                  // Send immediately (no delay)
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);     // Wait up to 30 seconds for a broker response

        return props;
    }
}