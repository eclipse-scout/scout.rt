/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json;

/**
 * This enum defines common JSON event names.
 */
public enum JsonEventType {

  CLICK("click"),
  PROPERTY("property"),
  SELECT("select"),
  APP_LINK_ACTION("appLinkAction");

  private final String m_eventType;

  JsonEventType(String eventType) {
    m_eventType = eventType;
  }

  public String getEventType() {
    return m_eventType;
  }

  public boolean matches(String eventType) {
    return m_eventType.equals(eventType);
  }

  public boolean matches(JsonEvent jsonEvent) {
    return matches(jsonEvent.getType());
  }

  public static JsonEventType valueOfName(String eventType) {
    for (JsonEventType e : values()) {
      if (eventType.equals(e.m_eventType)) {
        return e;
      }
    }
    throw new IllegalArgumentException("Value with eventType=" + eventType + " does not exist");
  }
}
