/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swing.text;

import java.awt.Color;

/**
 * Create a styledText from the given attributes
 * 
 * @since 3.10.0-M5
 */
public interface IStyledTextCreator {
  /**
   * The text to be styled
   */
  public void setText(String text);

  /**
   * The backgroundColor to be used. If empty no backgroundColor will be specified
   */
  public void setBackgroundColor(Color color);

  /**
   * The horizontal alignment of the text
   */
  public void setHorizontalAlignment(int scoutAlign);

  /**
   * The vertical alignment of the text
   */
  public void setVerticalAlignment(int scoutAlign);

  /**
   * The height of the container. This may be needed for vertical alignment
   */
  public void setHeight(int height);

  /**
   * Specifies whether text should be wrapped or not
   */
  public void setTextWrap(boolean wrap);

  /**
   * Returns the styled text using the specified attributes
   */
  public String createStyledText();
}
