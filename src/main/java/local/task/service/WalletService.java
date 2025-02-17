package local.task.service;

import local.task.exceptions.NotEnoughBalanceException;
import local.task.exceptions.WalletHasNotFoundException;
import local.task.model.Wallet;
import local.task.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
public class WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Transactional(rollbackFor = {WalletHasNotFoundException.class, NotEnoughBalanceException.class})
    public void deposit(UUID walletId, BigDecimal amount) throws WalletHasNotFoundException {
        Optional<Wallet> optionalWallet = walletRepository.findByIdWithLock(walletId);
        if (optionalWallet.isPresent()) {
            Wallet wallet = optionalWallet.get();
            wallet.setBalance(wallet.getBalance().add(amount));
            walletRepository.save(wallet);
        } else throw new WalletHasNotFoundException("Wallet hasn't found!");
    }

    @Transactional(rollbackFor = {WalletHasNotFoundException.class, NotEnoughBalanceException.class})
    public void withdraw(UUID walletId, BigDecimal amount) throws WalletHasNotFoundException, NotEnoughBalanceException {
        Optional<Wallet> optionalWallet = walletRepository.findByIdWithLock(walletId);
        if (optionalWallet.isPresent()) {
            Wallet wallet = optionalWallet.get();
            if (wallet.getBalance().compareTo(amount) >= 0) {
                wallet.setBalance(wallet.getBalance().subtract(amount));
                walletRepository.save(wallet);
            } else throw new NotEnoughBalanceException("Not enough balance for this operation!");
        } else throw new WalletHasNotFoundException("Wallet hasn't found!");
    }

    @Transactional
    public BigDecimal getCurrentBalance(UUID walletId) throws WalletHasNotFoundException {
        Optional<Wallet> optionalWallet = walletRepository.findByIdWithLock(walletId);
        if (optionalWallet.isPresent()) {
            Wallet wallet = optionalWallet.get();
            return wallet.getBalance();
        } else throw new WalletHasNotFoundException("Wallet hasn't found!");
    }
}
