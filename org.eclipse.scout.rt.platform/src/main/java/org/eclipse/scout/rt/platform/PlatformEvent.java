/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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

  private final State m_state;

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
  public State getState() {
    return m_state;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + getState().name() + "]";
  }
}
