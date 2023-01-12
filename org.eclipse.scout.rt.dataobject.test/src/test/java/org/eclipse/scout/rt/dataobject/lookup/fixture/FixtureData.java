/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.lookup.fixture;

public class FixtureData {
  private final Long m_id;
  private final String m_text;
  private final String m_additionalData;
  private final Boolean m_active;

  public FixtureData(Long id, String text, String additionalData, Boolean active) {
    m_id = id;
    m_text = text;
    m_additionalData = additionalData;
    m_active = active;
  }

  public Long getId() {
    return m_id;
  }

  public String getText() {
    return m_text;
  }

  public String getAdditionalData() {
    return m_additionalData;
  }

  public Boolean getActive() {
    return m_active;
  }

  @Override
  public String toString() {
    return "FixtureData [m_id=" + m_id + ", m_text=" + m_text + ", m_additionalData=" + m_additionalData + ", m_active=" + m_active + "]";
  }
}
