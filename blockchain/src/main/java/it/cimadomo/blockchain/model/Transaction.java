package it.cimadomo.blockchain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    private String sender;
    private String recipient;
    private Double amount;
    private String data;
    private String timestamp;

    @Override
    public String toString() {
        return sender +
                recipient +
                amount +
                (data != null ? data : "None") +
                timestamp;
    }

}