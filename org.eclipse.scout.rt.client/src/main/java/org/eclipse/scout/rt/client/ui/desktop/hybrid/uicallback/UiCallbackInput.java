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

import org.eclipse.scout.rt.client.ui.desktop.hybrid.HybridActionContextElements;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.Bean;

@Bean
public class UiCallbackInput {

  private String m_callbackId;
  private IDoEntity m_data;
  private HybridActionContextElements m_contextElements;

  /**
   * An optional callback ID. All callbacks with the same ID are only sent once to the UI. When a response is received,
   * all pending callbacks with this ID are resolved at once. By default, a unique ID will be generated for each callback.
   */
  public String getCallbackId() {
    return m_callbackId;
  }

  /**
   * Assigns an explicit callback ID. All callbacks with the same ID are only sent once to the UI. When a response is received,
   * all pending callbacks with this ID are resolved at once. By default, a unique ID will be generated for each callback.
   */
  public UiCallbackInput withCallbackId(String callbackId) {
    m_callbackId = callbackId;
    return this;
  }

  /**
   * An optional {@link IDoEntity} to send to the UI.
   */
  public IDoEntity getData() {
    return m_data;
  }

  /**
   * Sets an {@link IDoEntity} to send to the UI.
   */
  public UiCallbackInput withData(IDoEntity data) {
    m_data = data;
    return this;
  }

  /**
   * Optional context elements to send to the UI.
   */
  public HybridActionContextElements getContextElements() {
    return m_contextElements;
  }

  /**
   * Sets context elements to send to the UI.
   */
  public UiCallbackInput withContextElements(HybridActionContextElements contextElements) {
    m_contextElements = contextElements;
    return this;
  }
}
