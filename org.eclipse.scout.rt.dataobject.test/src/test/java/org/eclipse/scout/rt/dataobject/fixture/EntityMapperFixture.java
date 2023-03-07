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

import org.eclipse.scout.rt.dataobject.DoCollection;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.DoSet;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.holders.Holder;

@Bean
public class EntityMapperFixture {

  private Holder<String> m_id;
  private String m_otherId;
  private Long m_contributedValue;

  private DoCollection<String> m_stringCollection;
  private DoCollection<OtherEntityMapperFixtureDo> m_entityCollection;
  private DoList<OtherEntityMapperFixtureDo> m_entityList;
  private DoSet<OtherEntityMapperFixtureDo> m_entitySet;

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

  public DoCollection<String> getStringCollection() {
    return m_stringCollection;
  }

  public void setStringCollection(DoCollection<String> stringCollection) {
    m_stringCollection = stringCollection;
  }

  public DoCollection<OtherEntityMapperFixtureDo> getEntityCollection() {
    return m_entityCollection;
  }

  public void setEntityCollection(DoCollection<OtherEntityMapperFixtureDo> entityCollection) {
    m_entityCollection = entityCollection;
  }

  public DoList<OtherEntityMapperFixtureDo> getEntityList() {
    return m_entityList;
  }

  public void setEntityList(DoList<OtherEntityMapperFixtureDo> entityList) {
    m_entityList = entityList;
  }

  public DoSet<OtherEntityMapperFixtureDo> getEntitySet() {
    return m_entitySet;
  }

  public void setEntitySet(DoSet<OtherEntityMapperFixtureDo> entitySet) {
    m_entitySet = entitySet;
  }
}
