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
package org.eclipse.scout.rt.ui.rap.window.desktop;

import java.util.EventObject;

/**
 * <h3>ViewTabSelectionEvent</h3> ...
 * 
 * @author aho
 * @since 3.7.0 June 2011
 */
public class ViewTabSelectionEvent extends EventObject {
  private static final long serialVersionUID = 1L;

  public static final int TYPE_VIEW_TAB_SELECTION = 1;
  public static final int TYPE_VIEW_TAB_CLOSE_SELECTION = 2;

  private final int m_eventType;

  public ViewTabSelectionEvent(ViewStackTabButton source, int eventType) {
    super(source);
    m_eventType = eventType;
  }

  @Override
  public ViewStackTabButton getSource() {
    return (ViewStackTabButton) super.getSource();
  }

  /**
   * @return the eventType
   */
  public int getEventType() {
    return m_eventType;
  }

}
