/**
 * 
 */
package tech.droptable.ynab.data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Joen Peter
 *
 */
public class CsvImport {
  private static final String DEFAULT_COLUMNS = "Date,Amount,Payee,Memo";
  
  private String accountName;
  private String columns;
  private List<Transaction> transactions;
  
  public CsvImport(String accountName) {
    columns = DEFAULT_COLUMNS;
    transactions = new ArrayList<>();
    this.accountName = accountName;
  }
  
  public void addTransaction(Transaction t) {
    transactions.add(t);
  }
  
  public String getAccountName() {
    return accountName;
  }
  
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder
        .append(columns)
        .append('\n');
    
    for(Transaction t: transactions) {
      builder.append(t.toString(columns)).append('\n');
    }
    return builder.toString();
  }
}
