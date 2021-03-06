/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui.form.fields.button;

import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.text.TEXTS;

@ClassId("d5b9a643-bdf2-483c-8adf-e8728021611e")
public abstract class AbstractCancelButton extends AbstractButton {

  public AbstractCancelButton() {
    this(true);
  }

  public AbstractCancelButton(boolean callInitializer) {
    super(callInitializer);
  }

  /*
   * Configuration
   */
  @Override
  protected int getConfiguredSystemType() {
    return SYSTEM_TYPE_CANCEL;
  }

  @Override
  protected String getConfiguredLabel() {
    return TEXTS.get("CancelButton");
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
