/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.messagebox;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.EventObject;

import org.eclipse.scout.rt.client.ui.IModelEvent;

public class MessageBoxEvent extends EventObject implements IModelEvent {
  private static final long serialVersionUID = 1L;
  // state
  public static final int TYPE_CLOSED = 900;

  private final int m_type;

  public MessageBoxEvent(IMessageBox mb, int type) {
    super(mb);
    m_type = type;
  }

  public IMessageBox getMessageBox() {
    return (IMessageBox) getSource();
  }

  @Override
  public int getType() {
    return m_type;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder();
    buf.append("MessageBoxEvent[");
    // decode type
    try {
      Field[] f = getClass().getDeclaredFields();
      for (Field aF : f) {
        if (Modifier.isPublic(aF.getModifiers())
            && Modifier.isStatic(aF.getModifiers())
            && aF.getName().startsWith("TYPE_")
            && ((Number) aF.get(null)).intValue() == m_type) {
          buf.append(aF.getName());
          break;
        }
      }
    }
    catch (Exception t) { // NOSONAR
      buf.append("#").append(m_type);
    }
    // messageBox
    if (getMessageBox() != null) {
      buf.append(" ").append(getMessageBox().getHeader());
    }
    buf.append("]");
    return buf.toString();
  }
}
