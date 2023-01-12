/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.action.menu.root;

import java.util.EventObject;

import org.eclipse.scout.rt.client.ui.IModelEvent;

public class ContextMenuEvent extends EventObject implements IModelEvent {
  private static final long serialVersionUID = 1L;

  public static final int TYPE_STRUCTURE_CHANGED = 1;

  private final int m_eventType;

  public ContextMenuEvent(IContextMenu source, int eventType) {
    super(source);
    m_eventType = eventType;
  }

  @Override
  public IContextMenu getSource() {
    return (IContextMenu) super.getSource();
  }

  @Override
  public int getType() {
    return m_eventType;
  }
}
