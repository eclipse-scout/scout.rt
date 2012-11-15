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

import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.rt.shared.ScoutTexts;

public abstract class AbstractResetButton extends AbstractButton implements IButton {

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
  @ConfigPropertyValue("SYSTEM_TYPE_RESET")
  protected int getConfiguredSystemType() {
    return SYSTEM_TYPE_RESET;
  }

  @Override
  @ConfigPropertyValue("\"ResetButton\"")
  protected String getConfiguredLabel() {
    return ScoutTexts.get("ResetButton");
  }

  @Override
  @ConfigPropertyValue("\"ResetButtonTooltip\"")
  protected String getConfiguredTooltipText() {
    return ScoutTexts.get("ResetButtonTooltip");
  }

  @Override
  protected int getConfiguredHorizontalAlignment() {
    return 1;
  }
}
