package it.cimadomo.miner.dto;

import it.cimadomo.miner.model.Transaction;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class BlockPayloadDto {
    private Integer index;
    private String timestamp;
    private String previousHash;
    private String hash;
    private Integer nonce;
    private Integer difficulty;
    private List<Transaction> transactions;
}
