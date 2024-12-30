package it.cimadomo.miner.service;

import lombok.Getter;
import org.springframework.stereotype.Service;

@Service
@Getter
public class CreditService {
    private long credits;

    public CreditService() {
        credits = 0;
    }

    public void addCredits(long credits) {
        this.credits += credits;
    }
}
