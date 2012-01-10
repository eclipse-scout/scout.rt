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
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.ui.rap.ext.custom.StyledText;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

public class OnFieldLabelDecorator implements FocusListener {
  private static final long serialVersionUID = 1L;

  private String m_label;
  private String m_text;
  private boolean m_mandatory;
  private Control m_control;
  private RwtScoutFieldComposite m_rwtScoutFieldComposite;

  public OnFieldLabelDecorator(Control control, boolean mandatory, RwtScoutFieldComposite rwtScoutFieldComposite) {
    m_mandatory = mandatory;
    m_control = control;
    m_rwtScoutFieldComposite = rwtScoutFieldComposite;
  }

  public void setLabel(String s) {
    m_label = s;

    paintOnFieldLabel(false);
  }

  private Control getControl() {
    return m_control;
  }

  private void paintOnFieldLabel(boolean hasFocus) {
    if (getControl() instanceof StyledText && hasFocus) {
      if (getLabel().equalsIgnoreCase(((StyledText) getControl()).getText())) {
        ((StyledText) getControl()).setOnFieldLabel("");
        if (m_rwtScoutFieldComposite instanceof RwtScoutValueFieldComposite) {
          ((RwtScoutValueFieldComposite) m_rwtScoutFieldComposite).setOnFieldLabelFromScout("", "");
        }
      }
      getControl().setData(WidgetUtil.CUSTOM_VARIANT, null);
      return;
    }
    if (getControl() instanceof Text && StringUtility.length(((Text) getControl()).getText()) > 0) {
      return;
    }
    if (getControl() instanceof StyledText && StringUtility.length(((StyledText) getControl()).getText()) > 0) {
      return;
    }
    if (getControl() instanceof StyledText) {
      if (m_rwtScoutFieldComposite instanceof RwtScoutValueFieldComposite) {
        ((RwtScoutValueFieldComposite) m_rwtScoutFieldComposite).setOnFieldLabelFromScout("", getLabel());
        getControl().setData(WidgetUtil.CUSTOM_VARIANT, "onFieldLabel");
      }
    }
  }

  public String getLabel() {
    return m_label;
  }

  public void attach(Control c) {
    if (c != null && !c.isDisposed()) {
      m_control = c;
      c.addFocusListener(this);
    }
  }

  public void detach(Control c) {
    if (c != null && !c.isDisposed()) {
      c.removeFocusListener(this);
    }
  }

  @Override
  public void focusGained(FocusEvent e) {
    if (e.widget instanceof Control) {
      paintOnFieldLabel(true);
      ((Control) e.widget).redraw();
    }
  }

  @Override
  public void focusLost(FocusEvent e) {
    if (e.widget instanceof Control) {
      paintOnFieldLabel(false);
      ((Control) e.widget).redraw();
    }
  }

}
