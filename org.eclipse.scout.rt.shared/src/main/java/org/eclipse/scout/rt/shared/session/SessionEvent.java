/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.session;

import java.util.EventObject;

import org.eclipse.scout.rt.shared.ISession;

/**
 * Event fired once the session state changed.
 *
 * @since 5.1
 */
public class SessionEvent extends EventObject {
  private static final long serialVersionUID = 1L;

  public static final int TYPE_STARTED = 100;
  public static final int TYPE_STOPPING = 105;
  public static final int TYPE_STOPPED = 110;

  private final int m_type;

  public SessionEvent(final ISession session, final int type) {
    super(session);
    m_type = type;
  }

  public int getType() {
    return m_type;
  }

  @Override
  public ISession getSource() {
    return (ISession) super.getSource();
  }
}
