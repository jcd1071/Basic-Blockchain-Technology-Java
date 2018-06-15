/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blockchain;

import java.security.PublicKey;

/**
 *
 * @author Jose
 */
public class TransactionOutput {
    
    public String id;
    public PublicKey recipient; //owner of the coins
    public float value; //amount of coins
    public String parentTransactionId; //id of trans where out put was created
    
    //Constructor:
    
    public TransactionOutput(PublicKey recipient, float value, String parentTransactionId) {
        this.recipient = recipient;
        this.value = value;
        this.parentTransactionId = parentTransactionId;
        this.id = StringUtility.applySha256(StringUtility.getStringFromKey(recipient) + Float.toString(value) +
                parentTransactionId);
    }
    
    //Check if coin belongs to you
    public boolean isMine(PublicKey publicKey) {
        return (publicKey == recipient);
    }
    
    /*
    transaction outputs will show final amount sent to each party from transaction
    when reference as inputs in new transactions, act as proof that you have coins to send. 
    */
    
}
