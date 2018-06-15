/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blockchain;

/**
 *
 * @author Jose
 */

/*
Reference TransactionOutputs that have not been spent. transactionOutputId is used to
find the relevant TransactionOutput - allows miners to check ownership
*/
public class TransactionInput {
    public String transactionOutputId; //reference to TransactionOutputs -> transactionId
    public TransactionOutput UTXO; //Contains the unspent transaction output
    
    public TransactionInput(String transactionOutputId) {
        this.transactionOutputId = transactionOutputId;
    }
    
}
