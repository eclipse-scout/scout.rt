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

/**
 * @deprecated will be removed with 7.1, property is no longer supported by Html UI
 */
@ClassId("292b7886-de8f-42ee-ab52-cd1b4bf3647e")
@Deprecated
public abstract class AbstractNonFocusableRadioButton<T> extends AbstractRadioButton<T> {

  public AbstractNonFocusableRadioButton() {
    this(true);
  }

  public AbstractNonFocusableRadioButton(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  @SuppressWarnings("deprecation")
  protected boolean getConfiguredFocusable() {
    return false;
  }
}
