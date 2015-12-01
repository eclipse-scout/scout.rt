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
    return ScoutTexts.get("SearchButton");
  }

  @Override
  protected String getConfiguredKeyStroke() {
    return IKeyStroke.ENTER;
  }
}
