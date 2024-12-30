package it.cimadomo.transactions.model.protocol;

public enum MessageType {
    TRANSACTION,
    BLOCK,
    REQUEST_LATEST,
    REQUEST_MEMPOOL,
    REWARD,
    ERROR
}