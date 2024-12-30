package it.cimadomo.blockchain.service.blockchain;

import it.cimadomo.blockchain.model.Transaction;
import org.springframework.stereotype.Service;
import java.util.Random;

import java.util.ArrayList;
import java.util.List;

@Service
public class MempoolService {

    private final List<Transaction> transactions = new ArrayList<>();
    private final Integer MAX_TRANSACTIONS = 4;

    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
    }

    public List<Transaction> getMempool() {
        Random random = new Random();
        List<Transaction> selectedTransactions = new ArrayList<>();
        List<Transaction> tempTransactions = new ArrayList<>(transactions);
        for (int i = 0; i < MAX_TRANSACTIONS && !tempTransactions.isEmpty(); i++) {
            selectedTransactions.add(tempTransactions.remove(random.nextInt(tempTransactions.size())));
        }
        transactions.removeAll(selectedTransactions);
        return selectedTransactions;
    }

    public void removeTransactions(List<Transaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return;
        }
        transactions.forEach(this.transactions::remove);
    }

    public void clearMempool() {
        transactions.clear();
    }

    public boolean isTransactionValid(Transaction transaction) {
        return true;
    }
}
