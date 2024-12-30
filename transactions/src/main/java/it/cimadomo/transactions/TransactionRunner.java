package it.cimadomo.transactions;

import it.cimadomo.transactions.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Random;

@RequiredArgsConstructor
@Component
@Slf4j
public class TransactionRunner implements CommandLineRunner {
    
    private final TransactionService transactionService;
    private volatile boolean isRunning = true;

    @Override
    public void run(String... args) throws Exception {
        while (isRunning){
            String sender = generateRandomString(Math.random() * 10);
            String receiver = generateRandomString(Math.random() * 10);
            Double amount = Math.round(Math.random() * 1000 * 100.0) / 100.0;
            String data = generateRandomString(Math.random() * 10);
            transactionService.sendTransaction(sender, receiver, amount, data);
            Thread.sleep(1000);
        }
    }

    private String generateRandomString(Double length) {
        Random random = new Random();
        StringBuilder builder = new StringBuilder();
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        for (int i = 0; i < length; i++) {
            char randomChar = characters.charAt(random.nextInt(characters.length()));
            builder.append(randomChar);
        }
        return builder.toString();
    }
}
