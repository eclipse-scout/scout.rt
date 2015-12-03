/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform;

import java.util.EventObject;

import org.eclipse.scout.rt.platform.IPlatform.State;

/**
 * Event object describing a {@link IPlatform} change.
 * 
 * @since 5.2
 */
public class PlatformEvent extends EventObject {
  private static final long serialVersionUID = 1L;

  private final IPlatform.State m_state;

  public PlatformEvent(IPlatform platform, State state) {
    super(platform);
    m_state = state;
  }

  /**
   * @return The {@link IPlatform} sending the event.
   */
  @Override
  public IPlatform getSource() {
    return (IPlatform) super.getSource();
  }

  /**
   * @return The new platform state.
   */
  public IPlatform.State getState() {
    return m_state;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + getState().name() + "]";
  }
}
