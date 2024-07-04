package com.example.budget;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

// Main Application Class
@SpringBootApplication
public class BudgetApplication {
    public static void main(String[] args) {
        SpringApplication.run(BudgetApplication.class, args);
    }
}

// Entity Class
@Entity
class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String product;
    private String type; // SELL or BUY
    private double amount;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}

// Repository Interface
@Repository
interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByProductContainingAndTypeContainingAndAmountBetween(
            String product, String type, double minAmount, double maxAmount);
}

// Service Class
@Service
class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public List<Transaction> getAllTransactions(String product, String type, Double minAmount, Double maxAmount) {
        return transactionRepository.findByProductContainingAndTypeContainingAndAmountBetween(
                product, type, minAmount, maxAmount);
    }

    public Transaction getTransactionById(Long id) {
        return transactionRepository.findById(id).orElse(null);
    }

    public Transaction addTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    public Transaction updateTransaction(Long id, Transaction transaction) {
        transaction.setId(id);
        return transactionRepository.save(transaction);
    }

    public void deleteTransaction(Long id) {
        transactionRepository.deleteById(id);
    }

    public Map<String, List<Transaction>> getTransactionsByType() {
        return transactionRepository.findAll().stream()
                .collect(Collectors.groupingBy(Transaction::getType));
    }

    public Map<String, List<Transaction>> getTransactionsByProduct() {
        return transactionRepository.findAll().stream()
                .collect(Collectors.groupingBy(Transaction::getProduct));
    }
}

// Controller Class
@RestController
@RequestMapping("/transactions")
class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public List<Transaction> getAllTransactions(
            @RequestParam(required = false, defaultValue = "") String product,
            @RequestParam(required = false, defaultValue = "") String type,
            @RequestParam(required = false, defaultValue = "0") Double minAmount,
            @RequestParam(required = false, defaultValue = "1000000") Double maxAmount) {
        return transactionService.getAllTransactions(product, type, minAmount, maxAmount);
    }

    @GetMapping("/{id}")
    public Transaction getTransactionById(@PathVariable Long id) {
        return transactionService.getTransactionById(id);
    }

    @PostMapping
    public Transaction addTransaction(@RequestBody Transaction transaction) {
        return transactionService.addTransaction(transaction);
    }

    @PutMapping("/{id}")
    public Transaction updateTransaction(@PathVariable Long id, @RequestBody Transaction transaction) {
        return transactionService.updateTransaction(id, transaction);
    }

    @DeleteMapping("/{id}")
    public void deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
    }

    @GetMapping("/reports/type")
    public Map<String, List<Transaction>> getTransactionsByType() {
        return transactionService.getTransactionsByType();
    }

    @GetMapping("/reports/product")
    public Map<String, List<Transaction>> getTransactionsByProduct() {
        return transactionService.getTransactionsByProduct();
    }
}
