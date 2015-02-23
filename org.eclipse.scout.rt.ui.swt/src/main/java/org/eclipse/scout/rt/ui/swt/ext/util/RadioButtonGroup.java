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
package org.eclipse.scout.rt.ui.swt.ext.util;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * <h3>RadioButtonGroup</h3> ...
 *
 * @since 1.0.0 14.04.2008
 */
public class RadioButtonGroup {
  private ArrayList<Button> m_radioButtons = new ArrayList<Button>();
  private Listener m_radioBehaviourListener = new P_RadioBehaviourListener();

  public void addButton(Button button) {
    if (button != null) {
      synchronized (m_radioButtons) {
        m_radioButtons.add(button);
        button.addListener(SWT.Selection, m_radioBehaviourListener);
        button.addListener(SWT.Dispose, m_radioBehaviourListener);
      }
    }
  }

  public void removeButton(Button button) {
    if (button != null) {
      synchronized (m_radioButtons) {
        button.removeListener(SWT.Selection, m_radioBehaviourListener);
        button.removeListener(SWT.Dispose, m_radioBehaviourListener);
        m_radioButtons.remove(button);
      }
    }
  }

  protected void handleRadioButtonSelected(Button selectedButton) {
    Button[] buttons;
    synchronized (m_radioButtons) {
      buttons = m_radioButtons.toArray(new Button[m_radioButtons.size()]);
    }
    for (Button b : buttons) {
      b.setSelection(b.equals(selectedButton));
    }
  }

  private class P_RadioBehaviourListener implements Listener {
    @Override
    public void handleEvent(Event event) {
      switch (event.type) {
        case SWT.Dispose:
          removeButton((Button) event.widget);
          break;
        case SWT.Selection:
          handleRadioButtonSelected((Button) event.widget);
          break;
        default:
          break;
      }
    }
  }
}
