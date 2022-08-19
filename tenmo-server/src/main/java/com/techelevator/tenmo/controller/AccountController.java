package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.model.ShareableUser;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.techelevator.tenmo.dao.AccountDAO;
import com.techelevator.tenmo.model.Account;

import javax.validation.Valid;
import java.math.BigDecimal;

@RestController
//@PreAuthorize("isAuthenticated()")
public class AccountController {

    // Instance variables
    private AccountDAO accountDao;

    // Constructor
    public AccountController(AccountDAO accountDao) {
        this.accountDao = accountDao;
    }

    // Get account using the account ID
    @RequestMapping(path = "/accounts/{accountId}", method = RequestMethod.GET)
    public Account getAccountByAccountId(@PathVariable long accountId) {
        return accountDao.getAccountByAccountId(accountId);
    }

    // Get account using the user ID
    @RequestMapping(path = "/users/{userId}/account", method = RequestMethod.GET)
    public Account list(@PathVariable long userId) {
        return accountDao.getAccountByUserId(userId);
    }

    // Get account balance using the account ID
    @RequestMapping(path = "/accounts/{accountId}/balance", method = RequestMethod.GET)
    public BigDecimal getAccountBalanceByAccountId(@PathVariable long accountId) {
        return accountDao.getAccountBalanceByAccountId(accountId);
    }

    // Update account balance using the account ID
    @RequestMapping(path = "/accounts/{accountId}/balance", method = RequestMethod.PUT)
    public Account updateAccountBalanceByAccountId(@PathVariable long accountId,
                                                   @RequestParam(required=true) BigDecimal newBalance) {
        return accountDao.updateAccountBalanceByAccountId(newBalance, accountId);
    }

    // Method to get list TE individuals that the user can select for a transaction
    @RequestMapping(path = "/users/{userId}/potentialTransferUsers", method = RequestMethod.GET)
    public ShareableUser[] findUsersForTransfer(@PathVariable long userId) {
        return accountDao.findUsersForTransfer(userId);
    }

    // Get transfer from the transaction ID
    @RequestMapping(path = "/transfers/{transferId}", method = RequestMethod.GET)
    public Transfer getTransferByTransferId(@PathVariable long transferId) {
        return accountDao.getTransferByTransferId(transferId);
    }

    // Method to find user from the account id
    @RequestMapping(path = "/accounts/{accountId}/user" , method = RequestMethod.GET)
    public ShareableUser getUserIdByAccountID(@PathVariable long accountId) {
        return accountDao.getUserIdByAccountID(accountId);
    }

    // Create a transfer record
    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(path = "/transfers", method = RequestMethod.POST)
    public Transfer createTransfer(@Valid @RequestBody Transfer transfer) {
        Transfer newTransfer = accountDao.createTransfer(transfer);

        // check if transfer is approved
        if (newTransfer.getTransferStatusID() == 2) {
            BigDecimal amount = newTransfer.getAmount();

            // Get accounts in the transaction
            Account accountFrom = accountDao.getAccountByAccountId(newTransfer.getAccountFrom());
            Account accountTo = accountDao.getAccountByAccountId(newTransfer.getAccountTo());

            // check transaction type
            if (newTransfer.getTransferTypeId() == 1) {     // Request transfer
                // Add money from accountFrom and deduct money from accountTo
                accountDao.updateAccountBalanceByAccountId(accountFrom.getBalance().add(amount), accountFrom.getAccount_id());
                accountDao.updateAccountBalanceByAccountId(accountTo.getBalance().subtract(amount), accountTo.getAccount_id());
            } else {                                        // Send transfer
                // Deduct money to accountFrom and add money from accountTo
                accountDao.updateAccountBalanceByAccountId(accountFrom.getBalance().subtract(amount), accountFrom.getAccount_id());
                accountDao.updateAccountBalanceByAccountId(accountTo.getBalance().add(amount), accountTo.getAccount_id());
            }
        }
        return newTransfer;
    }

    // Get transactions by account ID
    @RequestMapping(path = "/accounts/{accountId}/transfers", method = RequestMethod.GET)
    public Transfer[] getTransfersByAccountId(@PathVariable long accountId, @RequestParam(required=true) Long transferStatusType) {
        return accountDao.getTransfersByAccountId(accountId, transferStatusType);
    }

}