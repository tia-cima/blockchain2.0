package it.cimadomo.miner.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.cimadomo.miner.model.Transaction;
import it.cimadomo.miner.model.protocol.Message;
import it.cimadomo.miner.model.protocol.MessageType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
@Getter
public class MempoolService {

    private final RequestService requestService;
    private final List<Transaction> transactions = new ArrayList<>();

    public void queryTransactions() {
        transactions.clear();
        try {
            CompletableFuture<String> responseFuture = requestService.sendRequest(MessageType.REQUEST_MEMPOOL, null);
            String response = responseFuture.get();
            Message receivedMessage = new ObjectMapper().readValue(response, Message.class);
            if (receivedMessage.getType() == MessageType.TRANSACTION) {
                List<Transaction> fetchedTransactions = new ObjectMapper().readValue(
                        receivedMessage.getPayload(),
                        new ObjectMapper().getTypeFactory().constructCollectionType(List.class, Transaction.class)
                );
                transactions.addAll(fetchedTransactions);
                log.debug("Fetched {} transactions from the server.", fetchedTransactions.size());
            } else {
                log.warn("Unexpected response type: {}", receivedMessage.getType());
            }
        } catch (Exception e) {
            log.error("Failed to query transactions from the server.", e);
        }
    }

    public boolean hasTransactions() {
        return !transactions.isEmpty();
    }
}
