package com.example.ds;

public class Message {

    private String text;
    private String sender;
    private String senderColour;

    public Message(String text, String sender, String senderColor) {
        this.text = text;
        this.sender = sender;
        this.senderColour = senderColor;
    }

    public Message(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSenderColour() {
        return senderColour;
    }

    public void setSenderColour(String senderColour) {
        this.senderColour = senderColour;
    }
}