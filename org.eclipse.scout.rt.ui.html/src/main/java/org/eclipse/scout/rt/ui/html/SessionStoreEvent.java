/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.ui.html;

import java.util.EventObject;

@SuppressWarnings({"serial", "squid:S2057"})
public class SessionStoreEvent extends EventObject {

  public static final int TYPE_UI_SESSION_REGISTERED = 100;
  public static final int TYPE_UI_SESSION_UNREGISTERED = 200;

  private final int m_type;

  public SessionStoreEvent(SessionStore source, int type) {
    super(source);
    m_type = type;
  }

  @Override
  public SessionStore getSource() {
    return (SessionStore) super.getSource();
  }

  public int getType() {
    return m_type;
  }
}
