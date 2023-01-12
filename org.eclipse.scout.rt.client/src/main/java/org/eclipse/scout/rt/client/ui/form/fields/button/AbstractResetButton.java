/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.button;

import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.text.TEXTS;

@ClassId("059a286e-5445-459e-8b5e-77bd2b019064")
public abstract class AbstractResetButton extends AbstractButton {

  public AbstractResetButton() {
    this(true);
  }

  public AbstractResetButton(boolean callInitializer) {
    super(callInitializer);
  }

  /*
   * Configuration
   */
  @Override
  protected int getConfiguredSystemType() {
    return SYSTEM_TYPE_RESET;
  }

  @Override
  protected String getConfiguredLabel() {
    return TEXTS.get("ResetButton");
  }

  @Override
  protected String getConfiguredKeyStroke() {
    return IKeyStroke.CONTROL + "-" + IKeyStroke.F6;
  }

  @Override
  protected boolean getConfiguredPreventDoubleClick() {
    return true;
  }
}
