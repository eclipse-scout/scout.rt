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
  String TRISTATE_TEXT = "?";

  String PROP_VERTICAL_ALIGNMENT = "verticalAlignment";

  String PROP_TRISTATE_ENABLED = "tristateEnabled";

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
   * see {@link #isTristateEnabled()}
   *
   * @since 6.1
   * @param b
   */
  void setTristateEnabled(boolean b);

  /**
   * true: the checkbox can have a {@link #getValue()} of true, false and also null. null is the tristate and is
   * typically displayed using a filled rectangluar area.
   * <p>
   * false: the checkbox can have a {@link #getValue()} of true, false. The value is never null.
   * <p>
   * default is false
   *
   * @since 6.1
   * @return true if this checkbox supports the so-called tristate and can be {@link #setValue(Boolean)} to null in
   *         order to represent the tristate value
   */
  boolean isTristateEnabled();
}
