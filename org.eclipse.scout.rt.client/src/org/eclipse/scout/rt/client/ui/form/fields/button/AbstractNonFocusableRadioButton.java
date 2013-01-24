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

public abstract class AbstractNonFocusableRadioButton extends AbstractButton implements IButton {

  public AbstractNonFocusableRadioButton() {
    this(true);
  }

  public AbstractNonFocusableRadioButton(boolean callInitializer) {
    super(callInitializer);
  }

  /*
   * Configuration
   */
  @ConfigPropertyValue("false")
  @Override
  protected boolean getConfiguredFocusable() {
    return false;
  }

  @Override
  @ConfigPropertyValue("DISPLAY_STYLE_RADIO")
  protected int getConfiguredDisplayStyle() {
    return DISPLAY_STYLE_RADIO;
  }
}
