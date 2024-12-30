package it.cimadomo.miner.model.protocol;

public enum MessageType {
    TRANSACTION,
    BLOCK,
    REQUEST_LATEST,
    REQUEST_MEMPOOL,
    REWARD,
    ERROR
}