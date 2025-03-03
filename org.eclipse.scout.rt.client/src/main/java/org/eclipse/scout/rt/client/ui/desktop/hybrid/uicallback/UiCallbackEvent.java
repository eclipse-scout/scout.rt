/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.hybrid.uicallback;

import static org.eclipse.scout.rt.platform.util.Assertions.*;

import java.util.EventObject;

import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.desktop.hybrid.HybridActionContextElements;
import org.eclipse.scout.rt.dataobject.IDoEntity;

/**
 * Event fired when a new callback is sent to the browser.
 */
public class UiCallbackEvent extends EventObject {

  private static final long serialVersionUID = 1L;

  private final String m_callbackId;
  private final String m_jsHandlerObjectType;
  private final IDoEntity m_data;
  private final IWidget m_owner;
  private final HybridActionContextElements m_contextElements;

  public UiCallbackEvent(Object source, String callbackId, String jsHandlerObjectType, IWidget owner, IDoEntity data, HybridActionContextElements contextElements) {
    super(assertNotNull(source));
    m_callbackId = assertNotNullOrEmpty(callbackId);
    m_jsHandlerObjectType = assertNotNullOrEmpty(jsHandlerObjectType, "JavaScript handler objectType is mandatory.");
    m_owner = assertNotNull(owner);
    m_data = data;
    m_contextElements = contextElements;
  }

  /**
   * @return The callback id.
   */
  public String getCallbackId() {
    return m_callbackId;
  }

  /**
   * @return The ObjectType of the UI handler.
   */
  public String getJsHandlerObjectType() {
    return m_jsHandlerObjectType;
  }

  /**
   * @return The owner {@link IWidget} of the UI callback.
   */
  public IWidget getOwner() {
    return m_owner;
  }

  /**
   * @return The {@link IDoEntity} to send to the UI.
   */
  public IDoEntity getData() {
    return m_data;
  }

  /**
   * @return The {@link HybridActionContextElements context elements} to send to the UI.
   */
  public HybridActionContextElements getContextElements() {
    return m_contextElements;
  }
}
