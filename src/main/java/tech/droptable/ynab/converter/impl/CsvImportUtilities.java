/**
 * 
 */
package tech.droptable.ynab.converter.impl;

import java.io.IOException;
import java.text.ParseException;

/**
 * Contains utility methods for managing CsvImports.
 * 
 * @author Joen Peter
 *
 */
public class CsvImportUtilities {

  /**
   * Create a CsvImport based on an export in csv format from earlier
   * @param data the content of the csv export 
   * @return the CsvImport representing the CSV file in question
   * @throws IOException 
   * @throws ParseException 
   */
  public static CsvImport createFromExport(String data, String accountName) throws IOException, ParseException {
    CsvImport csv = new CsvImport(accountName);
    String lines[] = data.split("\\r?\\n");
    String head = lines[0];
    for(int i = 1; i < lines.length; i++) {
      csv.addTransaction(new Transaction(lines[i], head));
    }
    return csv;
  }
  
}
