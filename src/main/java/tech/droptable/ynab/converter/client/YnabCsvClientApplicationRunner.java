/**
 * 
 */
package tech.droptable.ynab.converter.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import tech.droptable.ynab.converter.impl.CsvImport;
import tech.droptable.ynab.converter.impl.CsvImportUtilities;

/**
 * @author Joen Peter
 *
 */
public class YnabCsvClientApplicationRunner implements ApplicationRunner {
  private static final Logger logger = LoggerFactory.getLogger(YnabCsvClientApplicationRunner.class);
  
  @Value("${ynab.output}")
  private String exportPath;
  
  @Value("${ynab.input}")
  private String importPath;
  
  @Override
  public void run(ApplicationArguments args) throws Exception {
    logger.info("Import from: " + importPath);
    logger.info("Export to: " + exportPath);
    List<String> files = getFilesFromFolder();
    if(files.isEmpty()) {
      logger.info("No files to import. Aborting.");
    }
    List<CsvImport> imports = createImportsFromFiles(files);
    exportFiles(imports);
    deleteFiles(files);
  }

  /**
   * Delete the files provided
   * @param files paths to files that should be deleted
   * @throws IOException 
   */
  private void deleteFiles(List<String> files) throws IOException {
    for(String f: files) {
      Files.delete(Paths.get(f));
    }
  }

  /**
   * Create CsvImport pbjects from files; one CsvImport per file
   * @param files the list of paths to files to import
   * @return the list of imported Csvs
   * @throws IOException 
   * @throws ParseException 
   */
  private List<CsvImport> createImportsFromFiles(List<String> files) throws IOException, ParseException {
    List<CsvImport> imports = new ArrayList<>();
    for(String file: files) {
      String data = getContentFromFile(file);
      String accountName = getAccountName(file, data);
      CsvImport i = CsvImportUtilities.createFromExport(data, accountName);
      imports.add(i);
    }
    return imports;
  }
  
  private String getAccountName(String file, String data) {
    String fileName = Paths.get(file).getFileName().toString();
    String retval = "";
    if(fileName.startsWith("activity")) {
      retval = "Amex";
    } else if (fileName.startsWith("Transaktioner_")) {
      retval = "Mastercard";
    } else if (fileName.startsWith("PERSONKONTO")) {
      retval = "Nordea";
    }
    return retval;
  }

  /**
   * Export into the out folder on correct format
   * @param imports all the csvs to export
   * @throws IOException 
   */
  private void exportFiles(List<CsvImport> imports) throws IOException {
    int counter = 1;
    for(CsvImport i: imports) {
      String path = exportPath + "/export" + counter + "-" + i.getAccountName() + ".csv";
      logger.info("Exporting file: " + path);
      String data = i.toString();
      Files.write(Paths.get(path), data.getBytes(StandardCharsets.UTF_8));
      counter++;
    }
  }

  /**
   * Get the String content from the file
   * @param file the file to read
   * @return the content in String format
   * @throws IOException 
   */
  private String getContentFromFile(String file) throws IOException {
    logger.info("Importing: " + file);
    return Files.readString(Paths.get(file));
  }

  /**
   * Get a list of files in a folder
   * @return the list of files
   * @throws IOException 
   */
  private List<String> getFilesFromFolder() throws IOException {
    if(!Files.isDirectory(Paths.get(importPath))) {
      throw new IOException("Import path not a folder: " + importPath);
    }
    List<String> files = new ArrayList<>();
    DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(importPath));
    stream.forEach(p -> files.add(p.toString()));
    return files;
  }

}
