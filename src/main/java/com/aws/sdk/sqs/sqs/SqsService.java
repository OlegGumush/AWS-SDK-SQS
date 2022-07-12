package com.aws.sdk.sqs.sqs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.aws.sdk.sqs.sqs.policy.DLQPolicyModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Service
public class SqsService {

    public static final String QUEUE_PREFIX = "MySpringAwsSQS-";

    public static final String DLQ = "DLQ";

    @Autowired
    private SqsClient sqsClient;

    @Autowired
    private ObjectMapper objectMapper;

    public GetQueueUrlResponse getQueueByName(String queueName) {

        GetQueueUrlRequest getQueueUrl = GetQueueUrlRequest.builder()
                .queueName(queueName)
                .build();

        return sqsClient.getQueueUrl(getQueueUrl);
    }

    public CreateQueueResponse createQueue(String name) {

        String queueName = QUEUE_PREFIX + name;

        CreateQueueRequest createQueueRequest = CreateQueueRequest.builder()
                .queueName(queueName)
                .build();

        return sqsClient.createQueue(createQueueRequest);
    }

    public void createQueueDLQ(String linkToQueue) throws JsonProcessingException {

        CreateQueueResponse deadLetterQueue = createQueue(DLQ);

        linkDeadLetterQueueToQueue(linkToQueue, deadLetterQueue);
    }

    public void linkDeadLetterQueueToQueue(String queueUrl, CreateQueueResponse deadLetterQueue) throws JsonProcessingException {

        // Link the DLQ to the source queue
        String arn = getQueueArn(deadLetterQueue.queueUrl());

        // Specify the Redrive Policy
        HashMap<QueueAttributeName, String> attributes = new HashMap<QueueAttributeName, String>();
        attributes.put(QueueAttributeName.REDRIVE_POLICY, objectMapper.writeValueAsString(new DLQPolicyModel(arn, "3")));

        SetQueueAttributesRequest setAttrRequest = SetQueueAttributesRequest.builder()
                .queueUrl(queueUrl)
                .attributes(attributes)
                .build();

        sqsClient.setQueueAttributes(setAttrRequest);
    }

    private String getQueueArn(String queueName) {

        GetQueueAttributesRequest request = GetQueueAttributesRequest.builder()
                .queueUrl(queueName)
                .attributeNames(QueueAttributeName.QUEUE_ARN)
                .build();

        GetQueueAttributesResponse queueAttributes = sqsClient.getQueueAttributes(request);
        return queueAttributes.attributes().get(QueueAttributeName.QUEUE_ARN);
    }

    public List<String> listQueues() {

        ListQueuesRequest listQueuesRequest = ListQueuesRequest.builder()
                .queueNamePrefix(QUEUE_PREFIX)
                .build();

        ListQueuesResponse listQueuesResponse = sqsClient.listQueues(listQueuesRequest);

        List<String> queues = new ArrayList<>();
        for (String url : listQueuesResponse.queueUrls()) {
            queues.add(url);
        }
        return queues;
    }

    public void sendMessage(String queueUrl, String content) {

        SendMessageRequest messageRequest = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(content)
                .build();

        sqsClient.sendMessage(messageRequest);
    }

    public List<String> receiveMessagesWithoutDelete(String queueUrl) {

        List<Message> receivedMessages = receiveMessages(queueUrl);

        List<String> messages = new ArrayList<>();
        for (Message receivedMessage : receivedMessages) {
            messages.add(receivedMessage.body());
        }

        return messages;
    }

    private List<Message> receiveMessages(String queueUrl) {

        ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .build();

        return sqsClient.receiveMessage(receiveMessageRequest).messages();
    }

    public List<String> receiveMessagesWithDelete(String queueUrl) {

        List<Message> receivedMessages = receiveMessages(queueUrl);

        List<String> messages = new ArrayList<>();
        for (Message receivedMessage : receivedMessages) {
            messages.add(receivedMessage.body());
            deleteMessage(queueUrl, receivedMessage);
        }
        return messages;
    }

    public void createLongPollingQueue(String name) throws JsonProcessingException {

        CreateQueueResponse queue = createQueue(name);

        HashMap<QueueAttributeName, String> attributes = new HashMap<QueueAttributeName, String>();
        attributes.put(QueueAttributeName.RECEIVE_MESSAGE_WAIT_TIME_SECONDS, "20");

        SetQueueAttributesRequest setAttrsRequest = SetQueueAttributesRequest.builder()
                .queueUrl(queue.queueUrl())
                .attributes(attributes)
                .build();

        sqsClient.setQueueAttributes(setAttrsRequest);
    }

    public List<String> receiveMessagesWithoutDeleteLimitedVisibilityTimeout(String queueUrl) {

        List<Message> messages = receiveMessages(queueUrl);

        if (messages.isEmpty()) {
            return new ArrayList<>();
        }

        Message message = messages.get(0);

        changeMessageVisibility(queueUrl, message);

        return Arrays.asList(message.body());
    }

    public void deleteMessage(String queueUrl, Message message) {

        DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(message.receiptHandle())
                .build();
        sqsClient.deleteMessage(deleteMessageRequest);
    }

    private void changeMessageVisibility(String queueUrl, Message message) {
        ChangeMessageVisibilityRequest visibilityRequest = ChangeMessageVisibilityRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(message.receiptHandle())
                .visibilityTimeout(5)
                .build();

        sqsClient.changeMessageVisibility(visibilityRequest);
    }

    public List<String> receiveMessagesWithLongPolling(String queueUrl) {

        ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .waitTimeSeconds(20)
                .build();
        List<Message> receivedMessages = sqsClient.receiveMessage(receiveMessageRequest).messages();

        List<String> messages = new ArrayList<>();
        for (Message receivedMessage : receivedMessages) {
            messages.add(receivedMessage.body());
            deleteMessage(queueUrl, receivedMessage);
        }
        return messages;
    }

    public void deleteQueue(String queueUrl) {

        DeleteQueueRequest deleteQueueRequest = DeleteQueueRequest.builder()
                .queueUrl(queueUrl)
                .build();

        sqsClient.deleteQueue(deleteQueueRequest);
    }
}