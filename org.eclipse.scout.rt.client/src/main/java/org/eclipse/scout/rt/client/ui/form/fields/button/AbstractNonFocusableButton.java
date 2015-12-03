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

import org.eclipse.scout.rt.platform.classid.ClassId;

@ClassId("36dc920b-26df-4d40-bf38-0c83361fd4c8")
public abstract class AbstractNonFocusableButton extends AbstractButton {

  public AbstractNonFocusableButton() {
    this(true);
  }

  public AbstractNonFocusableButton(boolean callInitializer) {
    super(callInitializer);
  }

  /*
   * Configuration
   */
  @Override
  protected boolean getConfiguredFocusable() {
    return false;
  }
}
