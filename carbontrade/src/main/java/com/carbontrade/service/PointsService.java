package com.carbontrade.service;

import com.carbontrade.model.PointsTransaction;
import com.carbontrade.model.PointsWallet;
import com.carbontrade.repository.PointsTransactionRepository;
import com.carbontrade.repository.PointsWalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PointsService {

    public static final int POINTS_PER_COIN = 50;

    private final PointsWalletRepository walletRepo;
    private final PointsTransactionRepository txnRepo;

    public PointsService(PointsWalletRepository walletRepo, PointsTransactionRepository txnRepo) {
        this.walletRepo = walletRepo;
        this.txnRepo = txnRepo;
    }

    @Transactional
    public PointsWallet earnPoints(Long userId, long amount, String reason) {
        if (amount <= 0) throw new IllegalArgumentException("amount must be > 0");

        PointsWallet wallet = walletRepo.findByUserId(userId).orElseGet(() -> {
            PointsWallet w = new PointsWallet(userId);
            return walletRepo.save(w);
        });

        // 1) earn
        wallet.setPoints(wallet.getPoints() + amount);
        txnRepo.save(new PointsTransaction(userId, PointsTransaction.Kind.EARN, amount, 0, reason));

        // 2) auto-convert
        long k = wallet.getPoints() / POINTS_PER_COIN; // integer division
        if (k > 0) {
            long pointsToConsume = k * POINTS_PER_COIN;
            wallet.setPoints(wallet.getPoints() - pointsToConsume);
            wallet.setCoins(wallet.getCoins() + k);
            txnRepo.save(new PointsTransaction(userId, PointsTransaction.Kind.AUTO_CONVERT, -pointsToConsume, k, "auto_convert"));
        }

        return walletRepo.save(wallet);
    }

    @Transactional(readOnly = true)
    public PointsWallet getBalance(Long userId) {
        return walletRepo.findByUserId(userId).orElseGet(() -> new PointsWallet(userId));
    }
}
