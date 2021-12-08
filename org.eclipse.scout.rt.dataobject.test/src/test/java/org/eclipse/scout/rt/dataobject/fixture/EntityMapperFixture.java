/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
