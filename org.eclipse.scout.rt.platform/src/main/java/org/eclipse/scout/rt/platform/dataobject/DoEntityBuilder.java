/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.dataobject;

import java.util.List;

import javax.annotation.PostConstruct;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;

/**
 * Simple builder for ad-hoc {@link DoEntity} instances.
 */
@Bean
public class DoEntityBuilder {

  protected DoEntity m_entity;

  @PostConstruct
  protected void init() {
    m_entity = BEANS.get(DoEntity.class);
  }

  /**
   * Adds new value to attribute map of entity.
   */
  public DoEntityBuilder put(String attributeName, Object value) {
    m_entity.put(attributeName, value);
    return this;
  }

  /**
   * Adds new list value to attribute map of entity.
   */
  public <V> DoEntityBuilder putList(String attributeName, List<V> value) {
    m_entity.putList(attributeName, value);
    return this;
  }

  /**
   * @return builded {@link DoEntity} instance
   */
  public DoEntity build() {
    return m_entity;
  }

  /**
   * @return serialized {@link String} representation of builded {@link DoEntity}
   */
  public String buildString() {
    return BEANS.get(IDataObjectMapper.class).writeValue(m_entity);
  }
}
