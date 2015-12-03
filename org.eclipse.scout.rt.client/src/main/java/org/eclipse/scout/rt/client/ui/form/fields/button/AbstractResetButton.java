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
import org.eclipse.scout.rt.shared.ScoutTexts;

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
    return ScoutTexts.get("ResetButton");
  }

  @Override
  protected String getConfiguredKeyStroke() {
    return IKeyStroke.CONTROL + "-" + IKeyStroke.F6;
  }

}
