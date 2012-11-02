/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     BSI Business Systems Integration AG - customization in this extension class
 ******************************************************************************/
package org.eclipse.scout.rt.ui.rap.basic.comp;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.Hyperlink;

/**
 * This class overrides the text shortening behaviour of the Hyperlink class.
 * Instead of replacing the center of the text with an ellipsis, this class adds
 * an ellipsis at the end of the text (or before the last word of the text, if
 * the label is part of a field range composition).
 */
public class HyperlinkEx extends Hyperlink {
  private static final long serialVersionUID = 1L;

  private static final int PAINT_FLAGS = /*XXX rap SWT.DRAW_MNEMONIC | */SWT.DRAW_TAB | SWT.DRAW_TRANSPARENT | SWT.DRAW_DELIMITER;
  private static final String ELLIPSIS_TEXT = "...";
  /** the alignment. Either CENTER, RIGHT, LEFT. Default is LEFT */
  private int align = SWT.LEFT;
  /** Left and right margins */
  private static final int INDENT = 3;

  public HyperlinkEx(Composite parent, int style) {
    super(parent, style);
    if ((style & (SWT.CENTER | SWT.RIGHT)) == 0) {
      style |= SWT.LEFT;
    }
    if ((style & SWT.CENTER) != 0) {
      align = SWT.CENTER;
    }
    if ((style & SWT.RIGHT) != 0) {
      align = SWT.RIGHT;
    }
    if ((style & SWT.LEFT) != 0) {
      align = SWT.LEFT;
    }
  }

  /*XXX rap
  @SuppressWarnings("restriction")
  @Override
  protected void paintText(GC gc, Rectangle r) {
    Rectangle clientRect = getClientArea();
    gc.setFont(getFont());
    gc.setForeground(getForeground());
    if ((getStyle() & SWT.WRAP) != 0) {
      try {
        org.eclipse.ui.internal.forms.widgets.FormUtil.paintWrapText(gc, getText(), r, isUnderlined());
      }
      catch (Throwable t) {
        //nop
      }
    }
    else {
      Point textSize = computeTextSize(SWT.DEFAULT, SWT.DEFAULT);
      boolean mustShortenText = false;
      if (r.width < textSize.x) {
        mustShortenText = true;
      }
      int textW = Math.min(r.width, textSize.x);
      int textH = textSize.y;
      String text = getText();
      if (mustShortenText) {
        text = shortenText(gc, getText(), r.width);
        if (getToolTipText() == null) {
          super.setToolTipText(getText());
        }
      }
      else {
        super.setToolTipText(getToolTipText());
      }
      int posX = clientRect.x + INDENT;
      if (align == SWT.CENTER) {
        posX = (clientRect.width - textW) / 2;
      }
      if (align == SWT.RIGHT) {
        posX = clientRect.width - INDENT - textW;
      }
      int extX = posX;
      if (align == SWT.CENTER) {
        int ext = gc.textExtent(text, PAINT_FLAGS).x;
        extX = posX + Math.max(0, (r.x - ext) / 2);
      }
      if (align == SWT.RIGHT) {
        int ext = gc.textExtent(text, PAINT_FLAGS).x;
        extX = Math.max(posX, clientRect.x + clientRect.width - INDENT - ext);
      }
      gc.drawText(text, extX, r.y, true);
      if (isUnderlined()) {
        int fontDescent = gc.getFontMetrics().getDescent();
        int extY = r.y + textH - fontDescent + 1;
        gc.drawLine(extX, extY, extX + textW, extY);
      }
    }
  }
  */
  /*XXX rap
  @Override
  protected String shortenText(GC gc, String text, int linkWidth) {
    if (text == null) {
      return null;
    }
    String s = text;
    String suffix = "";
    int textWidth = gc.textExtent(ELLIPSIS_TEXT, PAINT_FLAGS).x;
    int suffixWidth = gc.textExtent(suffix, PAINT_FLAGS).x;
    int sLen = s.length();
    while (sLen >= 0) {
      String sSub = text.substring(0, sLen);
      int pos = gc.textExtent(sSub, PAINT_FLAGS).x;
      if (pos + textWidth + suffixWidth < linkWidth) {
        text = sSub + ELLIPSIS_TEXT + suffix;
        break;
      }
      sLen--;
    }
    return text;
  }
  */

  @Override
  public int getStyle() {
    int style = super.getStyle();
    switch (align) {
      case SWT.RIGHT:
        style |= SWT.RIGHT;
        break;
      case SWT.CENTER:
        style |= SWT.CENTER;
        break;
      case SWT.LEFT:
        style |= SWT.LEFT;
        break;
    }
    return style;
  }

  /**
   * Returns the alignment. The alignment style (LEFT, CENTER or RIGHT) is
   * returned.
   * 
   * @return SWT.LEFT, SWT.RIGHT or SWT.CENTER
   */
  public int getAlignment() {
    // checkWidget();
    return align;
  }
}
