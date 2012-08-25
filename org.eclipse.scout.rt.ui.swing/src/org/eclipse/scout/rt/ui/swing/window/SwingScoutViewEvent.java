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
package org.eclipse.scout.rt.ui.swing.window;

import java.util.EventObject;

public class SwingScoutViewEvent extends EventObject {
  private static final long serialVersionUID = 1L;
  /**
   * the view is open but not yet active
   */
  public static final int TYPE_OPENED = 10;
  /**
   * the view is active
   */
  public static final int TYPE_ACTIVATED = 20;
  /**
   * the view is requesting closing but remains open
   */
  public static final int TYPE_CLOSING = 30;
  /**
   * the view is closed
   */
  public static final int TYPE_CLOSED = 40;

  private int m_type;

  public SwingScoutViewEvent(ISwingScoutView source, int type) {
    super(source);
    m_type = type;
  }

  public int getType() {
    return m_type;
  }

}
