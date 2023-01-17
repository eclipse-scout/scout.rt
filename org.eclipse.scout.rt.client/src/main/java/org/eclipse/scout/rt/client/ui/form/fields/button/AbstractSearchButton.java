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

@ClassId("5ca31667-6bdf-4e06-8d51-5e283aa16c52")
public abstract class AbstractSearchButton extends AbstractButton {

  public AbstractSearchButton() {
    this(true);
  }

  public AbstractSearchButton(boolean callInitializer) {
    super(callInitializer);
  }

  /*
   * Configuration
   */
  @Override
  protected int getConfiguredSystemType() {
    return SYSTEM_TYPE_SAVE_WITHOUT_MARKER_CHANGE;
  }

  @Override
  protected String getConfiguredLabel() {
    return TEXTS.get("SearchButton");
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
