package jasperwiese.dev.mybank.service;

import jasperwiese.dev.mybank.model.Transaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
@Service
public class TransactionService {
    private static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();

    private final String bankSlogan;

    private final JdbcTemplate jdbcTemplate;

    public TransactionService(@Value("${bank.slogan}")String bankSlogan, JdbcTemplate jdbcTemplate) {
        this.bankSlogan = bankSlogan;
        this.jdbcTemplate = jdbcTemplate;
    }
    @Transactional
    public List<Transaction> findAll() {
        /*System.out.println("hit findall");
        return jdbcTemplate.query("SELECT * FROM TRANSACTIONS",
                TransactionService::mapRow);*/
        System.out.println("Executing findAll method");
        try {
            List<Transaction> transactions = jdbcTemplate.query("SELECT * FROM TRANSACTIONS",
                    TransactionService::mapRow);
            System.out.println("Found " + transactions.size() + " transactions");
            return transactions;
        } catch (Exception e) {
            System.err.println("Error in findAll method: " + e.getMessage());
            e.printStackTrace();
            throw e; // Rethrow the exception to see the full stack trace in logs
        }
    }
    @Transactional
    public List<Transaction> findByIncomingId(String userId) {
        return jdbcTemplate.query(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT tx.id, tx.amount, tx.timestamp, tx.reference, tx.bank_slogan, tx.input_user FROM TRANSACTIONS tx WHERE tx.receiving_user = ?");
            ps.setString(1, userId);
            return ps;
        }, TransactionService::mapRow);
    }
    @Transactional
    public Transaction create(BigDecimal amount, String reference){
        ZonedDateTime timestamp = LocalDateTime.now().atZone(DEFAULT_ZONE);
        Transaction transaction = new Transaction(amount,timestamp,reference, bankSlogan);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO TRANSACTIONS (amount, timestamp, reference, bank_slogan) VALUES ( ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setBigDecimal(1, transaction.getAmount());
            ps.setTimestamp(2, Timestamp.valueOf(transaction.getTimestamp().toLocalDateTime()));
            ps.setString(3, transaction.getReference());
            ps.setString(4, transaction.getBankSlogan());
            return ps;
        }, keyHolder);
        String uuid = !keyHolder.getKeys().isEmpty() ? keyHolder.getKeys().values().iterator().next().toString() : null;
        transaction.setId(uuid);
        return transaction;
    }
    @Transactional
    public Transaction createFromInput(BigDecimal amount, String reference, String inputUser) {
        ZonedDateTime timestamp = LocalDateTime.now().atZone(DEFAULT_ZONE);
        Transaction transaction = new Transaction(amount, timestamp, reference, bankSlogan, inputUser);

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO TRANSACTIONS (amount, timestamp, reference, bank_slogan, input_user) VALUES (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setBigDecimal(1, transaction.getAmount());
            ps.setTimestamp(2, Timestamp.valueOf(transaction.getTimestamp().toLocalDateTime()));
            ps.setString(3, transaction.getReference());
            ps.setString(4, transaction.getBankSlogan());
            ps.setString(5, transaction.getInputUser());
            return ps;
        }, keyHolder);

        String uuid = !keyHolder.getKeys().isEmpty() ? keyHolder.getKeys().values().iterator().next().toString() : null;
        transaction.setId(uuid);
        return transaction;
    }

    private static Transaction mapRow(ResultSet rs, int rowNum) throws SQLException {
        Transaction tx = new Transaction();
        tx.setId(rs.getObject("id").toString());
        tx.setAmount(rs.getBigDecimal("amount"));
        tx.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime().atZone(DEFAULT_ZONE));
        tx.setReference(rs.getString("reference"));
        tx.setBankSlogan(rs.getString("bank_slogan"));
        tx.setInputUser(rs.getString("input_user"));
        return tx;
    }
}
