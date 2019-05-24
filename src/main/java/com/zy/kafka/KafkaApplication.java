package com.zy.kafka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Kafka启动类
 * 
 * @author walkman
 *
 */
@SpringBootApplication(scanBasePackages = "com.zy.kafka")
public class KafkaApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(KafkaApplication.class, args);
	}
}
