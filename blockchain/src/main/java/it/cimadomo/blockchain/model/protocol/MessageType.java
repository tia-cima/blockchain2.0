package it.cimadomo.blockchain.model.protocol;

public enum MessageType {
    TRANSACTION,
    BLOCK,
    REQUEST_BLOCKCHAIN,
    REQUEST_LATEST,
    REQUEST_MEMPOOL,
    REWARD,
    ERROR
}