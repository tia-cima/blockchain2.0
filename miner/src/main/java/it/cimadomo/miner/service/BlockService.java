package it.cimadomo.miner.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.cimadomo.miner.dto.BlockPayloadDto;
import it.cimadomo.miner.model.Block;
import it.cimadomo.miner.model.protocol.Message;
import it.cimadomo.miner.model.protocol.MessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class BlockService {

    private final RequestService requestService;
    private final ObjectMapper objectMapper;
    private final CreditService creditService;

    public CompletableFuture<Block> requestLatestBlock() {
        CompletableFuture<Block> blockFuture = new CompletableFuture<>();
        try {
            CompletableFuture<String> responseFuture = requestService.sendRequest(MessageType.REQUEST_LATEST, null);
            responseFuture.thenAccept(response -> {
                try {
                    Message receivedMessage = objectMapper.readValue(response, Message.class);
                    if (receivedMessage.getType() == MessageType.BLOCK) {
                        Block latestBlock = objectMapper.readValue(receivedMessage.getPayload(), Block.class);
                        blockFuture.complete(latestBlock);
                        log.debug("Latest block retrieved: {}", latestBlock);
                    } else {
                        log.warn("Unexpected response type for latest block: {}", receivedMessage.getType());
                        blockFuture.completeExceptionally(
                                new IllegalArgumentException("Unexpected response type: " + receivedMessage.getType())
                        );
                    }
                } catch (Exception e) {
                    log.error("Failed to parse latest block response", e);
                    blockFuture.completeExceptionally(e);
                }
            }).exceptionally(ex -> {
                blockFuture.completeExceptionally(ex);
                return null;
            });
        } catch (Exception e) {
            log.error("Failed to send REQUEST_LATEST", e);
            blockFuture.completeExceptionally(e);
        }

        return blockFuture;
    }

    public boolean submitBlock(Block block) {
        try {
            BlockPayloadDto blockPayloadDto = BlockPayloadDto.builder()
                    .index(block.getIndex())
                    .timestamp(block.getTimestamp())
                    .previousHash(block.getPreviousHash())
                    .hash(block.getHash())
                    .nonce(block.getNonce())
                    .difficulty(block.getDifficulty())
                    .transactions(block.getTransactions())
                    .build();
            String jsonPayload = objectMapper.writeValueAsString(blockPayloadDto);
            log.debug("Submitting block: {}", jsonPayload);
            log.debug("JSON payload size: {} bytes", jsonPayload.length());
            CompletableFuture<String> responseFuture = requestService.sendRequest(MessageType.BLOCK, jsonPayload);
            String response = responseFuture.get();
            Message receivedMessage = new ObjectMapper().readValue(response, Message.class);
            if(receivedMessage != null && receivedMessage.getType().equals(MessageType.REWARD)){
                creditService.addCredits(Long.parseLong(receivedMessage.getPayload()));
                
                return true;
            } else {
                log.warn("Error submitting block: {}", receivedMessage);
                return false;
            }

        } catch (Exception e) {
            log.error("Failed to submit block", e);
            return false;
        }
    }
}
