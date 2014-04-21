/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.form.fields;

import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.ui.rap.basic.RwtScoutComposite;

/**
 * @since 3.8.0
 */
public abstract class RwtScoutValueFieldComposite<T extends IValueField<?>> extends RwtScoutFieldComposite<T> {

  @Override
  protected void attachScout() {
    super.attachScout();
    setValueFromScout();
    setDisplayTextFromScout(getScoutObject().getDisplayText());
  }

  protected void setValueFromScout() {
  }

  protected void setDisplayTextFromScout(String s) {
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (IValueField.PROP_DISPLAY_TEXT.equals(name)) {
      String displayText = (String) newValue;
      setDisplayTextFromScout(displayText);
    }
    else if (IValueField.PROP_VALUE.equals(name)) {
      setValueFromScout();
    }
  }

  /**
   * Forces UI Input to be verified.
   * 
   * @deprecated Use {@link RwtScoutComposite#runUiInputVerifier()} instead. Will be removed in the 5.0 Release.
   */
  @Deprecated
  public void verifyUiInput() {
    handleUiInputVerifier(true);
  }
}
