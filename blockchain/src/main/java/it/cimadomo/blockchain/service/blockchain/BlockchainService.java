package it.cimadomo.blockchain.service.blockchain;

import it.cimadomo.blockchain.model.Block;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Service
@Slf4j
@RequiredArgsConstructor
public class BlockchainService {

    private final List<Block> blockChain = new ArrayList<>();
    private final Integer DIFFICULTY = 5;
    private final String MINING_REWARD = "10";
    private final MempoolService mempoolService;

    @Value("${blockchain.genesis-node}")
    private boolean isGenesisNode;

    @PostConstruct
    public void initializeBlockchain() {
        if (isGenesisNode) {
            initBlockchain();
        } else {
            log.info("Node started as a regular node. Waiting to sync with peers...");
        }
    }

    public void setBlockchain(List<Block> blocks){
        blockChain.addAll(blocks);
    }

    public Block getLatestBlock(){
        return blockChain.getLast();
    }

    public boolean isBlockValid(Block block, Block previousBlock){
        if (!block.getPreviousHash().equals(previousBlock.getHash())) {
            log.error("Current block's previous hash is not equal to the previous block's hash");
            return false;
        }
        String hashRetrieved = block.getHash();
        String hashCalculated = block.retrieveHash();
        if (!hashCalculated.equals(hashRetrieved)) {
            log.error("Current block's hash is not equal to the calculated hash");
            return false;
        }
        String prefix = "0".repeat(block.getDifficulty());
        if(!block.getHash().startsWith(prefix)){
            log.error("Block hash does not meet the difficulty requirement");
            return false;
        }
        return true;
    }

    public boolean addBlock(Block block){
        blockChain.add(block);
        if(!checkChainValidity()){
            log.error("Blockchain is not valid");
            blockChain.remove(block);
            return false;
        }
        mempoolService.removeTransactions(block.getTransactions());
        log.info("Transactions removed: {}", block.getTransactions());
        return true;
    }

    private boolean checkChainValidity(){
        for(int i = 1; i < blockChain.size(); i++){
            Block currentBlock = blockChain.get(i);
            Block previousBlock = blockChain.get(i - 1);
            if(currentBlock.getHash() == null || previousBlock.getHash() == null){
                log.error("Block hash is null");
                return false;
            }
            if(!isBlockValid(currentBlock, previousBlock)){
                return false;
            }

        }
        log.info("Blockchain is valid");
        return true;
    }

    public String getReward(){
        return MINING_REWARD;
    }

    public void initBlockchain() {
        Block genesisBlock = Block.builder()
                .index(0)
                .timestamp(Instant.now().toString())
                .previousHash("0")
                .nonce(0)
                .difficulty(DIFFICULTY)
                .hash("0")
                .transactions(new ArrayList<>())
                .build();
        genesisBlock.setHash(genesisBlock.retrieveHash());
        blockChain.add(genesisBlock);
        log.info("Genesis block added to the blockchain: {}", genesisBlock);
    }
}