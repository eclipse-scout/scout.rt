/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
   *     Either {@code null}, {@link #FORMAT_LOWER} or {@link #FORMAT_UPPER}.
   */
  void setDisplayFormat(String s);

  String getDisplayFormat();

  void setMaxLength(int len);

  int getMaxLength();
}
