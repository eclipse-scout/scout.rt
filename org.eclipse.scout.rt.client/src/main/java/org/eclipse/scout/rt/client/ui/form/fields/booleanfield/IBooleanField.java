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
package org.eclipse.scout.rt.client.ui.form.fields.booleanfield;

import org.eclipse.scout.rt.client.ui.form.fields.IValueField;

public interface IBooleanField extends IValueField<Boolean> {
  String PROP_TRISTATE_ENABLED = "tristateEnabled";

  /**
   * Configuration
   */
  void setChecked(boolean b);

  boolean isChecked();

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

  IBooleanFieldUIFacade getUIFacade();
}
