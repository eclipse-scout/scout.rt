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

import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.IStringField;
import org.eclipse.scout.rt.ui.rap.ext.custom.StyledText;
import org.eclipse.swt.widgets.Control;

/**
 * <h3>RwtScoutValueFieldComposite</h3> ...
 * 
 * @since 3.7.0 June 2011
 * @param <T>
 */
public abstract class RwtScoutValueFieldComposite<T extends IValueField<?>> extends RwtScoutFieldComposite<T> {

  @Override
  protected void attachScout() {
    super.attachScout();
    setValueFromScout();
    setDisplayTextFromScout(getScoutObject().getDisplayText());
    if (getOnFieldLabelDecorator() != null) {
      setOnFieldLabelFromScout(getScoutObject().getDisplayText(), getScoutObject().getLabel());
    }
  }

  protected void setValueFromScout() {
  }

  protected void setDisplayTextFromScout(String s) {
  }

  public void setOnFieldLabelFromScout(String text, final String label) {
    if (text == null || text.length() == 0) {
      if (getUiField() != null && getUiField() instanceof StyledText) {
        Object length = getScoutObject().getCustomProperty(IStringField.PROP_MAX_LENGTH);
        try {
          if (length != null && label.length() > 0) {
            if (length instanceof Integer && (Integer) length < label.length()) {
              getScoutObject().setCustomProperty(IStringField.PROP_MAX_LENGTH, label.length());
            }
          }
          ((StyledText) getUiField()).setOnFieldLabel(label);
          ((Control) getUiField()).setData(WidgetUtil.CUSTOM_VARIANT, "onFieldLabel");
        }
        finally {
          if (length != null && length instanceof Integer && (Integer) length < label.length()) {
            getScoutObject().setCustomProperty(IStringField.PROP_MAX_LENGTH, length);
          }
        }
      }
    }
    else if (getUiField() instanceof Control) {
      ((Control) getUiField()).setData(WidgetUtil.CUSTOM_VARIANT, null);
    }
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (name.equals(IValueField.PROP_DISPLAY_TEXT)) {
        setDisplayTextFromScout((String) newValue);
    	if (getOnFieldLabelDecorator() != null && StringUtility.hasText((String) newValue) && CompareUtility.notEquals((String) newValue, getScoutObject().getLabel())) {
        setOnFieldLabelFromScout((String) newValue, getScoutObject().getLabel());
      }
    }
    else if (name.equals(IValueField.PROP_VALUE)) {
      setValueFromScout();
    }
  }
}
