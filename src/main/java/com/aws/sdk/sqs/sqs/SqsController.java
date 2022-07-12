package com.aws.sdk.sqs.sqs;

import com.aws.sdk.sqs.sqs.models.QueueNameModel;
import com.aws.sdk.sqs.sqs.models.QueueUrlModel;
import com.aws.sdk.sqs.sqs.models.SendMessageModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.sqs.model.CreateQueueResponse;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;

import java.util.List;

@Slf4j
@RestController
public class SqsController {

    @Autowired
    private SqsService sqsService;

    @GetMapping("/sqs")
    public ResponseEntity<List<String>> getAllQueues() {

        List<String> queues = sqsService.listQueues();

        return ResponseEntity.ok(queues);
    }

    @PostMapping("/sqs/getQueueByName")
    public ResponseEntity<String> getQueueByName(@RequestBody QueueNameModel queueNameModel) {

        GetQueueUrlResponse queueResponse = sqsService.getQueueByName(queueNameModel.name);

        return ResponseEntity.ok(queueResponse.queueUrl());
    }

    @PostMapping("/sqs/createSimpleQueue")
    public ResponseEntity<String> createSimpleQueue(@RequestBody QueueNameModel queueNameModel) {

        CreateQueueResponse createQueueResponse = sqsService.createQueue(queueNameModel.name);

        return ResponseEntity.ok(createQueueResponse.queueUrl());
    }

    @PostMapping("/sqs/createDeadLetterQueue")
    public ResponseEntity<String> createDeadLetterQueue(@RequestBody QueueUrlModel model) throws JsonProcessingException {

        sqsService.createQueueDLQ(model.queueUrl);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/sqs/createLongPollingQueue")
    public ResponseEntity<String> createLongPollingQueue(@RequestBody QueueNameModel model) throws JsonProcessingException {

        sqsService.createLongPollingQueue(model.name);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/sqs/sendMessage")
    public ResponseEntity<Object> sendMessage(@RequestBody SendMessageModel message) {

        sqsService.sendMessage(message.queueUrl, message.content);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/sqs/receiveMessagesWithoutDelete")
    public ResponseEntity<List<String>> receiveMessagesWithoutDelete(@RequestBody QueueUrlModel receiveMessageModel) {

        List<String> messages = sqsService.receiveMessagesWithoutDelete(receiveMessageModel.queueUrl);

        return ResponseEntity.ok(messages);
    }

    @PostMapping("/sqs/receiveMessagesWithDelete")
    public ResponseEntity<List<String>> receiveMessagesWithDelete(@RequestBody QueueUrlModel receiveMessageModel) {

        List<String> messages = sqsService.receiveMessagesWithDelete(receiveMessageModel.queueUrl);

        return ResponseEntity.ok(messages);
    }

    @PostMapping("/sqs/receiveMessagesWithoutDeleteLimitedVisibilityTimeout")
    public ResponseEntity<List<String>> receiveMessagesWithoutDeleteLimitedVisibilityTimeout(@RequestBody QueueUrlModel receiveMessageModel) {

        List<String> messages = sqsService.receiveMessagesWithoutDeleteLimitedVisibilityTimeout(receiveMessageModel.queueUrl);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/sqs/receiveMessagesWithLongPolling")
    public ResponseEntity<List<String>> receiveMessagesWithLongPolling(@RequestBody QueueUrlModel receiveMessageModel) {

        List<String> messages = sqsService.receiveMessagesWithLongPolling(receiveMessageModel.queueUrl);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/sqs/deleteQueue")
    public ResponseEntity<List<String>> deleteQueue(@RequestBody QueueUrlModel receiveMessageModel) {

        sqsService.deleteQueue(receiveMessageModel.queueUrl);
        return ResponseEntity.ok().build();
    }
}
