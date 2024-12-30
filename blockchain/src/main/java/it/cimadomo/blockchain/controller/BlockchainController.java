package it.cimadomo.blockchain.controller;

import it.cimadomo.blockchain.model.Block;
import it.cimadomo.blockchain.service.blockchain.BlockchainService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/blockchain")
@RequiredArgsConstructor
public class BlockchainController {

    private final BlockchainService blockchainService;

    @GetMapping("/latest")
    public Block getLatestBlock() {
        return blockchainService.getLatestBlock();
    }

    @GetMapping
    public List<Block> getBlockchain() {
        return blockchainService.getBlockChain();
    }
}
