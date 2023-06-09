/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.uinotification;

import java.io.Serializable;

import org.eclipse.scout.rt.api.uinotification.UiNotificationMessageDo;
import org.eclipse.scout.rt.dataobject.IDataObjectMapper;
import org.eclipse.scout.rt.platform.BEANS;

public class UiNotificationClusterMessage implements Serializable {
  private static final long serialVersionUID = 1L;
  private String m_json;

  public UiNotificationClusterMessage(UiNotificationMessageDo notification) {
    m_json = BEANS.get(IDataObjectMapper.class).writeValue(notification);
  }

  public UiNotificationMessageDo get() {
    return BEANS.get(IDataObjectMapper.class).readValue(m_json, UiNotificationMessageDo.class);
  }
}
