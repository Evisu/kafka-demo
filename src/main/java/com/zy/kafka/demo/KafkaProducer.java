package com.zy.kafka.demo;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 生产者
 */
@RestController
@RequestMapping("/kafka")
public class KafkaProducer {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 定时任务
     */
    @RequestMapping(value = "/send", method = RequestMethod.GET)
    public void send(HttpServletRequest request, HttpServletResponse response){
        final String message = UUID.randomUUID().toString();
        ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send("test","标识",message);
        future.addCallback(o -> logger.info("send-消息发送成功：" + message), throwable -> logger.info("消息发送失败：" + message));
    }

}