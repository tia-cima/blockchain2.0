package it.cimadomo.miner.model.protocol;

import lombok.Data;

@Data
public class Message {
    private MessageType type;
    private String payload;
}
