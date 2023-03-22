/**
 * 
 */
package tech.droptable.ynab.converter.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a single transaction on an account
 * 
 * @author Joen Peter
 */
public class Transaction {

  private static final String DATE_PATTERN = "yyyy-MM-dd";
  private static final String DATE_INPUT_PATTERN = "[MM/dd/yyyy][yyyy/MM/dd][dd-MMM][MM-dd][yyyy-MM-dd]";
  private static final int HARDCODED_DEFAULT_YEAR = 2023;
  private DateTimeFormatter formatter;
  private DateTimeFormatter inputFormatter;
  private Logger logger;
  
  private Instant date;
  private BigDecimal amount;
  private String payee;
  private String memo;
  
  public Transaction(Instant date, BigDecimal amount, String payee, String memo) {
    this.date = date;
    this.amount = amount;
    this.payee = payee;
    this.memo = memo;
    
    formatter = getDateFormatter();
    inputFormatter = createInputDateFormatter();
  }
  
  /**
   * Create a new Transaction based on a single line of a csv
   * @param line the line from the csv
   * @param ordering order that columns come (std names)
   * @throws IOException 
   * @throws ParseException 
   */
  public Transaction(String line, String ordering) throws IOException, ParseException, UnsupportedOperationException {
    if(line.startsWith("Valutakurs:")) {
      // Special case for int transaction for MC
      // This actually belongs to previous transaction, but is not needed
      throw new UnsupportedOperationException("Cannot handle add-on lines for transaction");
    }
    
    formatter = getDateFormatter();
    inputFormatter = createInputDateFormatter();
    logger = LoggerFactory.getLogger(Transaction.class);
    if(parseInput(line, ordering, ",")) {
      // ok!
      return;
    } 
    if(parseInput(line, ordering, ";")) {
      // also ok!
      return;
    }
    
    throw new IOException("Unable to parse transaction: [" + line + "] with [" + ordering + "]");
  }
  
  private DateTimeFormatter createInputDateFormatter() {
    return new DateTimeFormatterBuilder()
        //.parseDefaulting(ChronoField.YEAR, getDefaultYear())
        .appendPattern(DATE_INPUT_PATTERN)
        .toFormatter(Locale.ENGLISH);
  }

  /**
   * Gets us the most reasonable year for the current time
   * @return
   */
  private int getDefaultYear() {
    return HARDCODED_DEFAULT_YEAR;
  }

  private DateTimeFormatter getDateFormatter() {
    return DateTimeFormatter.ofPattern(DATE_PATTERN).withZone(ZoneId.from(ZoneOffset.UTC));
  }

  private boolean parseInput(String line, String ordering, String separator) throws ParseException {
    String[] data = line.split(separator + "(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
    String[] head = ordering.split(separator);
    boolean dateOk = false;
    boolean amountOk = false;
    boolean payeeOk = false;
    if(data.length < 3) {
      logger.info("Less than 3 data blocks with [" + separator + "] (" + data.length + ")");
      logger.info(Arrays.toString(data));
      return false;
    } else if(data.length != head.length) {
      logger.info("Length difference with [" + separator + "]");
      logger.info(Arrays.toString(data));
      logger.info(Arrays.toString(head));
      return false;
    }
    
    for(int i = 0; i < data.length; i++) {
      if(isDate(head[i])) {
        date = parseDate(data[i]);
        dateOk = true;
      } else if(isAmount(head[i])) {
        amount = parseAmount(data[i]);
        amountOk = true;
      } else if(isPayee(head[i])) {
        payee = data[i];
        payeeOk = true;
      } else if(isMemo(head[i])) {
        memo = data[i];
      }
    }
    if(dateOk && amountOk && payeeOk) {
      return true;
    } else {
      logger.warn("Tried parsing transaction, but did not find all needed data.");
      logger.info("date=" + dateOk + " amount=" + amountOk + " payee=" + payeeOk);
      logger.info(Arrays.toString(head));
      logger.info(Arrays.toString(data));
      return false;
    }
  }
  
  private boolean isDate(String string) {
    return "Date".equalsIgnoreCase(string)
        || string.contains("Datum")
        || (string.contains("Bokf") && string.contains("ringsdag"));
  }
  
  private boolean isAmount(String string) {
    return "Amount".equalsIgnoreCase(string)
        || "Belopp".equalsIgnoreCase(string);
  }

  private boolean isMemo(String string) {
    return "Memo".equalsIgnoreCase(string);
  }
  
  private boolean isPayee(String string) {
    return "Payee".equalsIgnoreCase(string)
        || "Beskrivning".equalsIgnoreCase(string)
        || "Rubrik".equalsIgnoreCase(string)
        || "Specifikation".equalsIgnoreCase(string);
  }

  private BigDecimal parseAmount(String string) throws ParseException {
    try {
      return BigDecimal.valueOf(Double.parseDouble(string));
    } catch (NumberFormatException e) {
      logger.debug("Regular double parser does not work - falling back to Number");
    }
    string = string.replaceAll("\"", "");
    string = string.replaceAll("\\s+","");
    
    NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
    Number number = format.parse(string);
    return BigDecimal.valueOf(number.doubleValue());
  }

  private Instant parseDate(String string) {
    TemporalAccessor accessor = inputFormatter.parse(string);
    LocalDate date;
    if(accessor.isSupported(ChronoField.YEAR)) {
      date = LocalDate.from(accessor);
    } else {
      date = LocalDate.of(HARDCODED_DEFAULT_YEAR, accessor.get(ChronoField.MONTH_OF_YEAR), accessor.get(ChronoField.DAY_OF_MONTH));
    }
    
    return date.atStartOfDay(ZoneOffset.UTC).toInstant();
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder
        .append(formatDate(date))
        .append(",")
        .append(amount)
        .append(",")
        .append(payee)
        .append(",")
        .append(memo);
    
    return builder.toString();
  }
  
  public String toString(String ordering) {
    return toString();
  }

  private String formatDate(Instant date) {
    return formatter.format(date);
  }
}
