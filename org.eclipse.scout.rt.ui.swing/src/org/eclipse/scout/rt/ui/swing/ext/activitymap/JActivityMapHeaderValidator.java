/*******************************************************************************
 * Copyright (c) 2010,2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swing.ext.activitymap;

import java.awt.FontMetrics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

/**
 * The JActivityMapHeaderValidator is responsible for calculating and validating the correct
 * text sizes so that they fit into the their given drawing space.
 * Depending on the available drawing space, to text sizes large, medium or small are chosen
 * dynamically.
 * 
 * @since 3.8.3
 */
class JActivityMapHeaderValidator {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JActivityMapHeaderValidator.class);

  enum ColumnType {
    MAJOR, MINOR
  }

  /**
   * Calculates the text sizes so that they fit into the given rectangle.
   * In case a text is empty or not available, more space is given to the previous rectangle.
   * 
   * @param texts
   *          is an out parameter
   * @return list of validated and resized rectangles
   */
  List<Rectangle> calculateTextSizeRectangles(Object[] columns, ColumnType columnType, ActivityMapColumnModel columnModel, List<Rectangle> textRects, FontMetrics fm, List<String> texts) {
    List<String> largeTexts = new ArrayList<String>();
    List<String> mediumTexts = new ArrayList<String>();
    List<String> smallTexts = new ArrayList<String>();

    retrieveColumnTexts(columns, columnType, columnModel, largeTexts, mediumTexts, smallTexts);

    LOG.debug("FontMetrics {0}", fm);
    LOG.debug("textrects {0}", textRects.size());

    for (Rectangle rect : textRects) {
      LOG.debug(rect.toString());
    }

    for (int i = 0; i < largeTexts.size(); i++) {
      LOG.debug("large {0}, medium {1}, small {2}", new Object[]{largeTexts.get(i), mediumTexts.get(i), smallTexts.get(i)});
    }

    Map<Integer, ValidatedTextData> validatedTextDataMap = validateAllTexts(fm, textRects, largeTexts, mediumTexts, smallTexts);
    List<Rectangle> validatedRects = mergeTexts(validatedTextDataMap, texts);

    return validatedRects;
  }

  /**
   * Retrieves the texts of all sizes (large, medium and small) of all given columns.
   * The type of the column (major or minor) is passed as parameter.
   * 
   * @param largeTexts
   *          is an out parameter
   * @param mediumTexts
   *          is an out parameter
   * @param smallTexts
   *          is an out parameter
   */
  void retrieveColumnTexts(Object[] columns, ColumnType columnType, ActivityMapColumnModel columnModel, List<String> largeTexts, List<String> mediumTexts, List<String> smallTexts) {
    if (columnType == ColumnType.MAJOR) {
      for (Object o : columns) {
        largeTexts.add(columnModel.getMajorColumnText(o, ActivityMapColumnModel.LARGE));
        mediumTexts.add(columnModel.getMajorColumnText(o, ActivityMapColumnModel.MEDIUM));
        smallTexts.add(columnModel.getMajorColumnText(o, ActivityMapColumnModel.SMALL));
      }
    }
    else if (columnType == ColumnType.MINOR) {
      for (Object o : columns) {
        largeTexts.add(columnModel.getMinorColumnText(o, ActivityMapColumnModel.LARGE));
        mediumTexts.add(columnModel.getMinorColumnText(o, ActivityMapColumnModel.MEDIUM));
        smallTexts.add(columnModel.getMinorColumnText(o, ActivityMapColumnModel.SMALL));
      }
    }
  }

  Map<Integer, ValidatedTextData> validateAllTexts(FontMetrics fm, List<Rectangle> rects, List<String> largeTexts, List<String> mediumTexts, List<String> smallTexts) {
    Map<Integer, ValidatedTextData> validatedTextDataMap = new HashMap<Integer, ValidatedTextData>();
    ValidatedTextData largeTextData = validateTexts(fm, largeTexts, rects);

    validatedTextDataMap.put(ActivityMapColumnModel.LARGE, largeTextData);
    if (!largeTextData.doAllTextsFitIntoRectangle()) {
      ValidatedTextData mediumTextData = validateTexts(fm, mediumTexts, rects);
      validatedTextDataMap.put(ActivityMapColumnModel.MEDIUM, mediumTextData);
      if (!mediumTextData.doAllTextsFitIntoRectangle()) {
        ValidatedTextData smallTextData = validateTexts(fm, smallTexts, rects);
        validatedTextDataMap.put(ActivityMapColumnModel.SMALL, smallTextData);
      }
    }
    return validatedTextDataMap;
  }

  /**
   * Validates of the texts fit into the given rectangles.
   * Returns a ValidatedTextData object that contains the validated information.
   */
  ValidatedTextData validateTexts(FontMetrics fm, List<String> texts, List<Rectangle> rects) {
    List<Boolean> textsFit = new ArrayList<Boolean>();
    List<Rectangle> validatedRects = new ArrayList<Rectangle>();
    validatedRects.addAll(rects);

    for (int i = 0; i < validatedRects.size(); i++) {
      boolean isTextTooLarge = false;
      String text = texts.get(i);
      if (text != null && text.length() > 0) {
        int k = i + 1;
        while (k < validatedRects.size() && (texts.get(k) == null || texts.get(k).length() == 0)) {
          validatedRects.get(i).width += validatedRects.get(k).width;
          validatedRects.get(k).width = 0;
          k++;
        }
        isTextTooLarge = fm.stringWidth(text) > validatedRects.get(i).width - 4;

        LOG.debug(isTextTooLarge + ", text " + text + ", stringwidth " + fm.stringWidth(text) + " validaterect " + (validatedRects.get(i).width - 4));

      }
      textsFit.add(Boolean.valueOf(!isTextTooLarge));
    }

    return new ValidatedTextData(texts, textsFit, validatedRects);
  }

  /**
   * Uses the ValidatedTextData object to decide which text and rectangle size should be chosen.
   * 
   * @param texts
   *          is an out parameter
   * @return list of validated rectangles
   */
  List<Rectangle> mergeTexts(Map<Integer, ValidatedTextData> validatedTextDataMap, List<String> texts) {
    List<Rectangle> validatedRectangles = new ArrayList<Rectangle>();
    ValidatedTextData largeTextData = validatedTextDataMap.get(ActivityMapColumnModel.LARGE);
    ValidatedTextData mediumTextData = validatedTextDataMap.get(ActivityMapColumnModel.MEDIUM);
    ValidatedTextData smallTextData = validatedTextDataMap.get(ActivityMapColumnModel.SMALL);

    List<String> largeTexts = largeTextData.getTexts();
    for (int i = 0; i < largeTexts.size(); i++) {

      if (!addTextAndRectangleIfFit(largeTextData, i, texts, validatedRectangles)
          && !addTextAndRectangleIfFit(mediumTextData, i, texts, validatedRectangles)) {
        addTextAndRectangle(smallTextData, i, texts, validatedRectangles);
      }
    }

    return validatedRectangles;
  }

  /**
   * Adds a given text and and rectangle to the result lists if the text fits into the rectangle.
   * 
   * @param resultTexts
   *          is an out parameter
   * @param resultRectangles
   *          is an out parameter
   * @return @code{true} if the text fits into the rectangle. Otherwise @code{false}
   */
  boolean addTextAndRectangleIfFit(ValidatedTextData validatedTextData, int index, List<String> resultTexts, List<Rectangle> resultRectangles) {
    if (validatedTextData != null && validatedTextData.getTextsFit().get(index)) {
      addTextAndRectangle(validatedTextData, index, resultTexts, resultRectangles);
      return true;
    }
    return false;
  }

  /**
   * Adds a given text and and rectangle to the result lists.
   * 
   * @param resultTexts
   *          is an out parameter
   * @param resultRectangles
   *          is an out parameter
   */
  void addTextAndRectangle(ValidatedTextData validatedTextData, int index, List<String> resultTexts, List<Rectangle> resultRectangles) {
    resultTexts.add(validatedTextData.getTexts().get(index));
    resultRectangles.add(validatedTextData.getValidatedRectangles().get(index));
  }

  /**
   * Container for holding validated text and rectangle information
   */
  static class ValidatedTextData {

    private final List<String> m_texts;
    private final List<Boolean> m_textsFit;
    private final List<Rectangle> m_validatedRectangles;

    ValidatedTextData(List<String> texts, List<Boolean> textsFit, List<Rectangle> validatedRectangles) {
      m_texts = texts;
      m_textsFit = textsFit;
      m_validatedRectangles = validatedRectangles;
    }

    List<String> getTexts() {
      return m_texts;
    }

    List<Boolean> getTextsFit() {
      return m_textsFit;
    }

    List<Rectangle> getValidatedRectangles() {
      return m_validatedRectangles;
    }

    boolean doAllTextsFitIntoRectangle() {
      for (Boolean textFit : m_textsFit) {
        if (!textFit) {
          return false;
        }
      }
      return true;
    }
  }
}
