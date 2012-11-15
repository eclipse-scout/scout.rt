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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * This class overrides the text shortening behaviour of the CLabel
 * class.Instead of replacing the center of the text with an ellipsis, this
 * class adds an ellipsis at the end of the text (or before the last word of the
 * text, if the label is part of a field range composition).
 */
@SuppressWarnings("restriction")
public class CLabelEx extends CLabel {
  private static final long serialVersionUID = 1L;

  private static final String ELLIPSIS = "...";

  private String m_originalText = "";
  private String m_originalTooltip;
  private Image m_originalImage;

  public CLabelEx(Composite parent, int style) {
    super(parent, style | SWT.NO_FOCUS);

    addListener(SWT.Resize, new P_ResizeListener());
  }

  @Override
  public void setBounds(Rectangle bounds) {
    super.setBounds(bounds);
    if (bounds.width > 0) {
      updateText(bounds.width);
    }
  }

  @Override
  public void setText(String text) {
    super.setText(text);
    m_originalText = text;
  }

  @Override
  public void setToolTipText(String tooltip) {
    super.setToolTipText(tooltip);
    m_originalTooltip = tooltip;
  }

  @Override
  public void setImage(Image image) {
    super.setImage(image);
    m_originalImage = image;
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
      super.setImage(null);
      if (extent.x > availableWidth) {
        super.setText(shortenText(null, m_originalText, availableWidth));
        super.setToolTipText(m_originalText);
      }
      else {
        super.setText(m_originalText);
        super.setToolTipText(m_originalTooltip);
      }
    }
    else {
      super.setText(m_originalText);
      super.setToolTipText(m_originalTooltip);
      super.setImage(m_originalImage);
    }
  }

  /**
   * Shorten the given text <code>text</code> so that its length doesn't exceed
   * the given width. The default implementation replaces characters in the
   * center of the original string with an ellipsis ("...").
   * Override if you need a different strategy.
   * 
   * @param gc
   *          the gc to use for text measurement
   * @param text
   *          the text to shorten
   * @param width
   *          the width to shorten the text to, in pixels
   * @return the shortened text
   */
  public String shortenText(Object gc, String text, int width) {
    if (text == null || width <= 0) {
      return null;
    }

    int ecllipsisWidth = TextSizeUtil.stringExtent(getFont(), ELLIPSIS).x;
    int charCount = text.length();

    // shorten string
    while (charCount >= 0) {
      String shortenedText = text.substring(0, charCount);
      int textWidth = TextSizeUtil.stringExtent(getFont(), shortenedText).x;
      if (textWidth + ecllipsisWidth < width) {
        return shortenedText + ELLIPSIS;
      }
      if (charCount == 0) {
        return "";
      }
      charCount--;
    }

    return null;
  }

  private class P_ResizeListener implements Listener {

    private static final long serialVersionUID = 1L;

    @Override
    public void handleEvent(Event event) {
      if (event.type == SWT.Resize) {
        updateText(getBounds().width);
      }
    }
  }
}
