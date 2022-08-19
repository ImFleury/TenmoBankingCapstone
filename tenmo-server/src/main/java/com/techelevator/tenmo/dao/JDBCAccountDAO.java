package com.techelevator.tenmo.dao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.techelevator.tenmo.model.ShareableUser;
import com.techelevator.tenmo.model.Transfer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import com.techelevator.tenmo.model.Account;

@Component
public class JDBCAccountDAO implements AccountDAO {

    private JdbcTemplate jdbcTemplate;

    public JDBCAccountDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Method to get account using the account id
    @Override
    public Account getAccountByAccountId(long account_id) {
        String sql = "SELECT * FROM account WHERE account_id = ?";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, account_id);

        if (results.next()) {
            return mapRowToAccount(results);
        }
        return null;
    }

    // Method to get account using the user id
    @Override
    public Account getAccountByUserId(long user_id) {
        String sql = "SELECT * FROM account WHERE user_id = ?";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, user_id);

        if (results.next()) {
            return mapRowToAccount(results);
        }
        return null;
    }

    // Method to get account balance using the account id
    @Override
    public BigDecimal getAccountBalanceByAccountId(long account_id) {
        String sql = "SELECT balance FROM account WHERE account_id = ?";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, account_id);

        BigDecimal balance = null;
        if (results.next()) {
            balance = results.getBigDecimal("balance");
        }
        return balance;
    }

    // Method to update the account balance using the account id
    @Override
    public Account updateAccountBalanceByAccountId(BigDecimal balance, long account_id) {
        String sql = "UPDATE account SET balance = ? WHERE account_id = ?";
        jdbcTemplate.update(sql, balance, account_id);
        return getAccountByUserId(account_id);
    }

    // Method to get list TE individuals that the user can select for a transaction
    @Override
    public ShareableUser[] findUsersForTransfer(long user_id) {
        List<ShareableUser> users = new ArrayList<>();
        String sql = "SELECT user_id, username FROM tenmo_user WHERE user_id <> ?;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, user_id);
        while (results.next()) {
            users.add(mapRowToShareableUser(results));
        }

        return users.toArray(new ShareableUser[users.size()]);
    }

    // Method to get a transfer from the transfer ID
    @Override
    public Transfer getTransferByTransferId(long transferId) {
        String sql = "SELECT * FROM transfer WHERE transfer_id = ?";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, transferId);
        if (results.next()) {
            return mapRowToTransfer(results);
        }
        return null;
    }

    // Method to create a transfer record
    @Override
    public Transfer createTransfer(Transfer transfer) {
        String sql = "INSERT INTO transfer (transfer_type_id, transfer_status_id, account_from, account_to, amount) " +
                "VALUES (?, ?, ?, ?, ?) RETURNING transfer_id;";
        Long newId = jdbcTemplate.queryForObject(sql, Long.class,
                transfer.getTransferTypeId(), transfer.getTransferStatusID(), transfer.getAccountFrom(),
                transfer.getAccountTo(), transfer.getAmount());
        return getTransferByTransferId(newId);
    }

    // Method to select all transactions to or from for a user
    @Override
    public Transfer[] getTransfersByAccountId(long accountId, long transferStatusType) {
        List<Transfer> transfers = new ArrayList<>();
        String sql = "SELECT * FROM transfer WHERE transfer_status_id = ? AND (account_from = ? OR account_to = ?);";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, transferStatusType, accountId, accountId);
        while (results.next()) {
            transfers.add(mapRowToTransfer(results));
        }

        return transfers.toArray(new Transfer[transfers.size()]);
    }

     // Method to find user from the account id
    @Override
    public ShareableUser getUserIdByAccountID(long accountId) {
        String sql = "SELECT tenmo_user.user_id, username FROM tenmo_user JOIN account ON tenmo_user.user_id = account.user_id WHERE account.account_id = ?";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, accountId);

        if (results.next()) {
            return mapRowToShareableUser(results);
        }
        return null;
    }


    // Method to map a row in the account DB table to the Account object
    private Account mapRowToAccount(SqlRowSet results) {
        Account account = new Account();
        account.setAccount_id(results.getLong("account_id"));
        account.setBalance(results.getBigDecimal("balance"));
        account.setUser_id(results.getLong("user_id"));
        return account;
    }

    // Method to map a row in the transfer DB table to the Transfer object
    private Transfer mapRowToTransfer(SqlRowSet results) {
        Transfer transfer = new Transfer();
        transfer.setTransferId(results.getLong("transfer_id"));
        transfer.setTransferTypeId(results.getLong("transfer_type_id"));
        transfer.setTransferStatusID(results.getLong("transfer_status_id"));
        transfer.setAccountFrom(results.getLong("account_from"));
        transfer.setAccountTo(results.getLong("account_to"));
        transfer.setAmount(results.getBigDecimal("amount"));
        return transfer;
    }

    // Method to map a row in the user DB table to the ShareableUser object
    private ShareableUser mapRowToShareableUser(SqlRowSet results) {
        ShareableUser user = new ShareableUser();
        user.setId(results.getLong("user_id"));
        user.setUsername(results.getString("username"));
        return user;
    }

}