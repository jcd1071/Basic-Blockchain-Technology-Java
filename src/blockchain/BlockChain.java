/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blockchain;

import com.google.gson.GsonBuilder;
import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;
import org.bouncycastle.*;


/**
 *
 * @author Jose
 */
public class BlockChain {
    
    public static Boolean isChainValid() {
    /*loop through all blocks in the chain - compare the hashes
    check the hash variable is actually equal to the calculated hash and
    previous block hash is equal to the previousHash variable
    */
    
        Block currentBlock;
        Block previousBlock;
        String hashTarget = new String(new char[difficulty]).replace('\0', '0');
        HashMap<String,TransactionOutput> tempUTXOs = new HashMap<String,TransactionOutput>(); //temporary work list of unspent transactions at block state
        tempUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));
        
        //loop through blockchain to check hashes
        for(int i = 1; i < blockchain.size(); i++){
            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i-1);
            
            //compare registered hash and calculated hash
            if(!currentBlock.hash.equals(currentBlock.calculateHash())) {
                System.out.println("Current Hashes not equal");
                return false;
            }
            
            //compare previous hash and registered previous hash
            if(!previousBlock.hash.equals(currentBlock.previousHash)) {
                System.out.println("Previous Hashes not equal");
                return false;
            }
            
            //check if hash is solved
            if(!currentBlock.hash.substring(0, difficulty).equals(hashTarget)) {
                System.out.println("This block hasnt been mined");
                return false;
            }
            
            //loop thru blockchains transactions :
            TransactionOutput tempOutput;
            for(int t = 0; t<currentBlock.transactions.size(); i++ ) {
                Transaction currentTransaction = currentBlock.transactions.get(i);
                
                if(!currentTransaction.verifySignature()) {
                    System.out.println("#Signature on Transaction (" + t + ") is Invalid");
                    return false;
                }
                if(currentTransaction.getInputValue() != currentTransaction.getOutputsVlue()) {
                    System.out.println("#Inputs are not equal to outputs on Transaction (" + t + ")");
                    return false;
                }
                
                for(TransactionInput input : currentTransaction.inputs) {
                    tempOutput = tempUTXOs.get(input.transactionOutputId);
                    
                    if(tempOutput == null) {
                        System.out.println("#Referenced input on Transaction (" + t + ") is missing");
                        return false;
                    }
                    if(input.UTXO.value != tempOutput.value) {
                        System.out.println("#Reference input on Transaction (" + t + ") value is Invalid");
                        return false;
                    }
                    
                    tempUTXOs.remove(input.transactionOutputId);
                }
                
                for(TransactionOutput output : currentTransaction.outputs) {
                    tempUTXOs.put(output.id, output);
                    
                }
                if(currentTransaction.outputs.get(0).recipient != currentTransaction.recipient) {
                    System.out.println("#Transaction(" + t + ") output recipeint is not who it should be");
                    return false;
                }
                if(currentTransaction.outputs.get(1).recipient != currentTransaction.sender) {
                    System.out.println("#Transaction(" + t + ") output 'change' is not sender.");
            }
        }
    }
    System.out.println("Blockchain is valid");
    return true;
        
        /*changes to the blockchains blocks will return false from method
        proof of work will cause considerable time and computational power to
        create new blocks. attackers will need more computational power than total
        combined peers. miners needed to provide the proof of work.
        */
    }
    
    public static void addBlock(Block newBlock) {
        newBlock.mineBlock(difficulty);
        blockchain.add(newBlock);
    }
        
    

    /**
     * @param args the command line arguments
     */

    //arraylist to add blocks
    public static ArrayList<Block> blockchain = new ArrayList<Block>();
    public static HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>(); //list all the unspent transactions
    
    public static int difficulty = 5; //start difficulty from 4-6 for testing 1-2 solved instantly
    public static float minimumTransaction = 0.1f;
    public static Wallet walletA;
    public static Wallet walletB;
    public static Transaction genesisTransaction;
    
    
    public static void main(String[] args) {
        
        //setup Bouncey castle as Security Provider
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        
        //create new Wallets
        walletA = new Wallet();
        walletB = new Wallet();
        Wallet coinbase = new Wallet();
        
        //Create genesis transaction, which sends 100 coins to walletA :
        genesisTransaction = new Transaction(coinbase.publicKey, walletA.publicKey, 100f, null);
        genesisTransaction.generateSignature(coinbase.privateKey); //manually sign genesis transaction
        genesisTransaction.transactionId = "0";
        genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.recipient, genesisTransaction.value,
                                        genesisTransaction.transactionId)); //manually add the transactions output
        UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0)); //store first transaction in the UTXOs list
        
        System.out.println("Creating and Mining Genesis block... ");
        Block genesis = new Block("0");
        genesis.addTransaction(genesisTransaction);
        addBlock(genesis);
        
        //testing
        Block block1 = new Block(genesis.hash);
        System.out.println("\nWalletA's balance is : " + walletA.getBalance());
        System.out.println("\nWalletA is attempting to send funds (40) to WalletB...");
        block1.addTransaction(walletA.sendFunds(walletB.publicKey, 40f));
        addBlock(block1);
        System.out.println("\nWalletA's balance is : " + walletA.getBalance());
        System.out.println("WalletB's balance is : " + walletB.getBalance());
        
        Block block2 = new Block(block1.hash);
        System.out.println("\nWalletA's balance is : " + walletA.getBalance());
        System.out.println("\nWalletA is attempting to send funds (1000) to WalletB...");
        block2.addTransaction(walletA.sendFunds(walletB.publicKey, 1000f));
        addBlock(block2);
        System.out.println("\nWalletA's balance is : " + walletA.getBalance());
        System.out.println("WalletB's balance is : " + walletB.getBalance());
        
        Block block3 = new Block(block2.hash);
        System.out.println("\nWalletA's balance is : " + walletA.getBalance());
        System.out.println("\nWalletA is attempting to send funds (20) to WalletB...");
        block3.addTransaction(walletA.sendFunds(walletB.publicKey, 20f));
        addBlock(block3);
        System.out.println("\nWalletA's balance is : " + walletA.getBalance());
        System.out.println("WalletB's balance is : " + walletB.getBalance());
        
        isChainValid();
    }

        
        /*
        ******************************************************
        *                                                    *
        *   step below is testing public and private keys    *
        *           and verifying signatures                 *   
        *                                                    *
        ******************************************************
        //Test the public and private keys
        System.out.println("Private and public keys : ");
        System.out.println(StringUtility.getStringFromKey(walletA.privateKey));
        System.out.println(StringUtility.getStringFromKey(walletA.publicKey));
        
        //Test transaction from walletA to walletB 
        Transaction transaction = new Transaction(walletA.publicKey, walletB.publicKey, 5, null);
        transaction.generateSignature(walletA.privateKey);
        
        //Verify the signature works and verify it from the public key
        System.out.println("Is signature verified : ");
        System.out.println(transaction.verifySignature());
      
        /*
        ******************************************************
        *                                                    *
        *   step below is testing of mining different blocks *
        *           while increasing difficulty              *   
        *                                                    *
        ******************************************************
        
        
        //add blocks to the blockchain arraylist
        blockchain.add(new Block("Block 1 : ","0"));
        System.out.println("Trying to mine for Block 1...");
        blockchain.get(0).mineBlock(difficulty);
        
        blockchain.add(new Block("Block 2 : ", blockchain.get(blockchain.size()-1).hash));
        System.out.println("Trying to mine for Block 2...");
        blockchain.get(1).mineBlock(difficulty);
        
        blockchain.add(new Block("Block 3 : ", blockchain.get(blockchain.size()-1).hash));
        System.out.println("Trying to mine for Block 3...");
        blockchain.get(2).mineBlock(difficulty);
        
        /*ABOVE - - trigger the mineblock for each new block
        BELOW - - check validity for each block solved hash
        
        System.out.println("\nBlockcahin is Valid : " + isChainValid());
        
        String blockchainJson = new GsonBuilder().setPrettyPrinting().create().toJson(blockchain);
        System.out.println("\nThe Block Chain : ");
        System.out.println(blockchainJson);
        */
        
}
    

