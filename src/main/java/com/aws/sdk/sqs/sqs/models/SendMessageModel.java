package com.aws.sdk.sqs.sqs.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SendMessageModel extends QueueUrlModel {

    @JsonProperty("content")
    public String content;
}
