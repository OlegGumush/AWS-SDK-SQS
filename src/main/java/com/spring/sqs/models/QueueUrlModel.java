package com.spring.sqs.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class QueueUrlModel {

    @JsonProperty("queueUrl")
    public String queueUrl;
}
