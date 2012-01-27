/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.basic.comp;

import org.eclipse.rwt.internal.textsize.TextSizeUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

/**
 * This class overrides the text shortening behaviour of the CLabel
 * class.Instead of replacing the center of the text with an ellipsis, this
 * class adds an ellipsis at the end of the text (or before the last word of the
 * text, if the label is part of a field range composition).
 */
@SuppressWarnings("restriction")
public class CLabelEx extends CLabel {
  private static final long serialVersionUID = 1L;

  private static final int DRAW_FLAGS = /*XXX rap SWT.DRAW_MNEMONIC | */SWT.DRAW_TAB | SWT.DRAW_TRANSPARENT | SWT.DRAW_DELIMITER;
  /** a string inserted in the middle of text that has been shortened */
  private static final String ELLIPSIS = "...";
  private boolean m_isFieldRangeLabel = false;

  private String m_originalText = "";

  public CLabelEx(Composite parent, int style) {
    super(parent, style | SWT.NO_FOCUS);
  }

  public boolean isFieldRangeLabel() {
    return m_isFieldRangeLabel;
  }

  public void setFieldRangeLabel(boolean isFieldRangeLabel) {
    this.m_isFieldRangeLabel = isFieldRangeLabel;
  }

  @Override
  public void setText(String text) {
    super.setText(text);
    m_originalText = text;
  }

  @Override
  public void setBounds(Rectangle bounds) {
    super.setBounds(bounds);
    if (bounds.width > 0) {
      updateText(bounds.width);
    }
  }

  @Override
  public void setSize(Point size) {
    super.setSize(size);
    if (size.x > 0) {
      updateText(size.x);
    }
  }

  private void updateText(int labelWidth) {
    Image image = getImage();

    int availableWidth = Math.max(0, labelWidth - (getLeftMargin() + getRightMargin()));
    Point extent = TextSizeUtil.stringExtent(getFont(), m_originalText);
    int imageWidth = 0;
    if (image != null) {
      Rectangle r = image.getBounds();
      imageWidth = r.width;
    }
    if (extent.x > availableWidth - imageWidth) {
      setImage(null);
      if (extent.x > availableWidth) {
        super.setText(shortenText(null, m_originalText, availableWidth));
        setToolTipText(m_originalText);
      }
      else {
        setToolTipText("");
      }
    }
  }

  /**
   * Shorten the given text <code>t</code> so that its length doesn't exceed
   * the given width. The default implementation replaces characters in the
   * center of the original string with an ellipsis ("...").
   * Override if you need a different strategy.
   * 
   * @param gc
   *          the gc to use for text measurement
   * @param t
   *          the text to shorten
   * @param width
   *          the width to shorten the text to, in pixels
   * @return the shortened text
   */
  public String shortenText(Object gc, String t, int width) {
    if (t == null) {
      return null;
    }
    String text = t;
    String rangeWord = "";
    if (m_isFieldRangeLabel) {
      text = t.substring(0, t.lastIndexOf(' '));
      rangeWord = t.substring(t.lastIndexOf(' '));
    }
    int textWidth = TextSizeUtil.stringExtent(getFont(), ELLIPSIS).x;
    int rangeWidth = TextSizeUtil.stringExtent(getFont(), rangeWord).x;
    // initial number of characters
    int s = text.length();
    // shorten string
    while (s >= 0) {
      String s1 = t.substring(0, s);
      int l1 = TextSizeUtil.stringExtent(getFont(), s1).x;
      if (l1 + textWidth + rangeWidth < width) {
        t = s1 + ELLIPSIS + rangeWord;
        break;
      }
      s--;
    }
    return t;
  }
}
