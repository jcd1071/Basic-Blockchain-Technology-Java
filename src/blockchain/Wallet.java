/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blockchain;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Jose
 */

/*
Gather balance - loop through UTXOs list and check if a transaout isMine();
generate transactions
*/

/*
TO ADD:
track record of transaction history
*/

public class Wallet {
    
    public PrivateKey privateKey; //used to sign transactions
    public PublicKey publicKey; //act as the address - sent with transaction to veryify signature validity & non-tampered data
    
    public HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>(); //onlyUTXOs owned by wallet
    
    public Wallet() {
        generateKeyPair();
    }

    private void generateKeyPair() {
        
        /*
        using KeyPairGenerator to generate Elliptic Curve keypair
        */
        
        try{
            
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA", "BC");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            ECGenParameterSpec eSpec = new ECGenParameterSpec("prime192v1");
            
            //intializing keygenerator and gerate keypair
            keyGen.initialize(eSpec, random); //spec and random instance
            KeyPair keyPair = keyGen.generateKeyPair();
            
            //set public and private keys from the keypair
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();
            
        } catch(Exception e){
            throw new RuntimeException(e);  
        }
         
    }
    
    public float getBalance() {
        float total = 0;
        for(Map.Entry<String,TransactionOutput> item: BlockChain.UTXOs.entrySet()) {
            TransactionOutput UTXO = item.getValue();
        
            if(UTXO.isMine(publicKey)) { //if output/coins belongs to me
                UTXOs.put(UTXO.id,UTXO); //add it to list of unspent transactions
                total += UTXO.value;
            
            }
        }
        return total;
    }
    
    //Generate and return a new transaction form this wallet
    public Transaction sendFunds(PublicKey _recipient, float value) {
        if(getBalance() < value) { //gather balance and check funds
            System.out.println("#Not enough funds to send transaction. Transaction discarded.");
            return null;
        }
        
        //arraylist of inputs
        ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
        
        float total = 0;
        for(Map.Entry<String, TransactionOutput> item: UTXOs.entrySet()) {
            TransactionOutput UTXO = item.getValue();
            total += UTXO.value;
            inputs.add(new TransactionInput(UTXO.id));
            if(total > value) break;
        }
        
        Transaction newTransaction = new Transaction(publicKey, _recipient, value, inputs);
        newTransaction.generateSignature(privateKey);
        
        for(TransactionInput input : inputs) {
            UTXOs.remove(input.transactionOutputId);
        }
        return newTransaction;
    }
}

