/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.colorfield;

import org.eclipse.scout.rt.client.ui.form.fields.IBasicField;

public interface IColorField extends IBasicField<String> {
  String PROP_ICON_ID = "iconId";

  /**
   * @return
   */
  String getIconId();

  /**
   * @param s
   */
  void setIconId(String s);
}
