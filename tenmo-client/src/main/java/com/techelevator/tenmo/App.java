package com.techelevator.tenmo;

import com.techelevator.tenmo.model.*;
import com.techelevator.tenmo.services.AccountService;
import com.techelevator.tenmo.services.AuthenticationService;
import com.techelevator.tenmo.services.ConsoleService;
import com.techelevator.tenmo.services.TransferService;

import java.math.BigDecimal;

public class App {

    private static final String API_BASE_URL = "http://localhost:8080/";

    private final ConsoleService consoleService = new ConsoleService();
    private final AuthenticationService authenticationService = new AuthenticationService(API_BASE_URL);
    private final AccountService accountService = new AccountService(API_BASE_URL);
    private final TransferService transferService = new TransferService(API_BASE_URL);

    private AuthenticatedUser currentUser;

    public static void main(String[] args) {
        App app = new App();
        app.run();
    }

    private void run() {
        consoleService.printGreeting();
        loginMenu();
        if (currentUser != null) {
            mainMenu();
        }
    }

    private void loginMenu() {
        int menuSelection = -1;
        while (menuSelection != 0 && currentUser == null) {
            consoleService.printLoginMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                handleRegister();
            } else if (menuSelection == 2) {
                handleLogin();
            } else if (menuSelection != 0) {
                System.out.println("Invalid Selection");
                consoleService.pause();
            }
        }
    }

    private void handleRegister() {
        System.out.println("Please register a new user account");
        UserCredentials credentials = consoleService.promptForCredentials();
        if (authenticationService.register(credentials)) {
            System.out.println("Registration successful. You can now login.");
        } else {
            consoleService.printErrorMessage();
        }
    }

    private void handleLogin() {
        UserCredentials credentials = consoleService.promptForCredentials();
        currentUser = authenticationService.login(credentials);
        if (currentUser == null) {
            consoleService.printErrorMessage();
        } else {  // set token for authenticated user
            accountService.setAuthToken(currentUser.getToken());
            transferService.setAuthToken(currentUser.getToken());
        }
    }

    private void mainMenu() {
        int menuSelection = -1;
        while (menuSelection != 0) {
            consoleService.printMainMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                viewCurrentBalance();
            } else if (menuSelection == 2) {
                viewTransferHistory();
            } else if (menuSelection == 3) {
                viewPendingRequests();
            } else if (menuSelection == 4) {
                sendBucks();
            } else if (menuSelection == 5) {
//                requestBucks();
            } else if (menuSelection == 0) {
                continue;
            } else {
                System.out.println("Invalid Selection");
            }
            consoleService.pause();
        }
    }

    private void viewCurrentBalance() {

        // get account id
        long userId = currentUser.getUser().getId();
        Account account = accountService.getAccountByUserId(userId);

        // get account balance
        BigDecimal balance = accountService.getBalanceByAccountId(account.getAccount_id());
        System.out.println("Your current account balance is: $" + balance);
    }

    private void viewTransferHistory() {

        // get account
        long userId = currentUser.getUser().getId();
        Account account = accountService.getAccountByUserId(userId);
        long accountId = account.getAccount_id();

        // get transfers
        Transfer[] transfers = transferService.getTransfersByAccountId(accountId, 2);

        // Display approved transfers
        System.out.println("-------------------------------------------");
        System.out.println("Transfers");
        System.out.println("ID           From/To           Amount");
        System.out.println("-------------------------------------------");

        // Display each transfer - 23 From: Bernice $ 903.14
        for (int i = 0; i < transfers.length; i++) {
            System.out.print(transfers[i].getTransferId());
            long accountFromId = transfers[i].getAccountFrom();
            if (accountFromId == accountId) {
                long accountToId = transfers[i].getAccountTo();
                User otherUser = transferService.getUserIdByAccountID(accountToId);
                System.out.println("         To:   " + otherUser.getUsername() + "        $ " + transfers[i].getAmount());
            } else {
                User otherUser = transferService.getUserIdByAccountID(accountFromId);
                System.out.println("         From: " + otherUser.getUsername() + "        $ " + transfers[i].getAmount());
            }
        }

        // Allow user to enter transfer id to view details
        int transferId = consoleService.promptForInt("Please enter transfer ID to view details (0 to cancel): ");
        boolean validTransferId = false;
        if (transferId != 0) {
            for (int i = 0; i < transfers.length; i++) {
                if (transferId == transfers[i].getTransferId()) {
                    User userFrom = transferService.getUserIdByAccountID(transfers[i].getAccountFrom());
                    User userTo = transferService.getUserIdByAccountID(transfers[i].getAccountTo());
                    consoleService.printTransferDetails(transfers[i], userFrom.getUsername(),userTo.getUsername());
                    validTransferId = true;
                }
            }
            if (validTransferId == false) {
                System.out.println("You have entered an invalid transfer ID.");
            }
        }

    }

    private void viewPendingRequests() {
        // get account
        long userId = currentUser.getUser().getId();
        Account account = accountService.getAccountByUserId(userId);
        long accountId = account.getAccount_id();

        // get transfers
        Transfer[] transfers = transferService.getTransfersByAccountId(accountId, 1);

        // TODO: Display transfers

        // TODO: Allow user to select Ids to approve/reject

        // TODO: Will need to add a method in service + DAO + controller to update transaction
        // can use the consoleService.printTransferDetails(newTransfer, userFrom, userTo) method
    }

    private void sendBucks() {
        // Display list of potential users for transfer
        long currentUserId = currentUser.getUser().getId();
        User[] users = transferService.findUsersForTransfer(currentUserId);
        consoleService.printUsers(users);

        // Collect input from user
        long transferUserId = consoleService.promptForInt("Enter Id of user you are sending to (0 to cancel): ");
        try {
            Account otherAccount = accountService.getAccountByUserId(transferUserId);
            while (transferUserId == currentUserId || (otherAccount == null && transferUserId != 0)) {
                if (transferUserId == currentUserId) {
                    System.out.println("It is impossible to send money to yourself.");
                } else {
                    System.out.println("The ID you have entered is invalid.");
                }

                transferUserId = consoleService.promptForInt("Enter Id of user you are sending to (0 to cancel): ");
                otherAccount = accountService.getAccountByUserId(transferUserId);
            }

            if (transferUserId != 0) {

                // Get amount for transfer
                BigDecimal amount = consoleService.promptForBigDecimal("Enter amount: ");

                // Get account information for transfer
                long accountFrom = accountService.getAccountByUserId(currentUserId).getAccount_id();
                long accountTo = otherAccount.getAccount_id();

                // Check that 0 < amount <= account balance
                BigDecimal balance = accountService.getBalanceByAccountId(accountFrom);
                if (amount.compareTo(new BigDecimal(0)) <= 0 || amount.compareTo(balance) > 0) {
                    System.out.println("You have entered an invalid amount. The transfer amount must be greater than 0 " +
                            "and cannot be greater than your account balance");
                } else {

                    // Create transfer object
                    Transfer transfer = new Transfer();
                    transfer.setTransferTypeId((long) 2);          // Transfer is type send (2)
                    transfer.setTransferStatusID((long) 2);        // Send transfers automatically have status of approved (2)
                    transfer.setAccountFrom(accountFrom);          // Set account from
                    transfer.setAccountTo(accountTo);              // Set account to
                    transfer.setAmount(amount);                    // Set amount

                    // Call API to insert transfer into DB
                    Transfer newTransfer = transferService.createTransfer(transfer);

                    //Display Transfer
                    if (newTransfer == null) {
                        consoleService.printErrorMessage();
                    } else {

                        // Get usernames - this should probably be replaced by a method to get the username from the account ids
                        String userFrom = currentUser.getUser().getUsername();
                        String userTo = null;
                        for (int i = 0; i < users.length; i++) {
                            if (users[i].getId().equals(transferUserId)) {
                                userTo = users[i].getUsername();
                            }
                        }
                        consoleService.printTransferDetails(newTransfer, userFrom, userTo);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }



	private void requestBucks() {
		// TODO collect input from the user
            long currentUserId = currentUser.getUser().getId();
            User[] users = transferService.findUsersForTransfer(currentUserId);
            consoleService.printUsers(users);

        int transferUserId = consoleService.promptForInt("Enter Id of user you are requesting from (0 to cancel): ");
        while (transferUserId == currentUserId) {
            System.out.println("You cannot select to transfer money from yourself.");

            //get rid of this , you cannot request on someone's behalf.
            transferUserId = consoleService.promptForInt("Enter Id of user you are sending to (0 to cancel): ");
        }
        if (transferUserId != 0) {

            // Get amount for transfer
            BigDecimal amount = consoleService.promptForBigDecimal("Enter amount: ");
            // Get account information for transfer
            try {
                long accountFrom = accountService.getAccountByUserId(currentUserId).getAccount_id();
                long accountTo = accountService.getAccountByUserId(transferUserId).getAccount_id();
                // Check that 0 < amount <= account balance
                BigDecimal balance = accountService.getBalanceByAccountId(accountFrom);

                if (amount.compareTo(new BigDecimal(0)) <= 0 || amount.compareTo(balance) < 0) {
                    System.out.println("You have entered an invalid amount. The transfer amount must be greater than 0 " +
                            "and cannot be greater than their account balance");
                } else {
                    // Create transfer object
                    Transfer transfer = new Transfer();
                    transfer.setTransferTypeId((long) 2);          // Transfer is type send (2)
                    transfer.setTransferStatusID((long) 2);        // Send transfers automatically have status of approved (2)
                    transfer.setAccountFrom(accountFrom);          // Set account from
                    transfer.setAccountTo(accountTo);              // Set account to
                    transfer.setAmount(amount);                    // Set amount

                }
                // TODO call createTransfer method

            } catch(Exception e) {
                System.out.println(e.getMessage());
            }
        }
}
}
