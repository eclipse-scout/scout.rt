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
package org.eclipse.scout.rt.ui.swt.util;

import java.util.HashMap;

import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.widgets.Control;

/**
 * <h3>SwtScoutFocusTraverseAdapter</h3>
 * 
 * @since 1.0.0 04.04.2008
 */
public abstract class SwtScoutFocusTraverseAdapter implements FocusListener, TraverseListener {
  private HashMap<String, Object> m_data = new HashMap<String, Object>();
  private Control m_control;

  public SwtScoutFocusTraverseAdapter(Control control) {
    m_control = control;
  }

  public final void focusLost(FocusEvent e) {
    if (m_control.getShell() != m_control.getDisplay().getActiveShell()) {
      temporaryFocusLost(e);
    }
    else {
      permanentFocusLost(e);
    }
  }

  /**
   * will be called when the focus is lost to an other shell or window
   * 
   * @param e
   */
  public void temporaryFocusLost(FocusEvent e) {
  }

  /**
   * will be called when the focus is lost permanently to an other control on
   * the same shell
   * 
   * @param e
   */
  public void permanentFocusLost(FocusEvent e) {
  }

  public void focusGained(FocusEvent e) {
  }

  public void keyTraversed(TraverseEvent e) {
  }

  public void setData(String identifier, Object data) {
    m_data.put(identifier, data);
  }

  public Object getData(String identifier) {
    return m_data.get(identifier);
  }

}
