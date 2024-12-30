package it.cimadomo.blockchain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BlockchainApplication {
    // TODO validate transactions
    // TODO persistence of the blockchain
    public static void main(String[] args) {
        SpringApplication.run(BlockchainApplication.class, args);
    }

}
