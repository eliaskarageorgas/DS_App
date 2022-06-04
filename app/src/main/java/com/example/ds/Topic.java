package com.example.ds;

public class Topic {

    private String topicName;
    private String topicColour;

    public Topic(String topicName, String topicColour) {
        this.topicName = topicName;
        this.topicColour = topicColour;
    }

    public String getTopicName() {
        return topicName;
    }

    public String getTopicColour() {
        return topicColour;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public void setTopicColour(String topicColour) {
        this.topicColour = topicColour;
    }
}
