/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blockchain;

import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author Jose
 */
public class Block {
    public String hash; //digital signature
        public String previousHash; // previous blocks hash
        public String data; //simple message for data - hold block data
        public String merkleRoot;
        public ArrayList<Transaction> transactions = new ArrayList<Transaction>(); //data will be a message
        private long timeStamp; //milliseconds since 1/1/1970
        private int nonce;

        //Block constructor
        public Block(String previousHash) {
            this.previousHash = previousHash;
            this.timeStamp = new Date().getTime();
            
            this.hash = calculateHash(); //completed after setting other variables
        }

        
        public String calculateHash() {
            //calculate hash from all block parts that are not to be tampered with
            String calculatedHash = StringUtility.applySha256(
                previousHash +
                Long.toString(timeStamp) +
                Integer.toString(nonce) +
                merkleRoot
                );
            return calculatedHash;
        }
        
        public void mineBlock(int difficulty) {
            /*takes int difficulty - number of 0's that must solve for. low difficulty
            such as 1 or 2 can be solved almost instantly. test 4-6 
            litecoins start difficulty == 442,592
            */
            merkleRoot = StringUtility.getMerkleRoot(transactions);
            
            String target = new String(new char[difficulty]).replace('\0', '0'); //String with difficulty * "0"
            while(!hash.substring(0, difficulty).equals(target)) {
                nonce++;
                hash = calculateHash();
            }
            System.out.println("Block Mined! : " + hash);
        }
        
        //add transaction to the block
        public boolean addTransaction(Transaction transaction) {
            //process transaction and check if valid, unless block is genesis then ignore
            if(transaction == null) return false;
            if(previousHash != "0") {
                if(transaction.processTransaction() != true) {
                    System.out.println("Transaction failed to process. Discarded.");
                    return false;
                }
            }
            transactions.add(transaction);
            System.out.println("Transaction successfully added to Block.");
            return true;
        }
}
