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
package org.eclipse.scout.rt.client.ui.form.fields.labelfield;

import org.eclipse.scout.rt.client.ui.IHtmlCapable;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;

public interface ILabelField extends IValueField<String>, IHtmlCapable {
  String PROP_WRAP_TEXT = "wrapText";
  String PROP_SELECTABLE = "selectable";

  void setWrapText(boolean b);

  boolean isWrapText();

  /**
   * Specifies whether the label should be selectable or not
   *
   * @since 3.10.0-M6
   */
  void setSelectable(boolean b);

  /**
   * returns <code>true</code> if the label is selectable, <code>false</code> otherwise
   */
  boolean isSelectable();

}
