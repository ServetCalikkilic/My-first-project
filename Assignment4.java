/**
 * @author Servet Çalikkılıç 20230808047
 * @date 13.12.2024
 * @version 4.0
 */

import java.io.*;
import java.util.Scanner;
import java.io.FileWriter;

public class Assignment4 {
    public static void main(String[] args) throws IOException {
        String AccountInfo = args[0] + "_AccountInfo.txt";
        String BillPay = args[0] + "_BillPay.txt";
        String TransferInfo = args[0] + "_TransferInfo.txt";
        String Log = args[0] + ".log";
        String AccountInfoOut = args[0] + "_AccountInfoOut.txt";
        int numOfAccounts = countAccounts(AccountInfo);
        int[] acctNums = new int[numOfAccounts];
        String[] names = new String[numOfAccounts];
        String[] surnames = new String[numOfAccounts];
        double[] balances = new double[numOfAccounts];
        readAccountInfo(acctNums, names, surnames, balances, AccountInfo);

        Scanner transferReader = new Scanner(new File(TransferInfo));
        String[] transferNum = new String[6];
        int[] acctNumFrom = new int[4];
        int[] acctNumTo = new int[4];
        double[] amount = new double[4];
        for(int i = 0; transferReader.hasNext(); i++){
            transferNum[i] = transferReader.next();
            acctNumFrom[i] = transferReader.nextInt();
            acctNumTo[i] = transferReader.nextInt();
            amount[i] = Double.parseDouble(transferReader.next());
            transfer(acctNums, balances, acctNumFrom[i], acctNumTo[i], amount[i]);
        }

        Scanner billReader = new Scanner(new File(BillPay));
        int[] acctNumOfBill = new int[3];
        double[] amountOfBill = new double[3];
        int index;
        for(int i = 0; billReader.hasNext(); i++){
            transferNum[i+3] = billReader.next();
            acctNumOfBill[i] = billReader.nextInt();
            billReader.next();
            amountOfBill[i] = Double.parseDouble(billReader.next());
            index = findAcct(acctNums, acctNumOfBill[i]);
            if(index != -1){
                withdrawal(balances, index, amountOfBill[i]);
            }
        }
        writeAccountInfo(acctNums, names, surnames, balances, AccountInfoOut);
        FileWriter logOutput = new FileWriter(Log);
        for (int i = 0; i < 3; i++){
            logOutput.write("Transfer " + transferNum[i] +
                    " resulted in code " + transfer(acctNums, balances, acctNumFrom[i], acctNumTo[i], amount[i])
                    + description(transfer(acctNums, balances, acctNumFrom[i], acctNumTo[i], amount[i])));
        }
        for (int i = 3; i < 5; i++){
            logOutput.write("Bill Pay " + transferNum[i] + " resulted in code " + billCode(acctNums, balances, acctNumOfBill[i-3], amountOfBill[i-3]) + billMessage(billCode(acctNums, balances, acctNumOfBill[i-3], amountOfBill[i-3])));
        }
        logOutput.close();
    }
    public static int countAccounts(String filename) throws IOException {
        Scanner input = new Scanner(new File(filename));
        int lines = 0;
        while (input.hasNextLine()) {
            input.nextLine();
            lines++;
        }
        input.close();
        return lines;
    }
    public static void readAccountInfo(int[] acctNums, String[] names, String[] surnames, double [] balances, String filename) {
        try (Scanner input = new Scanner(new File(filename))) {
            int i = 0;
            while (input.hasNext()) {
                acctNums[i] = input.nextInt();
                names[i] = input.next();
                surnames[i] = input.next();
                balances[i] = Double.parseDouble(input.next());
                i++;
            }
        }
        catch (FileNotFoundException e) {
            System.out.println("ERROR: file not found.");
        }
    }
    public static boolean deposit(double[] balances, int index, double amount) {
        if (isDepositValid(amount)) {
            balances[index] += amount;
            return true;
        }
        return false;
    }
    public static boolean isDepositValid(double amount) {
        return amount > 0;
    }
    public static boolean isWithdrawalValid(double balance, double amount) {
        return amount > 0 && amount <= balance;
    }
    public static boolean withdrawal(double[] balances, int index, double amount) {
        if (isWithdrawalValid(balances[index], amount)) {
            balances[index] -= amount;
            return true;
        }
        return false;
    }
    public static int findAcct(int [] acctNums, int acctNum) {
        int a = -1;
        for(int i = 0; i < acctNums.length ; i++) {
            if (acctNums[i] == acctNum) {
                a = i;
            }
        }
        return a;
    }
    public static int transfer(int[] acctNums, double[] balances, int acctNumFrom, int acctNumTo, double amount) {
        int fromIndex = findAcct(acctNums, acctNumFrom);
        int toIndex = findAcct(acctNums, acctNumTo);
        if (fromIndex == -1) {
            return 2;
        }
        if (toIndex == -1) {
            return 3;
        }
        if (!isWithdrawalValid(balances[fromIndex], amount)) {
            return 1;
        }

        withdrawal(balances, fromIndex, amount);

        deposit(balances, toIndex, amount);
        return 0;
    }
    public static void writeAccountInfo(int[] acctNums, String[] names, String[] surnames, double[] balances, String filename) {
        try (FileWriter writer = new FileWriter(filename)) {
            for (int i = 0; i < acctNums.length; i++) {
                String line = acctNums[i] + " " + names[i] + " " + surnames[i] + " " + balances[i] + "\n";
                writer.write(line);
            }
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }
    public static String description(int codeNumber){
        String str = "";
        if(codeNumber == 0){
            str = ": STX - Transfer Successful\n";
        }
        if(codeNumber == 2){
            str = ": FNF - From Account not found\n";
        }
        if(codeNumber == 3){
            str = ": TNF - To Account not found\n";
        }
        if(codeNumber == 1){
            str = ": NSF - Insufficient Funds\n";
        }
        return str;
    }
    public static int billCode(int[] acctNums, double[] balances, int billNum, double amount) {
        int index = findAcct(acctNums, billNum);
        if (index == -1) {
            return 2;
        }
        if (!withdrawal(balances, index, amount)) {
            return 1;
        }
        balances[index] -= amount;
        return 0;
    }
    public static String billMessage(int billCode){
        String str = "";
        if(billCode == 0){
            str = ": STX - Payment Successful\n";
        }
        if(billCode == 2){
            str = ": FNF - Account not found\n";
        }
        if(billCode == 1){
            str = ": NSF - Insufficient Funds\n";
        }
        return str;
    }
}