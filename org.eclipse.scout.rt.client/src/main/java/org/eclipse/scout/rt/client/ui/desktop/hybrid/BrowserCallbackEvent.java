/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.hybrid;

import static org.eclipse.scout.rt.platform.util.Assertions.*;

import java.util.EventObject;

import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.dataobject.IDoEntity;

public class BrowserCallbackEvent extends EventObject {

  private static final long serialVersionUID = 1L;

  private final String m_callbackId;
  private final String m_jsHandlerObjectType;
  private final IDoEntity m_data;
  private final IWidget m_owner;

  public BrowserCallbackEvent(Object source, String callbackId, String jsHandlerObjectType, IDoEntity data, IWidget owner) {
    super(assertNotNull(source));
    m_callbackId = assertNotNullOrEmpty(callbackId);
    m_jsHandlerObjectType = assertNotNullOrEmpty(jsHandlerObjectType);
    m_data = data;
    m_owner = assertNotNull(owner);
  }

  public IDoEntity getData() {
    return m_data;
  }

  public String getCallbackId() {
    return m_callbackId;
  }

  public String getJsHandlerObjectType() {
    return m_jsHandlerObjectType;
  }

  public IWidget getOwner() {
    return m_owner;
  }
}
