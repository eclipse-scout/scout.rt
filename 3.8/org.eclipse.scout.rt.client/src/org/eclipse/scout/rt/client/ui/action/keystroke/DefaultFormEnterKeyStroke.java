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
package org.eclipse.scout.rt.client.ui.action.keystroke;

import java.util.TreeMap;

import org.eclipse.scout.commons.CompositeLong;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;

/**
 * <h3>DefaultFormEnterKeyStroke</h3> ...
 * 
 * @since 3.1.12 23.07.2008
 */
public class DefaultFormEnterKeyStroke extends AbstractKeyStroke {

  private final IForm m_form;

  public DefaultFormEnterKeyStroke(IForm form) {
    m_form = form;
  }

  @Override
  protected String getConfiguredKeyStroke() {
    return "enter";
  }

  @Override
  protected void execAction() throws ProcessingException {
    IForm f = m_form;
    while (f != null) {
      TreeMap<CompositeLong, IButton> prioMap = new TreeMap<CompositeLong, IButton>();
      for (IButton b : f.getRootGroupBox().getSystemProcessButtons()) {
        switch (b.getSystemType()) {
          case IButton.SYSTEM_TYPE_OK:
            prioMap.put(new CompositeLong(3, prioMap.size()), b);
            break;
          case IButton.SYSTEM_TYPE_SAVE:
            prioMap.put(new CompositeLong(4, prioMap.size()), b);
            break;
          case IButton.SYSTEM_TYPE_SAVE_WITHOUT_MARKER_CHANGE:
            prioMap.put(new CompositeLong(5, prioMap.size()), b);
            break;
        }
      }
      int visibleCount = 0;
      for (IButton b : prioMap.values()) {
        if (b.isEnabled() && b.isVisible() && b.isEnabledProcessingButton()) {
          b.doClick();
          return;
        }
        else if (b.isVisible()) {
          visibleCount++;
        }
      }
      // here, no action was taken
      // if there was no visible button at all, ONLY THEN try parent form
      if (visibleCount == 0) {
        f = f.getOuterForm();
      }
      else {
        f = null;
      }
    }
  }

}
