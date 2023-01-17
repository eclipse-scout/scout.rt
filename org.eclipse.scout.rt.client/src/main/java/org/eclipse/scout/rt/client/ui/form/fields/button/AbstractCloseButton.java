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

@ClassId("008cee2c-630b-4377-a76f-a1a48a69ec2c")
public abstract class AbstractCloseButton extends AbstractButton {

  public AbstractCloseButton() {
    this(true);
  }

  public AbstractCloseButton(boolean callInitializer) {
    super(callInitializer);
  }

  /*
   * Configuration
   */
  @Override
  protected int getConfiguredSystemType() {
    return SYSTEM_TYPE_CLOSE;
  }

  @Override
  protected String getConfiguredLabel() {
    return TEXTS.get("CloseButton");
  }

  @Override
  protected String getConfiguredKeyStroke() {
    return IKeyStroke.ESCAPE;
  }

  @Override
  protected boolean getConfiguredPreventDoubleClick() {
    return true;
  }
}
