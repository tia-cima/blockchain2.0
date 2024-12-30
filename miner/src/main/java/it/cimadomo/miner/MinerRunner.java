package it.cimadomo.miner;

import it.cimadomo.miner.model.Block;
import it.cimadomo.miner.model.Transaction;
import it.cimadomo.miner.service.BlockService;
import it.cimadomo.miner.service.CreditService;
import it.cimadomo.miner.service.MempoolService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
@Component
@Slf4j
public class MinerRunner implements CommandLineRunner {

    private final MempoolService mempoolService;
    private final BlockService blockService;
    private final CreditService creditService;
    private volatile boolean isRunning = true;

    @Override
    public void run(String... args) throws Exception {
        while (isRunning) {
            log.info("Started mining...");
            mempoolService.queryTransactions();
            if(!mempoolService.hasTransactions()){
                log.warn("No transactions available. Sleeping...");
                Thread.sleep(1000);
                continue;
            }
            List<Transaction> transaction = mempoolService.getTransactions();
            CompletableFuture<Block> blockFuture = blockService.requestLatestBlock();
            Block latestBlock = blockFuture.get();
            if(latestBlock == null){
                log.warn("Latest block is null. Sleeping...");
                Thread.sleep(1000);
                continue;
            }
            Integer difficulty = latestBlock.getDifficulty();
            Integer index = latestBlock.getIndex() + 1;
            Integer nonce = 0;
            String prefix = "0".repeat(difficulty);
            String previousHash = latestBlock.getHash();
            String timestamp = Instant.now().toString();

            long startTime = System.currentTimeMillis();
            while (true){
                StringBuilder transactions = new StringBuilder();
                for(Transaction tx : transaction){
                    transactions.append(tx.getSender())
                            .append(tx.getRecipient())
                            .append(tx.getAmount())
                            .append(tx.getData())
                            .append(tx.getTimestamp());
                }
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hashBytes = digest.digest((index + timestamp + transactions + previousHash + nonce + difficulty)
                        .getBytes(StandardCharsets.UTF_8));
                StringBuilder hexString = new StringBuilder();
                for (byte b : hashBytes) {
                    String hex = Integer.toHexString(0xff & b);
                    if (hex.length() == 1) hexString.append('0');
                    hexString.append(hex);
                }
                String hash = hexString.toString();
                log.debug("Hash: {}", hash);
                if(hash.startsWith(prefix)){
                    log.debug("Block found! Nonce: {}", nonce);
                    if (blockService.submitBlock(Block.builder()
                            .index(index)
                            .timestamp(timestamp)
                            .previousHash(previousHash)
                            .hash(hash)
                            .nonce(nonce)
                            .difficulty(difficulty)
                            .transactions(transaction)
                            .build()
                    )){
                        log.info("New block mined! You have been rewarded with credits, new amount: {}", creditService.getCredits());
                    } else {
                        log.warn("Block submission failed!");
                    }
                    break;
                }
                nonce++;
                long elapsedTime = System.currentTimeMillis() - startTime;
                if (elapsedTime >= 10000) {
                    log.info("Still no solution found");
                    startTime = System.currentTimeMillis();
                }
            }
            Thread.sleep(100);
        }
    }
}
