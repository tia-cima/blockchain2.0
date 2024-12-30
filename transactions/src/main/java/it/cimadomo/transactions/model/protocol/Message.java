package it.cimadomo.transactions.model.protocol;

import lombok.Data;

@Data
public class Message {
    private MessageType type;
    private String payload;
}
