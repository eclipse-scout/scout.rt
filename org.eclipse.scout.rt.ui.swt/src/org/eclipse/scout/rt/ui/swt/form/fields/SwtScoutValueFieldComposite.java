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
package org.eclipse.scout.rt.ui.swt.form.fields;

import org.eclipse.scout.rt.client.ui.form.fields.IValueField;

/**
 * <h3>SwtScoutValueFieldComposite</h3> ...
 * 
 * @since 1.0.0 01.04.2008
 * @param <T>
 */
public abstract class SwtScoutValueFieldComposite<T extends IValueField<?>> extends SwtScoutFieldComposite<T> {

  @Override
  protected void attachScout() {

    super.attachScout();
    setDisplayTextFromScout(getScoutObject().getDisplayText());
  }

  protected void setDisplayTextFromScout(String s) {
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (name.equals(IValueField.PROP_DISPLAY_TEXT)) {
      setDisplayTextFromScout((String) newValue);
    }
  }

}
