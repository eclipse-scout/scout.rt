/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json;

/**
 * This enum defines common JSON event names.
 */
public enum JsonEventType {

  CLICK("click"),
  PROPERTY("property"),
  SELECT("select"),
  APP_LINK_ACTION("appLinkAction");

  private String m_eventType;

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
