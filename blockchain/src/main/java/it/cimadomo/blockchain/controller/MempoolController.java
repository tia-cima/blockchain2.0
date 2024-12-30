package it.cimadomo.blockchain.controller;

import it.cimadomo.blockchain.model.Transaction;
import it.cimadomo.blockchain.service.blockchain.MempoolService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mempool")
@RequiredArgsConstructor
public class MempoolController {

    private final MempoolService mempoolService;

    @GetMapping
    public List<Transaction> getMempool() {
        return mempoolService.getMempool();
    }

    @PostMapping("/add")
    public String addTransaction(@RequestBody Transaction transaction) {
        mempoolService.addTransaction(transaction);
        return "Transaction added to mempool.";
    }

    @DeleteMapping("/clear")
    public String clearMempool() {
        mempoolService.clearMempool();
        return "Mempool cleared.";
    }

    @DeleteMapping("/remove")
    public String removeTransactions(@RequestBody List<Transaction> transactions) {
        mempoolService.removeTransactions(transactions);
        return "Transactions removed from mempool.";
    }
}
