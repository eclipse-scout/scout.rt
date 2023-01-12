/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.fixture;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.holders.Holder;

@Bean
public class EntityMapperFixture {

  private Holder<String> m_id;
  private String m_otherId;
  private Long m_contributedValue;

  public EntityMapperFixture() {
    m_id = new Holder<>();
  }

  public Holder<String> getIdHolder() {
    return m_id;
  }

  public String getId() {
    return m_id.getValue();
  }

  public void setId(String id) {
    m_id.setValue(id);
  }

  public String getOtherId() {
    return m_otherId;
  }

  public void setOtherId(String otherId) {
    m_otherId = otherId;
  }

  public Long getContributedValue() {
    return m_contributedValue;
  }

  public void setContributedValue(Long contributedValue) {
    m_contributedValue = contributedValue;
  }
}
