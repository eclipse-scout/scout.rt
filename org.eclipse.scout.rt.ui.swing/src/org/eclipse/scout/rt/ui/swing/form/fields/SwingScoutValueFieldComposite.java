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
package org.eclipse.scout.rt.ui.swing.form.fields;

import java.awt.Color;

import javax.swing.JComponent;
import javax.swing.text.JTextComponent;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.ValueFieldEvent;

public abstract class SwingScoutValueFieldComposite<T extends IValueField<?>> extends SwingScoutFieldComposite<T> {

  private boolean m_menuOpened;

  @Override
  protected void attachScout() {
    super.attachScout();
    IValueField f = getScoutObject();
    setValueFromScout(f.getValue());
    setDisplayTextFromScout(f.getDisplayText());
    updateContextMenuFromScout();
  }

  protected void valueFieldChangedFromScout(ValueFieldEvent<?> event) {
    Runnable r = null;
    switch (event.getType()) {
      case ValueFieldEvent.TYPE_COPY: {
        r = new Runnable() {
          @Override
          public void run() {
            handleCopy();
          }
        };
        break;
      }
      case ValueFieldEvent.TYPE_PASTE: {
        r = new Runnable() {
          @Override
          public void run() {
            handlePaste();
          }
        };
        break;
      }
      case ValueFieldEvent.TYPE_CUT: {
        r = new Runnable() {
          @Override
          public void run() {
            handleCut();
          }
        };
        break;
      }
    }

    if (r != null) {
      getSwingEnvironment().invokeSwingLater(r);
    }
  }

  /**
   * only used in checkbox
   */
  protected void setValueFromScout(Object o) {
  }

  protected void setDisplayTextFromScout(String s) {
  }

  protected void updateContextMenuFromScout() {
  }

  /**
   * copy event handle. executed in swing thread
   */
  protected void handleCopy() {
    JComponent swingField = getSwingField();
    if (swingField instanceof JTextComponent) {
      JTextComponent txt = (JTextComponent) swingField;
      if (txt.isEnabled() && txt.isEditable()) {
        txt.copy();
      }
      else {
        boolean hasSelection = StringUtility.hasText(txt.getSelectedText());
        if (hasSelection) {
          txt.copy();
        }
        else {
          txt.selectAll();
          txt.copy();
          txt.select(0, 0);
        }
      }
    }
  }

  /**
   * cut event handle. executed in swing thread
   */
  protected void handleCut() {
    JComponent swingField = getSwingField();
    if (swingField instanceof JTextComponent) {
      ((JTextComponent) swingField).cut();
    }
  }

  /**
   * paste event handle. executed in swing thread
   */
  protected void handlePaste() {
    JComponent swingField = getSwingField();
    if (swingField instanceof JTextComponent) {
      ((JTextComponent) swingField).paste();
    }
  }

  protected void setDisabledTextColor(Color c, JTextComponent fld) {
    if (fld != null) {
      if (c == null) {
        c = fld.getDisabledTextColor();
      }
      else {
        c = getDisabledColor(c);
      }
      fld.setDisabledTextColor(c);
    }
  }

  protected Color getDisabledColor(Color origColor) {
    /**
     * some users wish that also the disabled fg color is the same as the fg
     * color, others wished the contrary. As a consequence, the disabled color
     * is now a lightened up version of the fg color
     */
    Color w = Color.white;
    Color cLight = new Color(
        (origColor.getRed() * 2 + w.getRed()) / 3,
        (origColor.getGreen() * 2 + w.getGreen()) / 3,
        (origColor.getBlue() * 2 + w.getBlue()) / 3
        );
    return cLight;
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (name.equals(IValueField.PROP_VALUE)) {
      setValueFromScout(newValue);
    }
    else if (name.equals(IValueField.PROP_DISPLAY_TEXT)) {
      setDisplayTextFromScout((String) newValue);
    }
    else if (name.equals(IValueField.PROP_CONTEXT_MENU)) {
      updateContextMenuFromScout();
    }
  }

  protected boolean isMenuOpened() {
    return m_menuOpened;
  }

  protected void setMenuOpened(boolean menuOpened) {
    m_menuOpened = menuOpened;
  }

}
