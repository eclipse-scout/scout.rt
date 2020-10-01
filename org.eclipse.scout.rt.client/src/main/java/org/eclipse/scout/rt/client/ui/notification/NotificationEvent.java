/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui.notification;

import java.util.EventObject;

import org.eclipse.scout.rt.client.ui.IModelEvent;

public class NotificationEvent extends EventObject implements IModelEvent {
  private static final long serialVersionUID = 1L;
  // state
  public static final int TYPE_CLOSED = 900;

  private final int m_type;

  public NotificationEvent(INotification mb, int type) {
    super(mb);
    m_type = type;
  }

  public INotification getNotification() {
    return (INotification) getSource();
  }

  @Override
  public int getType() {
    return m_type;
  }
}
