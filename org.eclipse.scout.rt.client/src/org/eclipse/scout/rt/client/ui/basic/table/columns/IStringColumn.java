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

  void setDisplayFormat(String s);

  String getDisplayFormat();

  void setTextWrap(boolean b);

  boolean isTextWrap();

  boolean isSelectAllOnEdit();

  public void setSelectAllOnEdit(boolean b);

  public void setValidateOnAnyKey(boolean b);

  public boolean isValidateOnAnyKey();

  public void setMaxLength(int len);

  public int getMaxLength();

}
