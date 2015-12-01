/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
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
import org.eclipse.scout.rt.shared.ScoutTexts;

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
    return ScoutTexts.get("CloseButton");
  }

  @Override
  protected String getConfiguredKeyStroke() {
    return IKeyStroke.ESCAPE;
  }
}
