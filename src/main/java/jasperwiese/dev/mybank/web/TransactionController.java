package jasperwiese.dev.mybank.web;

import jakarta.validation.Valid;
import jasperwiese.dev.mybank.dto.TransactionDto;
import jasperwiese.dev.mybank.model.Transaction;
import jasperwiese.dev.mybank.service.TransactionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@RestController
public class TransactionController {
    private TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/transactions")
    public List<Transaction> getTransactions(){
        return transactionService.findAll();
    }

    @PostMapping("/transaction")
    public Transaction createTransaction(@RequestBody @Valid TransactionDto transactionDto) {
        return transactionService.create(transactionDto.getAmount(), transactionDto.getReference());
    }
}
