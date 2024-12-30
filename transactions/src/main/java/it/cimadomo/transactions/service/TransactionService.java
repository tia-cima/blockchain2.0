package it.cimadomo.transactions.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.cimadomo.transactions.config.TcpConfig;
import it.cimadomo.transactions.model.Transaction;
import it.cimadomo.transactions.model.protocol.MessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionService {

    private final RequestService requestService;
    private final ObjectMapper objectMapper;

    public void sendTransaction(String sender, String receiver, Double amount, String data) {
        Transaction transaction = Transaction.builder()
                .sender(sender)
                .recipient(receiver)
                .amount(amount)
                .data(data)
                .timestamp(Instant.now().toString())
                .build();
        String serializedPayload = null;
        try {
            serializedPayload = objectMapper.writeValueAsString(transaction);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        requestService.sendRequest(MessageType.TRANSACTION, serializedPayload);
        log.info("Sent transaction: {}", serializedPayload);
    }

}
