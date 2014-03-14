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

import org.eclipse.scout.commons.annotations.ClassId;

@ClassId("1221cfac-4636-4d53-8485-07872f956fc1")
public abstract class AbstractRadioButton extends AbstractButton implements IButton {

  public AbstractRadioButton() {
    this(true);
  }

  public AbstractRadioButton(boolean callInitializer) {
    super(callInitializer);
  }

  /*
   * Configuration
   */
  @Override
  protected int getConfiguredDisplayStyle() {
    return DISPLAY_STYLE_RADIO;
  }
}
