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
package org.eclipse.scout.rt.ui.rap.window;

import java.util.EventObject;

import org.eclipse.scout.rt.ui.rap.core.window.IRwtScoutPart;

public class RwtScoutPartEvent extends EventObject {
  private static final long serialVersionUID = 1L;

  /**
   * the part is opening but not yet open
   */
  public static final int TYPE_OPENING = 10;
  /**
   * the part is open but not yet active
   */
  public static final int TYPE_OPENED = 20;
  /**
   * the part is active
   */
  public static final int TYPE_ACTIVATED = 30;
  /**
   * the part is requesting closing but remains open
   */
  public static final int TYPE_CLOSING = 40;
  /**
   * the part is closed
   */
  public static final int TYPE_CLOSED = 50;

  private int m_type;
  public boolean doit = true;

  public RwtScoutPartEvent(IRwtScoutPart source, int type) {
    super(source);
    m_type = type;
  }

  public int getType() {
    return m_type;
  }
}
