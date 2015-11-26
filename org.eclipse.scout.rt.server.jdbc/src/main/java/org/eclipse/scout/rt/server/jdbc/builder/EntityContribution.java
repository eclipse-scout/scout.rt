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
package org.eclipse.scout.rt.server.jdbc.builder;

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

  /**
   * moved to {@link EntityContributionUtility#constraintTextToContribution(String)}
   */
  public static EntityContribution create(String wherePart) {
    return EntityContributionUtility.constraintTextToContribution(wherePart);
  }

  public EntityContribution() {
    m_selectParts = new ArrayList<String>(2);
    m_fromParts = new ArrayList<String>(2);
    m_whereParts = new ArrayList<String>(2);
    m_groupByParts = new ArrayList<String>(2);
    m_havingParts = new ArrayList<String>(2);
  }

  public EntityContribution(EntityContribution... contributions) {
    this();
    if (contributions != null) {
      for (EntityContribution contrib : contributions) {
        add(contrib);
      }
    }
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
   * @return the life list Note that when adding a non-aggregated part to this list, also add it to the
   *         {@link #getGroupByParts()}
   *         <p>
   *         The entity builder checks if the size of {@link #getSelectParts()} and {@link #getGroupByParts()} is
   *         different. If yes, it activates the groupBy clause otherwise it hides it.
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

  /**
   * Adds the given expression to the list of select parts. If the expression is not aggregating, it is added to the
   * group by parts as well.
   * <p/>
   * <b>Note</b>: The given expression represents exactly one column in the result set and it must not define an alias.
   * 
   * @param expression
   *          SQL expression representing a single column in the result set without an attribute alias
   * @param aggregating
   *          <code>true</code> if the given expression uses a SQL aggregation, <code>false</code> otherwise.
   * @since 3.8.1
   */
  public void addSelectExpression(String expression, boolean aggregating) {
    getSelectParts().add(expression);
    if (!aggregating) {
      getGroupByParts().add(expression);
    }
  }

  /**
   * Adds the given expression to the list of from parts.
   * 
   * @param expression
   *          SQL table expression (e.g. table name or subquery)
   * @since 3.8.1
   */
  public void addFromExpression(String expression) {
    getFromParts().add(expression);
  }

  /**
   * Adds the given constraint to the list of where parts.
   * 
   * @param constraint
   *          SQL where constraint
   * @since 3.8.1
   */
  public void addWhereConstraint(String constraint) {
    getWhereParts().add(constraint);
  }

  /**
   * Adds the given expression to the list of group by parts.
   * <p/>
   * <b>Note</b>: The given expression represents exactly one column in the result set.
   * 
   * @param expression
   *          SQL expression representing a single column in the result set
   * @since 3.8.1
   */
  public void addGroupByExpression(String expression) {
    getGroupByParts().add(expression);
  }

  /**
   * Adds the given constraint to the list of having parts.
   * 
   * @param constraint
   *          SQL having constraint
   * @since 3.8.1
   */
  public void addHavingConstraint(String constraint) {
    getHavingParts().add(constraint);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((m_fromParts == null) ? 0 : m_fromParts.hashCode());
    result = prime * result + ((m_groupByParts == null) ? 0 : m_groupByParts.hashCode());
    result = prime * result + ((m_havingParts == null) ? 0 : m_havingParts.hashCode());
    result = prime * result + ((m_selectParts == null) ? 0 : m_selectParts.hashCode());
    result = prime * result + ((m_whereParts == null) ? 0 : m_whereParts.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    EntityContribution other = (EntityContribution) obj;
    if (m_fromParts == null) {
      if (other.m_fromParts != null) {
        return false;
      }
    }
    else if (!m_fromParts.equals(other.m_fromParts)) {
      return false;
    }
    if (m_groupByParts == null) {
      if (other.m_groupByParts != null) {
        return false;
      }
    }
    else if (!m_groupByParts.equals(other.m_groupByParts)) {
      return false;
    }
    if (m_havingParts == null) {
      if (other.m_havingParts != null) {
        return false;
      }
    }
    else if (!m_havingParts.equals(other.m_havingParts)) {
      return false;
    }
    if (m_selectParts == null) {
      if (other.m_selectParts != null) {
        return false;
      }
    }
    else if (!m_selectParts.equals(other.m_selectParts)) {
      return false;
    }
    if (m_whereParts == null) {
      if (other.m_whereParts != null) {
        return false;
      }
    }
    else if (!m_whereParts.equals(other.m_whereParts)) {
      return false;
    }
    return true;
  }
}
