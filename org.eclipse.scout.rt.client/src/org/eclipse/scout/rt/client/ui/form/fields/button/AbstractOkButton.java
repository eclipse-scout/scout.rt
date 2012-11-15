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

public abstract class AbstractOkButton extends AbstractButton implements IButton {

  public AbstractOkButton() {
    this(true);
  }

  public AbstractOkButton(boolean callInitializer) {
    super(callInitializer);
  }

  /*
   * Configuration
   */
  @Override
  @ConfigPropertyValue("SYSTEM_TYPE_OK")
  protected int getConfiguredSystemType() {
    return SYSTEM_TYPE_OK;
  }

  @Override
  @ConfigPropertyValue("\"OkButton\"")
  protected String getConfiguredLabel() {
    return ScoutTexts.get("OkButton");
  }

  @Override
  @ConfigPropertyValue("\"OkButtonTooltip\"")
  protected String getConfiguredTooltipText() {
    return ScoutTexts.get("OkButtonTooltip");
  }

  @Override
  protected int getConfiguredHorizontalAlignment() {
    return 1;
  }
}
