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
package org.eclipse.scout.rt.ui.swt.ext;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;

/**
 * A RadioButton whose label supports multiline. See {@link MultilineButton} for more information
 * 
 * @since 3.10.0-M4
 */
public class MultilineRadioButton extends MultilineButton {
  private static final long serialVersionUID = 1L;

  public MultilineRadioButton(Composite parent, int style) {
    super(parent, style | SWT.RADIO);

    m_label.addMouseListener(new P_LabelMouseListener());
  }

  /**
   * Install a mouse listener on the label. Otherwise, the RadioButton would only react on mouse clicks on the button.
   */
  private class P_LabelMouseListener implements MouseListener {

    @Override
    public void mouseDoubleClick(MouseEvent e) {
    }

    @Override
    public void mouseDown(MouseEvent e) {
    }

    @Override
    public void mouseUp(MouseEvent e) {
      if (leftMouseButtonClicked(e)) {
        m_btn.setSelection(true);
        m_btn.setFocus();
        Event event = new Event();
        event.widget = m_btn;
        event.type = SWT.Selection;
        m_btn.handleButtonSelection(event);
      }
    }

    private boolean leftMouseButtonClicked(MouseEvent e) {
      if (e.button == 1) {
        return true;
      }
      return false;
    }
  }
}
