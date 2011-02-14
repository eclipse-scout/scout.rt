/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.services.common.jdbc.builder;

import java.util.ArrayList;
import java.util.List;

/**
 * Contribution of an attribute to its containing entity
 */
public class EntityContribution {
  private final List<String> m_selectParts;
  private final List<String> m_fromParts;
  private final List<String> m_whereParts;
  private final List<String> m_groupByParts;
  private final List<String> m_havingParts;

  public static EntityContribution create(String wherePart) {
    EntityContribution contrib = new EntityContribution();
    if (wherePart != null) {
      contrib.getWhereParts().add(wherePart);
    }
    return contrib;
  }

  public EntityContribution() {
    m_selectParts = new ArrayList<String>(2);
    m_fromParts = new ArrayList<String>(2);
    m_whereParts = new ArrayList<String>(2);
    m_groupByParts = new ArrayList<String>(2);
    m_havingParts = new ArrayList<String>(2);
  }

  public boolean isEmpty() {
    return m_selectParts.size() + m_fromParts.size() + m_whereParts.size() + m_groupByParts.size() + m_havingParts.size() == 0;
  }

  public void add(EntityContribution c) {
    if (c == null) {
      return;
    }
    getSelectParts().addAll(c.getSelectParts());
    getFromParts().addAll(c.getFromParts());
    getWhereParts().addAll(c.getWhereParts());
    getGroupByParts().addAll(c.getGroupByParts());
    getHavingParts().addAll(c.getHavingParts());
  }

  /**
   * @return the life list
   */
  public List<String> getSelectParts() {
    return m_selectParts;
  }

  /**
   * @return the life list
   */
  public List<String> getFromParts() {
    return m_fromParts;
  }

  /**
   * @return the life list
   */
  public List<String> getWhereParts() {
    return m_whereParts;
  }

  /**
   * @return the life list
   */
  public List<String> getGroupByParts() {
    return m_groupByParts;
  }

  /**
   * @return the life list
   */
  public List<String> getHavingParts() {
    return m_havingParts;
  }

}
