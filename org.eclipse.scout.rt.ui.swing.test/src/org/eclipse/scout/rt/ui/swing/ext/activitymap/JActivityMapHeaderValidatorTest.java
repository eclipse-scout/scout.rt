package org.eclipse.scout.rt.ui.swing.ext.activitymap;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMock;
import org.eclipse.scout.rt.ui.swing.ext.activitymap.JActivityMapHeaderValidator.ColumnType;
import org.eclipse.scout.rt.ui.swing.ext.activitymap.JActivityMapHeaderValidator.ValidatedTextData;
import org.junit.Before;
import org.junit.Test;

/**
 * This class contains JUnit tests for JActivityMapHeaderValidator
 * 
 * @since 3.9.0
 */
public class JActivityMapHeaderValidatorTest {

  JActivityMapHeaderValidator m_validator;

  @Before
  public void setup() {
    m_validator = new JActivityMapHeaderValidator();
  }

  @Test
  public void testCalculateTextSizeRectanglesNoResize() {
    int[] rectWidths = new int[]{54, 29, 19};
    String[] largeTexts = new String[]{"October 2013", "November 2013", "December 2013"};
    String[] mediumTexts = new String[]{"Oct 13", "Nov 13", "Dec 13"};
    String[] smallTexts = new String[]{"Oct", "Nov", "Dec"};

    List<String> resultTexts = new ArrayList<String>();
    List<Rectangle> validatedRectangles = setupAndCalculateTextSizeRectangles(rectWidths, largeTexts, mediumTexts, smallTexts, resultTexts);

    assertEquals(3, resultTexts.size());
    assertEquals(3, validatedRectangles.size());

    assertEquals(54, validatedRectangles.get(0).width);
    assertEquals(29, validatedRectangles.get(1).width);
    assertEquals(19, validatedRectangles.get(2).width);

    assertEquals("October 2013", resultTexts.get(0));
    assertEquals("Nov 13", resultTexts.get(1));
    assertEquals("Dec", resultTexts.get(2));
  }

  @Test
  public void testCalculateTextSizeRectanglesWithResizeWithEmpty() {
    int[] rectWidths = new int[]{40, 25, 29};
    String[] largeTexts = new String[]{"October 2013", "", "December 2013"};
    String[] mediumTexts = new String[]{"Oct 13", "", "Dec 13"};
    String[] smallTexts = new String[]{"Oct", "", "Dec"};

    List<String> resultTexts = new ArrayList<String>();
    List<Rectangle> validatedRectangles = setupAndCalculateTextSizeRectangles(rectWidths, largeTexts, mediumTexts, smallTexts, resultTexts);

    assertEquals(3, resultTexts.size());
    assertEquals(3, validatedRectangles.size());

    assertEquals(65, validatedRectangles.get(0).width);
    assertEquals(0, validatedRectangles.get(1).width);
    assertEquals(29, validatedRectangles.get(2).width);

    assertEquals("October 2013", resultTexts.get(0));
    assertEquals("", resultTexts.get(1));
    assertEquals("Dec 13", resultTexts.get(2));
  }

  @Test
  public void testCalculateTextSizeRectanglesWithResizeWithNull() {
    int[] rectWidths = new int[]{40, 25, 19};
    String[] largeTexts = new String[]{"October 2013", null, "December 2013"};
    String[] mediumTexts = new String[]{"Oct 13", null, "Dec 13"};
    String[] smallTexts = new String[]{"Oct", null, "Dec"};

    List<String> resultTexts = new ArrayList<String>();
    List<Rectangle> validatedRectangles = setupAndCalculateTextSizeRectangles(rectWidths, largeTexts, mediumTexts, smallTexts, resultTexts);

    assertEquals(3, resultTexts.size());
    assertEquals(3, validatedRectangles.size());

    assertEquals(65, validatedRectangles.get(0).width);
    assertEquals(0, validatedRectangles.get(1).width);
    assertEquals(19, validatedRectangles.get(2).width);

    assertEquals("October 2013", resultTexts.get(0));
    assertNull(resultTexts.get(1));
    assertEquals("Dec", resultTexts.get(2));
  }

  private List<Rectangle> setupAndCalculateTextSizeRectangles(int[] rectWidths, String[] largeTexts, String[] mediumTexts, String[] smallTexts, List<String> resultTexts) {
    FontMetricsTest fm = new FontMetricsTest(null);
    ActivityMapColumnModel columnModel = EasyMock.createMock(ActivityMapColumnModel.class);
    Object[] columns = new Object[]{new Object(), new Object(), new Object()};
    List<Rectangle> rectangles = setupRectangles(rectWidths[0], rectWidths[1], rectWidths[2]);

    expect(columnModel.getMajorColumnText(columns[0], ActivityMapColumnModel.LARGE)).andReturn(largeTexts[0]);
    expect(columnModel.getMajorColumnText(columns[0], ActivityMapColumnModel.MEDIUM)).andReturn(mediumTexts[0]);
    expect(columnModel.getMajorColumnText(columns[0], ActivityMapColumnModel.SMALL)).andReturn(smallTexts[0]);

    expect(columnModel.getMajorColumnText(columns[1], ActivityMapColumnModel.LARGE)).andReturn(largeTexts[1]);
    expect(columnModel.getMajorColumnText(columns[1], ActivityMapColumnModel.MEDIUM)).andReturn(mediumTexts[1]);
    expect(columnModel.getMajorColumnText(columns[1], ActivityMapColumnModel.SMALL)).andReturn(smallTexts[1]);

    expect(columnModel.getMajorColumnText(columns[2], ActivityMapColumnModel.LARGE)).andReturn(largeTexts[2]);
    expect(columnModel.getMajorColumnText(columns[2], ActivityMapColumnModel.MEDIUM)).andReturn(mediumTexts[2]);
    expect(columnModel.getMajorColumnText(columns[2], ActivityMapColumnModel.SMALL)).andReturn(smallTexts[2]);

    EasyMock.replay(columnModel);

    return m_validator.calculateTextSizeRectangles(columns, ColumnType.MAJOR, columnModel, rectangles, fm, resultTexts);
  }

  @Test
  public void testRetrieveMajorColumnTexts() {
    ActivityMapColumnModel columnModel = EasyMock.createMock(ActivityMapColumnModel.class);
    Object majorColumn1 = new Object();
    Object majorColumn2 = new Object();
    Object majorColumn3 = new Object();

    expect(columnModel.getMajorColumnText(majorColumn1, ActivityMapColumnModel.LARGE)).andReturn("MajorColumn1 Large Text");
    expect(columnModel.getMajorColumnText(majorColumn1, ActivityMapColumnModel.MEDIUM)).andReturn("MajorColumn1 Medium Text");
    expect(columnModel.getMajorColumnText(majorColumn1, ActivityMapColumnModel.SMALL)).andReturn("MajorColumn1 Small Text");

    expect(columnModel.getMajorColumnText(majorColumn2, ActivityMapColumnModel.LARGE)).andReturn("MajorColumn2 Large Text");
    expect(columnModel.getMajorColumnText(majorColumn2, ActivityMapColumnModel.MEDIUM)).andReturn(null);
    expect(columnModel.getMajorColumnText(majorColumn2, ActivityMapColumnModel.SMALL)).andReturn("");

    expect(columnModel.getMajorColumnText(majorColumn3, ActivityMapColumnModel.LARGE)).andReturn("");
    expect(columnModel.getMajorColumnText(majorColumn3, ActivityMapColumnModel.MEDIUM)).andReturn("MajorColumn3 Medium Text");
    expect(columnModel.getMajorColumnText(majorColumn3, ActivityMapColumnModel.SMALL)).andReturn(null);

    Object[] columns = new Object[]{majorColumn1, majorColumn2, majorColumn3};

    EasyMock.replay(columnModel);

    List<String> largeTexts = new ArrayList<String>();
    List<String> mediumTexts = new ArrayList<String>();
    List<String> smallTexts = new ArrayList<String>();

    m_validator.retrieveColumnTexts(columns, ColumnType.MAJOR, columnModel, largeTexts, mediumTexts, smallTexts);

    assertEquals(3, largeTexts.size());
    assertEquals(3, mediumTexts.size());
    assertEquals(3, smallTexts.size());

    assertEquals("MajorColumn1 Large Text", largeTexts.get(0));
    assertEquals("MajorColumn1 Medium Text", mediumTexts.get(0));
    assertEquals("MajorColumn1 Small Text", smallTexts.get(0));

    assertEquals("MajorColumn2 Large Text", largeTexts.get(1));
    assertNull(mediumTexts.get(1));
    assertEquals("", smallTexts.get(1));

    assertEquals("", largeTexts.get(2));
    assertEquals("MajorColumn3 Medium Text", mediumTexts.get(2));
    assertNull(smallTexts.get(2));
  }

  @Test
  public void testRetrieveMinorColumnTexts() {
    ActivityMapColumnModel columnModel = EasyMock.createMock(ActivityMapColumnModel.class);
    Object minorColumn1 = new Object();
    Object minorColumn2 = new Object();
    Object minorColumn3 = new Object();

    expect(columnModel.getMinorColumnText(minorColumn1, ActivityMapColumnModel.LARGE)).andReturn("MinorColumn1 Large Text");
    expect(columnModel.getMinorColumnText(minorColumn1, ActivityMapColumnModel.MEDIUM)).andReturn("MinorColumn1 Medium Text");
    expect(columnModel.getMinorColumnText(minorColumn1, ActivityMapColumnModel.SMALL)).andReturn("MinorColumn1 Small Text");

    expect(columnModel.getMinorColumnText(minorColumn2, ActivityMapColumnModel.LARGE)).andReturn("MinorColumn2 Large Text");
    expect(columnModel.getMinorColumnText(minorColumn2, ActivityMapColumnModel.MEDIUM)).andReturn(null);
    expect(columnModel.getMinorColumnText(minorColumn2, ActivityMapColumnModel.SMALL)).andReturn("");

    expect(columnModel.getMinorColumnText(minorColumn3, ActivityMapColumnModel.LARGE)).andReturn("");
    expect(columnModel.getMinorColumnText(minorColumn3, ActivityMapColumnModel.MEDIUM)).andReturn("MinorColumn3 Medium Text");
    expect(columnModel.getMinorColumnText(minorColumn3, ActivityMapColumnModel.SMALL)).andReturn(null);

    Object[] columns = new Object[]{minorColumn1, minorColumn2, minorColumn3};

    EasyMock.replay(columnModel);

    List<String> largeTexts = new ArrayList<String>();
    List<String> mediumTexts = new ArrayList<String>();
    List<String> smallTexts = new ArrayList<String>();

    m_validator.retrieveColumnTexts(columns, ColumnType.MINOR, columnModel, largeTexts, mediumTexts, smallTexts);

    assertEquals(3, largeTexts.size());
    assertEquals(3, mediumTexts.size());
    assertEquals(3, smallTexts.size());

    assertEquals("MinorColumn1 Large Text", largeTexts.get(0));
    assertEquals("MinorColumn1 Medium Text", mediumTexts.get(0));
    assertEquals("MinorColumn1 Small Text", smallTexts.get(0));

    assertEquals("MinorColumn2 Large Text", largeTexts.get(1));
    assertNull(mediumTexts.get(1));
    assertEquals("", smallTexts.get(1));

    assertEquals("", largeTexts.get(2));
    assertEquals("MinorColumn3 Medium Text", mediumTexts.get(2));
    assertNull(smallTexts.get(2));
  }

  @Test
  public void testValidateAllTexts() {
    testValidateAllTextsAllFitLarge();
    testValidateAllTextsAllFitLargeMedium();
    testValidateAllTextsAllFitLargeMediumSmall();
  }

  private void testValidateAllTextsAllFitLarge() {
    FontMetricsTest fm = new FontMetricsTest(null);
    List<Rectangle> rectangles = setupRectangles(54, 54, 54);
    List<String> largeTexts = setupTexts("October 2013", "November 2013", "December 2013");
    List<String> mediumTexts = setupTexts("Oct 13", "Nov 13", "Dec 13");
    List<String> smallTexts = setupTexts("Oct", "Nov", "Dec");

    Map<Integer, ValidatedTextData> textDataMap = m_validator.validateAllTexts(fm, rectangles, largeTexts, mediumTexts, smallTexts);

    assertEquals(1, textDataMap.size());

    ValidatedTextData largeTextData = textDataMap.get(ActivityMapColumnModel.LARGE);
    ValidatedTextData mediumTextData = textDataMap.get(ActivityMapColumnModel.MEDIUM);
    ValidatedTextData smallTextData = textDataMap.get(ActivityMapColumnModel.SMALL);

    assertNotNull(largeTextData);
    assertNull(mediumTextData);
    assertNull(smallTextData);

    assertEquals(3, largeTextData.getTexts().size());
    assertEquals(3, largeTextData.getTextsFit().size());
    assertEquals(3, largeTextData.getValidatedRectangles().size());

    assertEquals("October 2013", largeTextData.getTexts().get(0));
    assertEquals("November 2013", largeTextData.getTexts().get(1));
    assertEquals("December 2013", largeTextData.getTexts().get(2));

    assertTrue(largeTextData.getTextsFit().get(0));
    assertTrue(largeTextData.getTextsFit().get(1));
    assertTrue(largeTextData.getTextsFit().get(2));

    assertEquals(rectangles.get(0), largeTextData.getValidatedRectangles().get(0));
    assertEquals(rectangles.get(1), largeTextData.getValidatedRectangles().get(1));
    assertEquals(rectangles.get(2), largeTextData.getValidatedRectangles().get(2));
  }

  private void testValidateAllTextsAllFitLargeMedium() {
    FontMetricsTest fm = new FontMetricsTest(null);
    List<Rectangle> rectangles = setupRectangles(54, 40, 54);
    List<String> largeTexts = setupTexts("October 2013", "November 2013", "December 2013");
    List<String> mediumTexts = setupTexts("Oct 13", "Nov 13", "Dec 13");
    List<String> smallTexts = setupTexts("Oct", "Nov", "Dec");

    Map<Integer, ValidatedTextData> textDataMap = m_validator.validateAllTexts(fm, rectangles, largeTexts, mediumTexts, smallTexts);

    assertEquals(2, textDataMap.size());

    ValidatedTextData largeTextData = textDataMap.get(ActivityMapColumnModel.LARGE);
    ValidatedTextData mediumTextData = textDataMap.get(ActivityMapColumnModel.MEDIUM);
    ValidatedTextData smallTextData = textDataMap.get(ActivityMapColumnModel.SMALL);

    assertNotNull(largeTextData);
    assertNotNull(mediumTextData);
    assertNull(smallTextData);

    assertEquals(3, largeTextData.getTexts().size());
    assertEquals(3, largeTextData.getTextsFit().size());
    assertEquals(3, largeTextData.getValidatedRectangles().size());

    assertEquals(rectangles.get(0), largeTextData.getValidatedRectangles().get(0));
    assertEquals(rectangles.get(1), largeTextData.getValidatedRectangles().get(1));
    assertEquals(rectangles.get(2), largeTextData.getValidatedRectangles().get(2));

    assertEquals("October 2013", largeTextData.getTexts().get(0));
    assertEquals("November 2013", largeTextData.getTexts().get(1));
    assertEquals("December 2013", largeTextData.getTexts().get(2));

    assertTrue(largeTextData.getTextsFit().get(0));
    assertFalse(largeTextData.getTextsFit().get(1));
    assertTrue(largeTextData.getTextsFit().get(2));

    assertEquals(3, mediumTextData.getTexts().size());
    assertEquals(3, mediumTextData.getTextsFit().size());
    assertEquals(3, mediumTextData.getValidatedRectangles().size());

    assertEquals(rectangles.get(0), mediumTextData.getValidatedRectangles().get(0));
    assertEquals(rectangles.get(1), mediumTextData.getValidatedRectangles().get(1));
    assertEquals(rectangles.get(2), mediumTextData.getValidatedRectangles().get(2));

    assertEquals("Oct 13", mediumTextData.getTexts().get(0));
    assertEquals("Nov 13", mediumTextData.getTexts().get(1));
    assertEquals("Dec 13", mediumTextData.getTexts().get(2));

    assertTrue(mediumTextData.getTextsFit().get(0));
    assertTrue(mediumTextData.getTextsFit().get(1));
    assertTrue(mediumTextData.getTextsFit().get(2));
  }

  private void testValidateAllTextsAllFitLargeMediumSmall() {
    FontMetricsTest fm = new FontMetricsTest(null);
    List<Rectangle> rectangles = setupRectangles(54, 40, 19);
    List<String> largeTexts = setupTexts("October 2013", "November 2013", "December 2013");
    List<String> mediumTexts = setupTexts("Oct 13", "Nov 13", "Dec 13");
    List<String> smallTexts = setupTexts("Oct", "Nov", "Dec");

    Map<Integer, ValidatedTextData> textDataMap = m_validator.validateAllTexts(fm, rectangles, largeTexts, mediumTexts, smallTexts);

    assertEquals(3, textDataMap.size());

    ValidatedTextData largeTextData = textDataMap.get(ActivityMapColumnModel.LARGE);
    ValidatedTextData mediumTextData = textDataMap.get(ActivityMapColumnModel.MEDIUM);
    ValidatedTextData smallTextData = textDataMap.get(ActivityMapColumnModel.SMALL);

    assertNotNull(largeTextData);
    assertNotNull(mediumTextData);
    assertNotNull(smallTextData);

    assertEquals(3, largeTextData.getTexts().size());
    assertEquals(3, largeTextData.getTextsFit().size());
    assertEquals(3, largeTextData.getValidatedRectangles().size());

    assertEquals(rectangles.get(0), largeTextData.getValidatedRectangles().get(0));
    assertEquals(rectangles.get(1), largeTextData.getValidatedRectangles().get(1));
    assertEquals(rectangles.get(2), largeTextData.getValidatedRectangles().get(2));

    assertEquals("October 2013", largeTextData.getTexts().get(0));
    assertEquals("November 2013", largeTextData.getTexts().get(1));
    assertEquals("December 2013", largeTextData.getTexts().get(2));

    assertTrue(largeTextData.getTextsFit().get(0));
    assertFalse(largeTextData.getTextsFit().get(1));
    assertFalse(largeTextData.getTextsFit().get(2));

    assertEquals(3, mediumTextData.getTexts().size());
    assertEquals(3, mediumTextData.getTextsFit().size());
    assertEquals(3, mediumTextData.getValidatedRectangles().size());

    assertEquals(rectangles.get(0), mediumTextData.getValidatedRectangles().get(0));
    assertEquals(rectangles.get(1), mediumTextData.getValidatedRectangles().get(1));
    assertEquals(rectangles.get(2), mediumTextData.getValidatedRectangles().get(2));

    assertEquals("Oct 13", mediumTextData.getTexts().get(0));
    assertEquals("Nov 13", mediumTextData.getTexts().get(1));
    assertEquals("Dec 13", mediumTextData.getTexts().get(2));

    assertTrue(mediumTextData.getTextsFit().get(0));
    assertTrue(mediumTextData.getTextsFit().get(1));
    assertFalse(mediumTextData.getTextsFit().get(2));

    assertEquals(3, smallTextData.getTexts().size());
    assertEquals(3, smallTextData.getTextsFit().size());
    assertEquals(3, smallTextData.getValidatedRectangles().size());

    assertEquals(rectangles.get(0), smallTextData.getValidatedRectangles().get(0));
    assertEquals(rectangles.get(1), smallTextData.getValidatedRectangles().get(1));
    assertEquals(rectangles.get(2), smallTextData.getValidatedRectangles().get(2));

    assertEquals("Oct", smallTextData.getTexts().get(0));
    assertEquals("Nov", smallTextData.getTexts().get(1));
    assertEquals("Dec", smallTextData.getTexts().get(2));

    assertTrue(smallTextData.getTextsFit().get(0));
    assertTrue(smallTextData.getTextsFit().get(1));
    assertTrue(smallTextData.getTextsFit().get(2));
  }

  @Test
  public void testValidateTexts() {
    testValidateTextsAllValid();
    testValidateTextsMediumSmallValid();
    testValidateTextsSmallValid();
    testValidateTextsAllInvalid();
    testValidateTextsResizeWidths();
  }

  private void testValidateTextsAllValid() {
    FontMetricsTest fm = new FontMetricsTest(null);
    List<Rectangle> rectangles = setupRectangles(54, 29, 19);
    List<String> texts = setupTexts("November 2013", "Nov 13", "Nov");
    ValidatedTextData textData = m_validator.validateTexts(fm, texts, rectangles);

    assertEquals(3, textData.getTexts().size());
    assertEquals(3, textData.getTextsFit().size());
    assertEquals(3, textData.getValidatedRectangles().size());

    assertEquals("November 2013", textData.getTexts().get(0));
    assertEquals("Nov 13", textData.getTexts().get(1));
    assertEquals("Nov", textData.getTexts().get(2));

    assertTrue(textData.getTextsFit().get(0));
    assertTrue(textData.getTextsFit().get(1));
    assertTrue(textData.getTextsFit().get(2));

    assertEquals(rectangles.get(0), textData.getValidatedRectangles().get(0));
    assertEquals(rectangles.get(1), textData.getValidatedRectangles().get(1));
    assertEquals(rectangles.get(2), textData.getValidatedRectangles().get(2));
  }

  private void testValidateTextsMediumSmallValid() {
    FontMetricsTest fm = new FontMetricsTest(null);
    List<Rectangle> rectangles = setupRectangles(53, 29, 19);
    List<String> texts = setupTexts("November 2013", "Nov 13", "Nov");
    ValidatedTextData textData = m_validator.validateTexts(fm, texts, rectangles);

    assertEquals(3, textData.getTexts().size());
    assertEquals(3, textData.getTextsFit().size());
    assertEquals(3, textData.getValidatedRectangles().size());

    assertEquals("November 2013", textData.getTexts().get(0));
    assertEquals("Nov 13", textData.getTexts().get(1));
    assertEquals("Nov", textData.getTexts().get(2));

    assertFalse(textData.getTextsFit().get(0));
    assertTrue(textData.getTextsFit().get(1));
    assertTrue(textData.getTextsFit().get(2));

    assertEquals(rectangles.get(0), textData.getValidatedRectangles().get(0));
    assertEquals(rectangles.get(1), textData.getValidatedRectangles().get(1));
    assertEquals(rectangles.get(2), textData.getValidatedRectangles().get(2));
  }

  private void testValidateTextsSmallValid() {
    FontMetricsTest fm = new FontMetricsTest(null);
    List<Rectangle> rectangles = setupRectangles(53, 28, 19);
    List<String> texts = setupTexts("November 2013", "Nov 13", "Nov");
    ValidatedTextData textData = m_validator.validateTexts(fm, texts, rectangles);

    assertEquals(3, textData.getTexts().size());
    assertEquals(3, textData.getTextsFit().size());
    assertEquals(3, textData.getValidatedRectangles().size());

    assertEquals("November 2013", textData.getTexts().get(0));
    assertEquals("Nov 13", textData.getTexts().get(1));
    assertEquals("Nov", textData.getTexts().get(2));

    assertFalse(textData.getTextsFit().get(0));
    assertFalse(textData.getTextsFit().get(1));
    assertTrue(textData.getTextsFit().get(2));

    assertEquals(rectangles.get(0), textData.getValidatedRectangles().get(0));
    assertEquals(rectangles.get(1), textData.getValidatedRectangles().get(1));
    assertEquals(rectangles.get(2), textData.getValidatedRectangles().get(2));
  }

  private void testValidateTextsAllInvalid() {
    FontMetricsTest fm = new FontMetricsTest(null);
    List<Rectangle> rectangles = setupRectangles(53, 28, 18);
    List<String> texts = setupTexts("November 2013", "Nov 13", "Nov");
    ValidatedTextData textData = m_validator.validateTexts(fm, texts, rectangles);

    assertEquals(3, textData.getTexts().size());
    assertEquals(3, textData.getTextsFit().size());
    assertEquals(3, textData.getValidatedRectangles().size());

    assertEquals("November 2013", textData.getTexts().get(0));
    assertEquals("Nov 13", textData.getTexts().get(1));
    assertEquals("Nov", textData.getTexts().get(2));

    assertFalse(textData.getTextsFit().get(0));
    assertFalse(textData.getTextsFit().get(1));
    assertFalse(textData.getTextsFit().get(2));

    assertEquals(rectangles.get(0), textData.getValidatedRectangles().get(0));
    assertEquals(rectangles.get(1), textData.getValidatedRectangles().get(1));
    assertEquals(rectangles.get(2), textData.getValidatedRectangles().get(2));
  }

  private void testValidateTextsResizeWidths() {
    FontMetricsTest fm = new FontMetricsTest(null);
    List<Rectangle> rectangles = setupRectangles(40, 15, 19);
    List<String> texts = setupTexts("November 2013", "", "Nov");
    ValidatedTextData textData = m_validator.validateTexts(fm, texts, rectangles);

    assertEquals(3, textData.getTexts().size());
    assertEquals(3, textData.getTextsFit().size());
    assertEquals(3, textData.getValidatedRectangles().size());

    assertEquals("November 2013", textData.getTexts().get(0));
    assertEquals("", textData.getTexts().get(1));
    assertEquals("Nov", textData.getTexts().get(2));

    assertTrue(textData.getTextsFit().get(0));
    assertTrue(textData.getTextsFit().get(1));
    assertTrue(textData.getTextsFit().get(2));

    assertEquals(rectangles.get(0), textData.getValidatedRectangles().get(0));
    assertEquals(rectangles.get(1), textData.getValidatedRectangles().get(1));
    assertEquals(rectangles.get(2), textData.getValidatedRectangles().get(2));

    assertEquals(55, textData.getValidatedRectangles().get(0).width);
    assertEquals(0, textData.getValidatedRectangles().get(1).width);
    assertEquals(19, textData.getValidatedRectangles().get(2).width);
  }

  private List<Rectangle> setupRectangles(int... rectangleWidths) {
    List<Rectangle> rectangles = new ArrayList<Rectangle>();
    for (int width : rectangleWidths) {
      rectangles.add(new Rectangle(width, 0));
    }
    return rectangles;
  }

  private List<String> setupTexts(String... texts) {
    List<String> textList = new ArrayList<String>();
    for (String text : texts) {
      textList.add(text);
    }
    return textList;
  }

  @Test
  public void testMergeTexts() {
    List<String> largeTexts = createListMock();
    List<String> mediumTexts = createListMock();
    List<String> smallTexts = createListMock();

    List<Boolean> largeFitTexts = createListMock();
    List<Boolean> mediumFitTexts = createListMock();
    List<Boolean> smallFitTexts = createListMock();

    List<Rectangle> largeRectangles = createListMock();
    List<Rectangle> mediumRectangles = createListMock();
    List<Rectangle> smallRectangles = createListMock();

    Rectangle largeRect1 = new Rectangle();
    Rectangle largeRect2 = new Rectangle();
    Rectangle largeRect3 = new Rectangle();
    Rectangle largeRect4 = new Rectangle();

    Rectangle mediumRect1 = new Rectangle();
    Rectangle mediumRect2 = new Rectangle();
    Rectangle mediumRect3 = new Rectangle();
    Rectangle mediumRect4 = new Rectangle();

    Rectangle smallRect1 = new Rectangle();
    Rectangle smallRect2 = new Rectangle();
    Rectangle smallRect3 = new Rectangle();
    Rectangle smallRect4 = new Rectangle();

    expect(largeTexts.size()).andReturn(4).anyTimes();
    expect(largeTexts.get(0)).andReturn("Large text that fits");
    expect(largeTexts.get(1)).andReturn("Very very very large text that does not fit");
    expect(largeTexts.get(2)).andReturn("Very very very large text that does not fit");
    expect(largeTexts.get(3)).andReturn("Very very very large text that does not fit");

    expect(largeFitTexts.get(0)).andReturn(true);
    expect(largeFitTexts.get(1)).andReturn(false);
    expect(largeFitTexts.get(2)).andReturn(false);
    expect(largeFitTexts.get(3)).andReturn(false);

    expect(largeRectangles.get(0)).andReturn(largeRect1);
    expect(largeRectangles.get(1)).andReturn(largeRect2);
    expect(largeRectangles.get(2)).andReturn(largeRect3);
    expect(largeRectangles.get(3)).andReturn(largeRect4);

    expect(mediumTexts.size()).andReturn(4).anyTimes();
    expect(mediumTexts.get(0)).andReturn("Medium text that fits");
    expect(mediumTexts.get(1)).andReturn("Medium text that fits");
    expect(mediumTexts.get(2)).andReturn("Very very long medium text that does not fit");
    expect(mediumTexts.get(3)).andReturn("Very very long medium text that does not fit");

    expect(mediumFitTexts.get(0)).andReturn(true);
    expect(mediumFitTexts.get(1)).andReturn(true);
    expect(mediumFitTexts.get(2)).andReturn(false);
    expect(mediumFitTexts.get(3)).andReturn(false);

    expect(mediumRectangles.get(0)).andReturn(mediumRect1);
    expect(mediumRectangles.get(1)).andReturn(mediumRect2);
    expect(mediumRectangles.get(2)).andReturn(mediumRect3);
    expect(mediumRectangles.get(3)).andReturn(mediumRect4);

    expect(smallTexts.size()).andReturn(3).anyTimes();
    expect(smallTexts.get(0)).andReturn("Small text that fits");
    expect(smallTexts.get(1)).andReturn("Small text that fits");
    expect(smallTexts.get(2)).andReturn("Small text that fits");
    expect(smallTexts.get(3)).andReturn("Very very long small text that does not fit but it taken anyways because its the fallback case");

    expect(smallFitTexts.get(0)).andReturn(true);
    expect(smallFitTexts.get(1)).andReturn(true);
    expect(smallFitTexts.get(2)).andReturn(true);
    expect(smallFitTexts.get(3)).andReturn(false);

    expect(smallRectangles.get(0)).andReturn(smallRect1);
    expect(smallRectangles.get(1)).andReturn(smallRect2);
    expect(smallRectangles.get(2)).andReturn(smallRect3);
    expect(smallRectangles.get(3)).andReturn(smallRect4);

    EasyMock.replay(largeTexts, mediumTexts, smallTexts, largeFitTexts, mediumFitTexts, smallFitTexts, largeRectangles, mediumRectangles, smallRectangles);

    ValidatedTextData largeTextData = new ValidatedTextData(largeTexts, largeFitTexts, largeRectangles);
    ValidatedTextData mediumTextData = new ValidatedTextData(mediumTexts, mediumFitTexts, mediumRectangles);
    ValidatedTextData smallTextData = new ValidatedTextData(smallTexts, smallFitTexts, smallRectangles);

    Map<Integer, ValidatedTextData> textDataMap = new HashMap<Integer, ValidatedTextData>();
    textDataMap.put(ActivityMapColumnModel.LARGE, largeTextData);
    textDataMap.put(ActivityMapColumnModel.MEDIUM, mediumTextData);
    textDataMap.put(ActivityMapColumnModel.SMALL, smallTextData);

    List<String> resultTexts = new ArrayList<String>();
    List<Rectangle> resultRectangles = new ArrayList<Rectangle>();

    resultRectangles = m_validator.mergeTexts(textDataMap, resultTexts);

    assertEquals(4, resultTexts.size());
    assertEquals(4, resultRectangles.size());

    assertEquals("Large text that fits", resultTexts.get(0));
    assertEquals(largeRect1, resultRectangles.get(0));

    assertEquals("Medium text that fits", resultTexts.get(1));
    assertEquals(mediumRect2, resultRectangles.get(1));

    assertEquals("Small text that fits", resultTexts.get(2));
    assertEquals(smallRect3, resultRectangles.get(2));

    assertEquals("Very very long small text that does not fit but it taken anyways because its the fallback case", resultTexts.get(3));
    assertEquals(smallRect4, resultRectangles.get(3));
  }

  @SuppressWarnings("unchecked")
  private <T> List<T> createListMock() {
    return (List<T>) EasyMock.createMock(List.class);
  }

  @Test
  public void testAddTextAndRectangleIfFit() {
    List<String> texts = new ArrayList<String>();
    List<Boolean> textsFit = new ArrayList<Boolean>();
    List<Rectangle> validatedRectangles = new ArrayList<Rectangle>();

    texts.add("A short text that fits");
    texts.add("A very very very very very long text that does not fit");
    texts.add("");
    texts.add(null);

    textsFit.add(true);
    textsFit.add(false);
    textsFit.add(true);
    textsFit.add(false);

    Rectangle rect1 = new Rectangle();
    Rectangle rect2 = new Rectangle();
    Rectangle rect3 = new Rectangle();

    validatedRectangles.add(rect1);
    validatedRectangles.add(rect2);
    validatedRectangles.add(rect3);
    validatedRectangles.add(null);

    ValidatedTextData textData = new ValidatedTextData(texts, textsFit, validatedRectangles);

    List<String> resultTexts = new ArrayList<String>();
    List<Rectangle> resultRectangles = new ArrayList<Rectangle>();

    boolean result = m_validator.addTextAndRectangleIfFit(textData, 0, resultTexts, resultRectangles);
    assertTrue(result);
    assertEquals(1, resultTexts.size());
    assertEquals(1, resultRectangles.size());
    assertEquals("A short text that fits", resultTexts.get(0));
    assertEquals(rect1, resultRectangles.get(0));

    result = m_validator.addTextAndRectangleIfFit(textData, 1, resultTexts, resultRectangles);
    assertFalse(result);
    assertEquals(1, resultTexts.size());
    assertEquals(1, resultRectangles.size());

    result = m_validator.addTextAndRectangleIfFit(textData, 2, resultTexts, resultRectangles);
    assertTrue(result);
    assertEquals(2, resultTexts.size());
    assertEquals(2, resultRectangles.size());
    assertEquals("", resultTexts.get(1));
    assertEquals(rect3, resultRectangles.get(1));

    result = m_validator.addTextAndRectangleIfFit(textData, 3, resultTexts, resultRectangles);
    assertFalse(result);
    assertEquals(2, resultTexts.size());
    assertEquals(2, resultRectangles.size());
  }

  @Test
  public void testAddTextAndRectangle() {
    List<String> texts = new ArrayList<String>();
    List<Boolean> textsFit = new ArrayList<Boolean>();
    List<Rectangle> validatedRectangles = new ArrayList<Rectangle>();

    texts.add("Text 1");
    texts.add("Text 2");
    texts.add("");
    texts.add(null);

    Rectangle rect1 = new Rectangle();
    Rectangle rect2 = new Rectangle();
    Rectangle rect3 = new Rectangle();

    validatedRectangles.add(rect1);
    validatedRectangles.add(rect2);
    validatedRectangles.add(rect3);
    validatedRectangles.add(null);

    ValidatedTextData textData = new ValidatedTextData(texts, textsFit, validatedRectangles);

    List<String> resultTexts = new ArrayList<String>();
    List<Rectangle> resultRectangles = new ArrayList<Rectangle>();

    m_validator.addTextAndRectangle(textData, 0, resultTexts, resultRectangles);
    assertEquals(1, resultTexts.size());
    assertEquals(1, resultRectangles.size());
    assertEquals("Text 1", resultTexts.get(0));
    assertEquals(rect1, resultRectangles.get(0));

    m_validator.addTextAndRectangle(textData, 1, resultTexts, resultRectangles);
    assertEquals(2, resultTexts.size());
    assertEquals(2, resultRectangles.size());
    assertEquals("Text 2", resultTexts.get(1));
    assertEquals(rect2, resultRectangles.get(1));

    m_validator.addTextAndRectangle(textData, 2, resultTexts, resultRectangles);
    assertEquals(3, resultTexts.size());
    assertEquals(3, resultRectangles.size());
    assertEquals("", resultTexts.get(2));
    assertEquals(rect3, resultRectangles.get(2));

    m_validator.addTextAndRectangle(textData, 3, resultTexts, resultRectangles);
    assertEquals(4, resultTexts.size());
    assertEquals(4, resultRectangles.size());
    assertNull(resultTexts.get(3));
    assertNull(resultRectangles.get(3));
  }

  /**
   * Since EasyMock is not able to mock abstract classes, this class subtypes from FontMetrics
   * and implements an own mocking method for stringWidth which is used in
   * 
   * @link{JActivityMapHeaderValidator.validateAllTexts
   *                                                    and @link{JActivityMapHeaderValidator.validateTexts}
   *                                                    As soon as Mockito is used as a mocking framework in Scout 3.10,
   *                                                    this class should be removed and replaced by a
   *                                                    real
   *                                                    mock.
   * @deprecated will be replaced with Mockito mock in 3.10
   */
  @Deprecated
  class FontMetricsTest extends FontMetrics {
    private static final long serialVersionUID = 1L;

    protected FontMetricsTest(Font font) {
      super(font);
    }

    @Override
    public int stringWidth(String str) {
      if ("October 2013".equals(str) || "November 2013".equals(str) || "December 2013".equals(str)) {
        return 50;
      }
      else if ("Oct 13".equals(str) || "Nov 13".equals(str) || "Dec 13".equals(str)) {
        return 25;
      }
      else if ("Oct".equals(str) || "Nov".equals(str) || "Dec".equals(str)) {
        return 15;
      }
      return 0;
    }
  }
}
