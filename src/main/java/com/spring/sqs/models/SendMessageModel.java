package com.spring.sqs.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SendMessageModel extends QueueUrlModel {

    @JsonProperty("content")
    public String content;
}
