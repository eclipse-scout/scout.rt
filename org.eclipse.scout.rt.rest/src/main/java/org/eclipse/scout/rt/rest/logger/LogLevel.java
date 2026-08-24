/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.logger;

import org.eclipse.scout.rt.dataobject.enumeration.EnumName;
import org.eclipse.scout.rt.dataobject.enumeration.IEnum;

@EnumName("scout.LogLevel")
public enum LogLevel implements IEnum {

  TRACE("trace"),
  DEBUG("debug"),
  INFO("info"),
  WARN("warn"),
  ERROR("error");

  private final String m_key;

  LogLevel(String key) {
    m_key = key;
  }

  @Override
  public String stringValue() {
    return m_key;
  }
}
