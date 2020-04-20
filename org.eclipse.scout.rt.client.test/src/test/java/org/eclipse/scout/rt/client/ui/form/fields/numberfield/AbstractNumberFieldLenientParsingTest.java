package org.eclipse.scout.rt.client.ui.form.fields.numberfield;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map.Entry;

import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ImmutablePair;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class AbstractNumberFieldLenientParsingTest extends AbstractNumberField<BigDecimal> {

  private static Locale ORIGINAL_LOCALE;
  private static final BigDecimal DEFAULT_MIN_VALUE = new BigDecimal("-999999999999999999999999999999999999999999999999999999999999");
  private static final BigDecimal DEFAULT_MAX_VALUE = new BigDecimal("999999999999999999999999999999999999999999999999999999999999");

  @BeforeClass
  public static void setupBeforeClass() {
    ORIGINAL_LOCALE = NlsLocale.getOrElse(null);
    NlsLocale.set(new Locale("de", "CH"));
  }

  @AfterClass
  public static void tearDownAfterClass() {
    NlsLocale.set(ORIGINAL_LOCALE);
  }

  @Override
  protected BigDecimal getConfiguredMinValue() {
    return null;
  }

  @Override
  protected BigDecimal getConfiguredMaxValue() {
    return null;
  }

  @Override
  protected BigDecimal getMinPossibleValue() {
    return DEFAULT_MIN_VALUE;
  }

  @Override
  protected BigDecimal getMaxPossibleValue() {
    return DEFAULT_MAX_VALUE;
  }

  @Override
  protected BigDecimal parseValueInternal(String text) {
    return parseToBigDecimalInternal(text);
  }

  @Override
  protected BigDecimal roundParsedValue(BigDecimal valBeforeRounding) {
    // multiplier requirements for fraction digits are considered
    int precision = valBeforeRounding.toBigInteger().toString().length() + 4;
    return valBeforeRounding.round(new MathContext(precision, getRoundingMode()));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testLenientGrouping2() {
    // decimal
    setRoundingMode(RoundingMode.HALF_UP);

    // Not all of these testcases are parsed in the best way possible but they represent the current best-effort approach.
    HashMap<Locale, ArrayList<ImmutablePair<String, BigDecimal>>> testMap = new HashMap<>();
    // Testcases for de_CH
    testMap.put(Locale.forLanguageTag("de-CH"), CollectionUtility.<ImmutablePair<String, BigDecimal>> arrayList(
        new ImmutablePair("500.5", new BigDecimal(500.5)),
        new ImmutablePair("500,5", new BigDecimal(500.5)),
        new ImmutablePair("1.500.5", new BigDecimal(1500.5)),
        new ImmutablePair("1,500,5", new BigDecimal(1500.5)),
        new ImmutablePair("1.500.000", new BigDecimal(1500000)),
        new ImmutablePair("1,500,000", new BigDecimal(1500000)),
        new ImmutablePair("1.500.000.25", new BigDecimal(1500000.25)),
        new ImmutablePair("1.500.000,25", new BigDecimal(1500000.25)),
        new ImmutablePair("1,500,000,25", new BigDecimal(1500000.25)),
        new ImmutablePair("1,500,000.25", new BigDecimal(1500000.25)),
        new ImmutablePair("15.15.15.25", new BigDecimal(151515.25)), // Here we simplify it to the most right decimal separator...
        new ImmutablePair("15,15,15,25", new BigDecimal(151515.25)), // Here we simplify it to the most right decimal separator...
        new ImmutablePair("15,15,15.25", new BigDecimal(151515.25)), // This appears wrong to the eye, but is parsed successfully by DecimalFormat.parse while trying combinations of decimal/grouping separators
        new ImmutablePair("15.15.15,25", new BigDecimal(151515.25)), // This appears wrong to the eye, but is parsed successfully by DecimalFormat.parse while trying combinations of decimal/grouping separators
        new ImmutablePair(".5", new BigDecimal(0.5)),
        new ImmutablePair(",5", new BigDecimal(0.5)),
        new ImmutablePair("'5", new BigDecimal(5)),
        new ImmutablePair("..5", new BigDecimal(0.5)),
        new ImmutablePair(",,5", new BigDecimal(0.5)),
        new ImmutablePair("''5", new BigDecimal(5))));

    // Testcases for de_DE
    testMap.put(Locale.forLanguageTag("de-DE"), CollectionUtility.<ImmutablePair<String, BigDecimal>> arrayList(
        new ImmutablePair("500.5", new BigDecimal(5005)), // This appears wrong to the eye, but is parsed successfully by DecimalFormat.parse
        new ImmutablePair("500,5", new BigDecimal(500.5)),
        new ImmutablePair("1.500.5", new BigDecimal(15005)), // This appears wrong to the eye, but is parsed successfully by DecimalFormat.parse
        new ImmutablePair("1,500,5", new BigDecimal(1500.5)),
        new ImmutablePair("1.500.000", new BigDecimal(1500000)),
        new ImmutablePair("1,500,000", new BigDecimal(1500000)),
        new ImmutablePair("1.500.000.25", new BigDecimal(150000025)), // This appears wrong to the eye, but is parsed successfully by DecimalFormat.parse
        new ImmutablePair("1.500.000,25", new BigDecimal(1500000.25)),
        new ImmutablePair("1,500,000,25", new BigDecimal(1500000.25)),
        new ImmutablePair("1,500,000.25", new BigDecimal(1500000.25)),
        new ImmutablePair("15.15.15.25", new BigDecimal(15151525)), // This appears wrong to the eye, but is parsed successfully by DecimalFormat.parse
        new ImmutablePair("15,15,15,25", new BigDecimal(151515.25)), // Here we simplify it to the most right decimal separator...
        new ImmutablePair("15,15,15.25", new BigDecimal(151515.25)), // This appears wrong to the eye, but is parsed successfully by DecimalFormat.parse while trying combinations of decimal/grouping separators
        new ImmutablePair("15.15.15,25", new BigDecimal(151515.25)), // This appears wrong to the eye, but is parsed successfully by DecimalFormat.parse while trying combinations of decimal/grouping separators
        new ImmutablePair(".5", new BigDecimal(5)), // This appears wrong to the eye, but is parsed successfully by DecimalFormat.parse
        new ImmutablePair(",5", new BigDecimal(0.5)),
        new ImmutablePair("'5", new BigDecimal(5)),
        new ImmutablePair("..5", new BigDecimal(5)),
        new ImmutablePair(",,5", new BigDecimal(0.5)),
        new ImmutablePair("''5", new BigDecimal(5))));

    // Testcases for en_US
    testMap.put(Locale.forLanguageTag("en-US"), CollectionUtility.<ImmutablePair<String, BigDecimal>> arrayList(
        new ImmutablePair("500.5", new BigDecimal(500.5)),
        new ImmutablePair("500,5", new BigDecimal(5005)), // This appears wrong to the eye, but is parsed successfully by DecimalFormat.parse
        new ImmutablePair("1.500.5", new BigDecimal(1500.5)),
        new ImmutablePair("1,500,5", new BigDecimal(15005)), // This appears wrong to the eye, but is parsed successfully by DecimalFormat.parse
        new ImmutablePair("1.500.000", new BigDecimal(1500000)),
        new ImmutablePair("1,500,000", new BigDecimal(1500000)),
        new ImmutablePair("1.500.000.25", new BigDecimal(1500000.25)),
        new ImmutablePair("1.500.000,25", new BigDecimal(1500000.25)),
        new ImmutablePair("1,500,000,25", new BigDecimal(150000025)), // This appears wrong to the eye, but is parsed successfully by DecimalFormat.parse
        new ImmutablePair("1,500,000.25", new BigDecimal(1500000.25)),
        new ImmutablePair("15.15.15.25", new BigDecimal(151515.25)), // Here we simplify it to the most right decimal separator...
        new ImmutablePair("15,15,15,25", new BigDecimal(15151525)), // This appears wrong to the eye, but is parsed successfully by DecimalFormat.parse
        new ImmutablePair("15,15,15.25", new BigDecimal(151515.25)), // This appears wrong to the eye, but is parsed successfully by DecimalFormat.parse while trying combinations of decimal/grouping separators
        new ImmutablePair("15.15.15,25", new BigDecimal(151515.25)), // This appears wrong to the eye, but is parsed successfully by DecimalFormat.parse while trying combinations of decimal/grouping separators
        new ImmutablePair(".5", new BigDecimal(0.5)),
        new ImmutablePair(",5", new BigDecimal(5)), // This appears wrong to the eye, but is parsed successfully by DecimalFormat.parse
        new ImmutablePair("'5", new BigDecimal(5)),
        new ImmutablePair("..5", new BigDecimal(0.5)),
        new ImmutablePair(",,5", new BigDecimal(5)), // This appears wrong to the eye, but is parsed successfully by DecimalFormat.parse
        new ImmutablePair("''5", new BigDecimal(5))));

    for (Entry<Locale, ArrayList<ImmutablePair<String, BigDecimal>>> e : testMap.entrySet()) {
      DecimalFormat format = (DecimalFormat) DecimalFormat.getNumberInstance(e.getKey());
      setFormat(format);
      for (ImmutablePair<String, BigDecimal> t : e.getValue()) {
        assertEquals("(" + e.getKey().toString() + ") parsing: " + t.getLeft(), t.getRight(), parseValueInternal(t.getLeft()));
      }
      assertTrue(true);
    }
  }
}
