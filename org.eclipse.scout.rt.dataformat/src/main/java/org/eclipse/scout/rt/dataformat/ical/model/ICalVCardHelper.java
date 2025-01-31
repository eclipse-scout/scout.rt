/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataformat.ical.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.scout.rt.dataformat.io.FoldingWriter;
import org.eclipse.scout.rt.dataformat.io.UnfoldingReader;
import org.eclipse.scout.rt.dataformat.vcard.VCardProperties;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.StringUtility;

@ApplicationScoped
public class ICalVCardHelper {
  protected static final String YEAR_MONTH_DAY_FORMAT = "yyyyMMdd";
  protected static final String HOUR_MINUTE_FORMAT = "HHmm";
  protected static final char ESCAPE_CHARACTER = '\\';
  protected static final char STRUCTURED_VALUE_DELIMITER = ';';
  protected static final char LIST_OF_VALUES_DELIMITER = ',';

  public <T extends AbstractEntity> T parse(Reader r, String characterSet, T entity) {
    Assertions.assertNotNull(entity);
    try (UnfoldingReader reader = new UnfoldingReader(r, characterSet)) {
      Property property = reader.readProperty();
      entity.addProperty(property);
      while ((property = reader.readProperty()) != null) {
        entity.addProperty(property);
      }
      return entity;
    }
    catch (Exception e) {
      throw new ProcessingException("invalid iCal/vCard format", e);
    }
  }

  public void write(AbstractEntity entity, Writer writer, String charset) {
    try (FoldingWriter f = new FoldingWriter(writer)) {
      for (Property p : entity.getProperties()) {
        applyCharset(p, charset);
        f.write(p);
      }
      f.flush();
    }
    catch (IOException e) {
      throw new ProcessingException("unable to write iCal/vCard", e);
    }
  }

  private void applyCharset(Property p, String charset) {
    if (!(VCardProperties.PROP_NAME_BEGIN.equals(p.getName()) || VCardProperties.PROP_NAME_VERSION.equals(p.getName()) || VCardProperties.PROP_NAME_END.equals(p.getName()))
        && !p.hasParameter(PropertyParameter.PARAM_CHARSET)) {
      if (StringUtility.equalsIgnoreCase(charset, "utf-8")) {
        // workaround for Microsoft Outlook 2007, 2010 (which can not read UTF-8 vCards otherwise)
        // (Outlook 2000, 2002 and 2003 can not read UTF-8 vCards anyway)
        p.addParameters(PropertyParameter.CHARSET_UTF_8_FOR_OUTLOOK);
      }
      else if (!StringUtility.isNullOrEmpty(charset)) {
        // set the configured charset (if not null) on each property (the default would be 'US-ASCII' for vcard 2.1 which does not support some special characters used in german)
        p.addParameters(new PropertyParameter(PropertyParameter.PARAM_CHARSET, charset));
      }
    }
  }

  public byte[] toBytes(AbstractEntity entity, String charset) {
    //noinspection resource
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    write(entity, new OutputStreamWriter(bos), charset);
    return bos.toByteArray();
  }

  /**
   * Computes a {@link Date} which results in the same text representation when interpreted in UTC as the given
   * {@link Date} when interpreted in the given {@link TimeZone}.<br>
   * In other words: When converting a {@link Date} to a text representation, a timezone must be used (because
   * {@link Date} is basically just a long) and has no {@link TimeZone} information. The given date converted to text in
   * the given {@link TimeZone} results in a certain text (different for every timezone). This method creates a
   * {@link Date} which results in the same text representation for the UTC timezone.
   *
   * @param d
   *     The date. Must not be {@code null}.
   * @param zone
   *     The {@link TimeZone} in which to read the {@link Date}.
   * @return A {@link Date} which has the same text representation in UTC as the given {@link Date} in the given
   * {@link TimeZone}.
   */
  public Date removeTimeZoneOffset(Date d, TimeZone zone) {
    Locale locale = NlsLocale.get();
    Calendar dateCal = Calendar.getInstance(zone, locale);
    dateCal.setTime(d);
    int zoneOffset = dateCal.get(Calendar.ZONE_OFFSET);
    int dayLightSavingOffset = dateCal.get(Calendar.DST_OFFSET);
    dateCal.add(Calendar.MILLISECOND, zoneOffset + dayLightSavingOffset);
    return dateCal.getTime();
  }

  /**
   * Format date without time according to ical format
   */
  public String createDate(Date d) {
    // Shift the timezone to UTC
    SimpleDateFormat dateFormatter = new SimpleDateFormat(YEAR_MONTH_DAY_FORMAT);
    dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    // ICal DATE pattern: <yyyyMMdd>
    return dateFormatter.format(d);
  }

  /**
   * Formats date with time according to ical format
   */
  public String createDateTime(Date d) {
    // Shift the timezone to UTC
    TimeZone utc = TimeZone.getTimeZone("UTC");
    SimpleDateFormat dateFormatter = new SimpleDateFormat(YEAR_MONTH_DAY_FORMAT);
    dateFormatter.setTimeZone(utc);
    SimpleDateFormat timeFormatter = new SimpleDateFormat(HOUR_MINUTE_FORMAT);
    timeFormatter.setTimeZone(utc);
    // ICal pattern: <yyyyMMdd>T<HHmmss>Z; T => time start marker, Z => time end marker; we always truncate secs
    return dateFormatter.format(d) + "T" + timeFormatter.format(d) + "00Z";
  }

  /**
   * Format a duration according to the ical spec
   */
  public String formatDurationAsNegative(Duration d) {
    if (d == null) {
      d = Duration.ZERO;
    }
    return "-" + d.toString();
  }

  /**
   * Joins the given parts together, separated by the given delimiter. <code>null</code> values are treated as empty
   * strings and are concatenated as well. Never returns <code>null</code>.
   * <p>
   * Examples:
   *
   * <pre>
   * concatenateStrings(".", "hello", null, "world", "", null) returns "hello..world.."
   * concatenateStrings(null, "hello", "", "foo", "world") returns "hellofooworld"
   * </pre>
   * </p>
   */
  public String concatenateStrings(String delimiter, String... parts) {
    if (parts == null) {
      return "";
    }
    if (delimiter == null) {
      delimiter = "";
    }

    return Stream.of(parts)
        .map(StringUtility::emptyIfNull)
        .collect(Collectors.joining(delimiter));
  }

  /**
   * In contrast to {@link #composeStructuredValue(String...)} this method also performs backslash escaping of the
   * BACKSLASH, COMMA and SEMI-COLON characters occurring in the given <code>values</code>. I.e. it assumes that the
   * given <code>values</code> are single values and do not contain a list of values themselves (where e.g. COMMA
   * characters should not be escaped).
   * <p>
   * Not to be used together with {@link #concatenateListOfValues(String...)}!
   * </p>
   *
   * @param values
   *     to be backslash escaped
   * @return The structured value, according to RFC-2426 or RFC-5545 respectively (concatenation of the given values
   * (after escaping special characters), delimited by the SEMI-COLON character).
   */
  public String composeStructuredValueFromSingleValues(String... values) {
    return concatenateStrings(String.valueOf(STRUCTURED_VALUE_DELIMITER),
        backslashEscape(new char[]{LIST_OF_VALUES_DELIMITER, STRUCTURED_VALUE_DELIMITER}, values));
  }

  /**
   * <p>
   * May be used together with {@link #concatenateListOfValues(String...)}.
   * </p>
   *
   * @param values
   *     MUST NOT contain any SEMI-COLON characters unless they are escaped by a BACKSLASH character.
   * @return The structured value, according to RFC-2426 or RFC-5545 respectively (concatenation of the given values,
   * delimited by the SEMI-COLON character).
   */
  public String composeStructuredValue(String... values) {
    return concatenateStrings(String.valueOf(STRUCTURED_VALUE_DELIMITER), values);
  }

  /**
   * Backslash escapes all BACKSLASH, COMMA and SEMI-COLON characters occurring in the given <code>values</code> and
   * then returns a comma separated list.
   * <p>
   * Not to be used together with {@link #composeStructuredValueFromSingleValues(String...)}! But may be used together
   * with {@link #composeStructuredValue(String...)}.
   * </p>
   *
   * @param values
   *     to be backslash escaped and concatenated
   * @return The list of values, according to RFC-2426 or RFC-5545 respectively (concatenation of the given values
   * (after escaping special characters), delimited by the COMMA character).
   */
  public String concatenateListOfValues(String... values) {
    return concatenateStrings(String.valueOf(LIST_OF_VALUES_DELIMITER),
        backslashEscape(new char[]{LIST_OF_VALUES_DELIMITER, STRUCTURED_VALUE_DELIMITER}, values));
  }

  /**
   * Escapes all occurrences of the given <code>c</code> in the given <code>values</code> by putting a backslash in
   * front of them.
   *
   * @param c
   *     character to be escaped
   * @param values
   *     possibly containing character to be escaped
   * @return values where occurrences of the given <code>c</code> are backslash escaped
   */
  public String[] backslashEscape(char c, String... values) {
    if (values == null) {
      return null;
    }

    String cAsString = String.valueOf(c);
    for (int i = 0; i < values.length; i++) {
      values[i] = StringUtility.replace(values[i], cAsString, ESCAPE_CHARACTER + cAsString);
    }
    return values;
  }

  /**
   * Escapes all occurrences of any of the given <code>chars</code> in the given <code>values</code> by putting a
   * backslash in front of them. The escaping occurs in the order of the given <code>chars</code>, after
   * backslash-escaping all already existent backslashes in the given <code>values</code>. If <code>chars</code>
   * contains '\\' this will be ignored. If a character in <code>chars</code> occurs more than once, only the first
   * occurrence will be processed.
   * <p>
   * I.e.:
   *
   * <pre>
   * backslashEscape("a@".toCharArray(), "hallo@all", "foo\\@bar\\..")
   * returns the same as
   * backslashEscape("a@\\@a".toCharArray(), "hallo@all", "foo\\@bar\\..")
   * which is:
   * [h\allo\@\all, foo\\\@b\ar\\..]
   * </pre>
   * </p>
   *
   * @param chars
   *     characters to be escaped
   * @param values
   *     possibly containing characters to be escaped
   * @return values where characters are backslash escaped in the following order (ignoring '\\' and multiple
   * occurrences in <code>chars</code>):
   * <ul>
   * <li><code>\</code> -> <code>\\</code>
   * <li>chars[0] -> <code>\</code>chars[0]
   * <li>chars[1] -> <code>\</code>chars[1]
   * <li>etc..
   * </ul>
   */
  public String[] backslashEscape(char[] chars, String... values) {
    if (values == null) {
      return null;
    }

    // put backslash character at first position, only take first occurrence of each given character, keep the order
    Set<Character> characters = new LinkedHashSet<>();
    characters.add(ESCAPE_CHARACTER);
    for (char c : chars) {
      characters.add(c);
    }

    // iterate over all characters to be escaped
    for (Character character : characters) {
      String characterAsString = character.toString();
      // escape character in all of the values
      for (int i = 0; i < values.length; i++) {
        values[i] = StringUtility.replace(values[i], characterAsString, ESCAPE_CHARACTER + characterAsString);
      }
    }
    return values;
  }

  /**
   * @return <code>true</code> if, and only if, support for the named charset is available in the current Java virtual
   * machine, <code>false</code> otherwise (does not throw exceptions)
   */
  public boolean isCharacterSetSupported(String charset) {
    try {
      return Charset.isSupported(charset);
    }
    catch (Exception e) { // NOSONAR
      return false;
    }
  }
}
