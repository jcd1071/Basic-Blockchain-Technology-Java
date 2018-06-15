/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blockchain;

import java.security.*;
import java.util.ArrayList;

/**
 *
 * @author Jose
 */

/*
data for transactions :
public key address of the sender
public key address of the reciever
the amount/value of funds to be transferred
inputs - references to previous transactions to show proof of funds
outputs - shows the amount relevant adreses recieved in transaction - reference as inputs in new transaction
cryptographic signature - prove owner of address is sending transaction and data has not been changed
*/
public class Transaction {
    
    public String transactionId; //functions as transaction hash as well
    public PublicKey sender; //senders public key
    public PublicKey recipient; //receivers public key
    public float value; //funds to be transferred
    public byte[] signature; //to prevent others from spending funds in wallet
    
    public ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();//store the inputs of transactions
    public ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>();//store the outputs of transactions
    
    private static int sequence = 0; //how many transactions have been generated
    
    //Constructor:
    public Transaction(PublicKey from, PublicKey to, float value, ArrayList<TransactionInput> inputs){
        this.sender = from;
        this.recipient = to;
        this.value = value;
        this.inputs = inputs;
    }
    
    //Calculate transaction hash and use as the ID
    private String calculateHash() {
        sequence++; //increase the sequence to not have 2 identical transactions having the same hash
        return StringUtility.applySha256(
                StringUtility.getStringFromKey(sender) +
                StringUtility.getStringFromKey(recipient) +
                Float.toString(value) + sequence
        );
    }
    
    //sign all the data that is not to be tampered with - miners verify signatures as new transactions are added to the block
    /*
    in polishing project review add sign information for :
    outputs/inputs
    timestamp
    */
    public void generateSignature(PrivateKey privateKey) {
        String data = StringUtility.getStringFromKey(sender) + StringUtility.getStringFromKey(recipient) +
                Float.toString(value);
        signature = StringUtility.applyECDSASig(privateKey, data);
    }
    
    public boolean verifySignature() {
        String data = StringUtility.getStringFromKey(sender) + StringUtility.getStringFromKey(recipient) +
                Float.toString(value);
        return StringUtility.verifyECDSASig(sender, data, signature);
    }
    
    //Returns true if new transaction could be created
    public boolean processTransaction() {
        
        if(verifySignature() == false) {
            System.out.println("#Transaction Signature failed to verify");
            return false;
        }
        
        //get transaction inputs - verify that they are unspent
        for(TransactionInput i : inputs) {
            i.UTXO = BlockChain.UTXOs.get(i.transactionOutputId);
        }
        
        //check if transaction is valid :
        if(getInputValue() < BlockChain.minimumTransaction) {
            System.out.println("#Transaction Inputs to small : " + getInputValue());
            return false;
        }
        
        //generate transaction outputs :
        float leftOver = getInputValue() - value; //get value of inputs change to leftovers
        transactionId = calculateHash();
        outputs.add(new TransactionOutput(this.recipient, value, transactionId)); //send value to recipient
        outputs.add(new TransactionOutput(this.sender, leftOver, transactionId)); //send the left over to sender
        
        //add outputs to unspent list
        for(TransactionOutput o : outputs) {
            BlockChain.UTXOs.put(o.id, o);
        }
        
        //remove transaction inputs from UTXO lists as spent:
        for(TransactionInput i : inputs) {
            if(i.UTXO == null) continue; //if transaction not found then skip
            BlockChain.UTXOs.remove(i.UTXO.id);
        }
        return true;
    }
    
    //return sum of inputs(UTXO) values
    public float getInputValue() {
        float total = 0;
        for(TransactionInput i : inputs) {
            if(i.UTXO == null) continue; //if transaction not found then skip
            total += i.UTXO.value;
        }
        return total;
    }
    
    //return sum of outputs
    public float getOutputsVlue() {
        float total = 0;
        for(TransactionOutput o : outputs){
            total += o.value;
        }
        return total;
    }
    
    /*
    transaction output can only be used once as an input 
    full value of imputs must be used -> sender sends change back to self
    */
    
}
