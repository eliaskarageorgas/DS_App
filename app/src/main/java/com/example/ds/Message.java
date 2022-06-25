package com.example.ds;

public class Message {

    private String text;
    private int sender;
    private String senderColour;

    public Message(String text, int sender, String senderColor) {
        this.text = text;
        this.sender = sender;
        this.senderColour = senderColor;
    }

    public Message(String text, int sender) {
        this.text = text;
        this.sender = sender;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSender() {
        return Integer.toString(sender);
    }

    public void setSender(int sender) {
        this.sender = sender;
    }

    public String getSenderColour() {
        return senderColour;
    }

    public void setSenderColour(String senderColour) {
        this.senderColour = senderColour;
    }
}