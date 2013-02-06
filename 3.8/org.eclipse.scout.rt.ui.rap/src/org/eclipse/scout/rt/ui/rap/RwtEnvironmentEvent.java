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
package org.eclipse.scout.rt.ui.rap;

import java.util.EventObject;

/**
 * <h3>EnvironmentEvent</h3> ...
 * 
 * @since 3.7.0 June 2011
 */
public class RwtEnvironmentEvent extends EventObject {
  public static final int INACTIVE = 0x00;
  public static final int STARTING = 0x01;
  public static final int STARTED = 0x02;
  public static final int STOPPING = 0x04;
  public static final int STOPPED = 0x08;

  private static final long serialVersionUID = 1L;
  private final int m_type;

  public RwtEnvironmentEvent(IRwtEnvironment source, int type) {
    super(source);
    m_type = type;
  }

  @Override
  public IRwtEnvironment getSource() {
    return (IRwtEnvironment) super.getSource();
  }

  public int getType() {
    return m_type;
  }
}
