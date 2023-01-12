/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.browserfield;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.EventObject;

import org.eclipse.scout.rt.client.ui.IModelEvent;

public class BrowserFieldEvent extends EventObject implements IModelEvent {
  private static final long serialVersionUID = 1L;

  public static final int TYPE_CONTENT_CHANGED = 900;
  public static final int TYPE_POST_MESSAGE = 901;

  private final int m_type;
  private Object m_message;
  private String m_targetOrigin;

  public BrowserFieldEvent(IBrowserField browserField, int type) {
    super(browserField);
    m_type = type;
  }

  public IBrowserField getFileChooser() {
    return (IBrowserField) getSource();
  }

  @Override
  public int getType() {
    return m_type;
  }

  public Object getMessage() {
    return m_message;
  }

  public BrowserFieldEvent withMessage(Object message) {
    m_message = message;
    return this;
  }

  public String getTargetOrigin() {
    return m_targetOrigin;
  }

  public BrowserFieldEvent withTargetOrigin(String targetOrigin) {
    m_targetOrigin = targetOrigin;
    return this;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder();
    buf.append(getClass().getSimpleName()).append("[");
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
    buf.append("]");
    return buf.toString();
  }
}
