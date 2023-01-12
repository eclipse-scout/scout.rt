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

@ClassId("74551d0d-a923-49e9-aaeb-a212d77e460d")
public abstract class AbstractSaveButton extends AbstractButton {

  public AbstractSaveButton() {
    this(true);
  }

  public AbstractSaveButton(boolean callInitializer) {
    super(callInitializer);
  }

  /*
   * Configuration
   */
  @Override
  protected int getConfiguredSystemType() {
    return SYSTEM_TYPE_SAVE;
  }

  @Override
  protected String getConfiguredLabel() {
    return TEXTS.get("SaveButton");
  }

  @Override
  protected String getConfiguredKeyStroke() {
    return IKeyStroke.ENTER;
  }

  @Override
  protected boolean getConfiguredPreventDoubleClick() {
    return true;
  }
}
