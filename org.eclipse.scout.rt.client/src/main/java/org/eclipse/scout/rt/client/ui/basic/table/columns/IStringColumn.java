/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.basic.table.columns;

public interface IStringColumn extends IColumn<String> {
  /* enum for formats */

  /**
   * Display format for upper case letters.
   */
  String FORMAT_UPPER = "A";

  /**
   * Display format for lower case letters.
   */
  String FORMAT_LOWER = "a";

  void setInputMasked(boolean b);

  boolean isInputMasked();

  /**
   * Sets the display format of this column.
   *
   * @param s
   *          Either {@code null}, {@link #FORMAT_LOWER} or {@link #FORMAT_UPPER}.
   */
  void setDisplayFormat(String s);

  String getDisplayFormat();

  void setTextWrap(boolean b);

  boolean isTextWrap();

  void setMaxLength(int len);

  int getMaxLength();

}
