package org.eclipse.scout.rt.platform.util.date;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Strict variant of {@link SimpleDateFormat}. When parsing a string, it only returns a valid date when the input string
 * matches the pattern <b>exactly</b>.
 * <p>
 * This prevents the parsing of strings with date and time information when the pattern only contains the date part but
 * no time zone information. The default {@link SimpleDateFormat} implementation silently consumes the "surplus" time
 * information, even when the lenient flag is disabled. This could lead to a "shifted day" problem when the resulting
 * date object is formatted again with the same pattern. Example: {@code "1970-01-01 00:00"} → (parse) → <i>1969-12-31
 * 22:00</i> → (format) → {@code "1969-12-31"}.
 * <p>
 * <b>Note:</b> When the pattern string contains a time zone character ({@code "Z"}, {@code "z"} or {@code "X"}), no
 * validation is performed, since the potential problem does not apply in this case.
 * <p>
 * The behavior can be controlled by setting the {@link #setLenient(boolean)} flag. The default is <code>false</code>.
 * The check being performed in this case is even more strict than the default implementation. The following table shows
 * some of the differences.
 * <p>
 * <table border=1>
 * <tr>
 * <th>Pattern</th>
 * <th>Input string</th>
 * <th>SimpleDateFormat</th>
 * <th>StrictSimpleDateFormat</th>
 * </tr>
 * <tr>
 * <td><code>yyyy-MM-dd hh:mm.ss.SSS</code></td>
 * <td><code>2019-01-18</code></td>
 * <td>Rejected
 * <td>Rejected
 * </tr>
 * <tr>
 * <td><code>yyyy-MM-dd</code>
 * <td><code>2019-18</code>
 * <td>Rejected
 * <td>Rejected
 * </tr>
 * <tr>
 * <td><code>yyyy-MM-dd</code></td>
 * <td><code>2019-1-18</code></td>
 * <td><font color="red">Accepted</font></td>
 * <td><font color="green">Rejected</font></td>
 * </tr>
 * <tr>
 * <td><code>yyyyMMdd</code></td>
 * <td><code>20190118xyz</code></td>
 * <td><font color="red">Accepted</font></td>
 * <td><font color="green">Rejected</font></td>
 * </tr>
 * <tr>
 * <td><code>yyyy-MM-dd</code></td>
 * <td><code>2019-01-18 23:00:00.000</code></td>
 * <td><font color="red">Accepted</font></td>
 * <td><font color="green">Rejected</font></td>
 * </tr>
 * <tr>
 * <td><code>yyyy/yyyy</code></td>
 * <td><code>2018/2019</code></td>
 * <td><font color="red">Accepted</font></td>
 * <td><font color="green">Rejected</font></td>
 * </tr>
 * </table>
 * <p>
 * <b>Note:</b> setting the "lenient" flag to <code>true</code> will change the behavior back to the default of
 * {@link SimpleDateFormat}.
 */
public class StrictSimpleDateFormat extends SimpleDateFormat {
  private static final long serialVersionUID = 1L;

  public StrictSimpleDateFormat() {
    super();
    setLenient(false);
  }

  public StrictSimpleDateFormat(String pattern) {
    super(pattern);
    setLenient(false);
  }

  public StrictSimpleDateFormat(String pattern, DateFormatSymbols formatSymbols) {
    super(pattern, formatSymbols);
    setLenient(false);
  }

  public StrictSimpleDateFormat(String pattern, Locale locale) {
    super(pattern, locale);
    setLenient(false);
  }

  @Override
  public Date parse(String source) throws ParseException {
    Date parsedDate = super.parse(source);
    if (parsedDate != null && !isLenient()) {
      verifyParsedDate(source, parsedDate);
    }
    return parsedDate;
  }

  protected void verifyParsedDate(String source, Date parsedDate) throws ParseException {
    String pattern = toPattern();
    String patternWithoutQuotedStrings = pattern.replaceAll("''", "").replaceAll("'[^']*'", "");
    if (StringUtility.containsRegEx(patternWithoutQuotedStrings, "[zZX]")) {
      // If the pattern contains a time zone character, we cannot reliably check if the input string matches the date
      // pattern by simply formatting the parsed date again, because the time zone used in the input string does not
      // necessarily have to be the system time zone. Fortunately, we don't _have_ to check the pattern, because the
      // particular problem this class attempts to solve does not apply in this case.
      return;
    }
    String formattedDate = format(parsedDate);
    if (!source.equals(formattedDate)) {
      throw new ParseException("Input string '" + source + "' does not exactly match the pattern '" + pattern + "' (date is formatted back to '" + formattedDate + "' instead)", 0);
    }
  }
}
