package com.spring.sqs.policy;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DLQPolicyModel {

    @JsonProperty("deadLetterTargetArn")
    public String deadLetterTargetArn;

    @JsonProperty("maxReceiveCount")
    public String maxReceiveCount;

    public DLQPolicyModel(String deadLetterTargetArn, String maxReceiveCount) {
        this.deadLetterTargetArn = deadLetterTargetArn;
        this.maxReceiveCount = maxReceiveCount;
    }
}
