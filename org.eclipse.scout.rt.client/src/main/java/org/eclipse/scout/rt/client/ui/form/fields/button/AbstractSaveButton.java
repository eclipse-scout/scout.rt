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
package org.eclipse.scout.rt.client.ui.form.fields.button;

import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.shared.TEXTS;

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
}
