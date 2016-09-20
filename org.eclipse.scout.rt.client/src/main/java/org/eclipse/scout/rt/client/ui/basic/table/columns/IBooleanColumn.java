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

public interface IBooleanColumn extends IColumn<Boolean> {

  String TRUE_TEXT = "X";
  String UNDEFINED_TEXT = "?";
  String PROP_VERTICAL_ALIGNMENT = "verticalAlignment";
  String PROP_TRI_STATE_ENABLED = "triStateEnabled";

  void setVerticalAlignment(int verticalAlignment);

  /**
   * <ul>
   * <li>-1: top alignment</li>
   * <li>0: middle alignment</li>
   * <li>1: bottom alignment</li>
   * </ul>
   * The vertical alignment of the checkbox
   */
  int getVerticalAlignment();

  /**
   * see {@link #isTriStateEnabled()}
   *
   * @since 6.1
   */
  void setTriStateEnabled(boolean triStateEnabled);

  /**
   * <ul>
   * <li><b>true:</b> the check box can have a {@link #getValue()} of <code>true</code>, <code>false</code> and
   * <code>null</code>. <code>null</code> is the third state that represents "undefined" and is typically displayed
   * using a filled rectangular area.
   * <li><b>false:</b> the check box can have a {@link #getValue()} of <code>true</code> and <code>false</code>. The
   * value is never <code>null</code> (setting the value to <code>null</code> will automatically convert it to
   * <code>false</code>).
   * </ul>
   * The default is <code>false</code>.
   *
   * @since 6.1
   * @return <code>true</code> if this check box supports the so-called "tri-state" and allows setting the value to
   *         <code>null</code> to represent the "undefined" value.
   */
  boolean isTriStateEnabled();
}
