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
package org.eclipse.scout.rt.server.services.common.jdbc.internal.legacy;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.commons.holders.NVPair;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle;
import org.eclipse.scout.rt.shared.data.form.fields.composer.AbstractComposerAttributeData;

public class LegacyStatementBuilder {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(LegacyStatementBuilder.class);

  private ISqlStyle m_sqlStyle;
  private Map<String, Object> m_bindMap;
  private long m_bindSeqNo = 0;
  private StringBuffer m_where;

  /**
   * @param sqlStyle
   */
  public LegacyStatementBuilder(ISqlStyle sqlStyle) {
    m_sqlStyle = sqlStyle;
    m_bindMap = new HashMap<String, Object>();
  }

  public String resolveComposerAttribute(int op, String attribute, String bindName, Object value) {
    return createComposerAttributeOpPattern(op, attribute, bindName, value);
  }

  /**
   * @return the life bind map
   */
  public Map<String, Object> getBindMap() {
    return m_bindMap;
  }

  public ISqlStyle getSqlStyle() {
    return m_sqlStyle;
  }

  /**
   * Convenience for {@link #getBindMap()}.put(name,value)
   */
  public void addBind(String name, Object value) {
    if (name != null) {
      getBindMap().put(name, value);
    }
  }

  /**
   * add sql part the ADD keyword is added (pre-pended) automatically
   */
  public void addWhereToken(String sql) {
    if (sql != null) {
      m_where.append(" AND ");
      addWhere(sql);
    }
  }

  /**
   * add sql part with bind references to :S and #S# the ADD keyword is added
   * (pre-pended) automatically
   */
  public void addWhereToken(String sql, Object valueForS) {
    if (sql != null) {
      // create generic bind for S
      String bindName = localizeBindName("S", "__");
      sql = localizeStatement(sql, "S", bindName);
      //
      m_where.append(" AND ");
      addWhere(sql, new NVPair(bindName, valueForS));
    }
  }

  /**
   * add sql part with custom binds the ADD keyword is NOT added (pre-pended)
   * automatically
   */
  public void addWhere(String sql, NVPair... customBinds) {
    if (sql != null) {
      m_where.append(sql);
      for (NVPair p : customBinds) {
        addBind(p.getName(), p.getValue());
      }
    }
  }

  public String getWhereConstraints() {
    return m_where.toString();
  }

  protected long getNextBindSeqNo() {
    return m_bindSeqNo++;
  }

  /**
   * Replace bind name by unique bind name so that it is not
   * conflicting with other parts that use the same statement
   * part and bind name. For example S is replaces by __S123.
   */
  protected String localizeBindName(String bindName, String prefix) {
    if (bindName != null) {
      String locName = prefix + bindName + getNextBindSeqNo();
      return locName;
    }
    else {
      return null;
    }
  }

  /**
   * Replace bind name in statement
   */
  protected String localizeStatement(String stm, String oldBindName, String newBindName) {
    stm = stm.replaceAll("#" + oldBindName + "#", "#" + newBindName + "#");
    stm = stm.replaceAll("\\&" + oldBindName + "\\&", "&" + newBindName + "&");
    stm = stm.replaceAll(":" + oldBindName + "([^A-Za-z0-9_])", ":" + newBindName + "$1");
    stm = stm.replaceAll(":" + oldBindName + "$", ":" + newBindName);
    return stm;
  }

  /**
   * @param bindMap
   *          out parameter containing all binds used in the created pattern
   * @return sql text with operation and bind names
   *         <p>
   *         Simple example for EQ operation
   * 
   *         <pre>
   * return something in the form of: attribute + &quot;=&quot; + &quot;:&quot; + bindName;
   * </pre>
   */
  protected String createComposerAttributeOpPattern(int op, String attribute, String bindName, Object value) {
    switch (op) {
      case AbstractComposerAttributeData.OPERATOR_NEQ: {
        getBindMap().put(bindName, value);
        return m_sqlStyle.createNEQ(attribute, bindName);
      }
      case AbstractComposerAttributeData.OPERATOR_LT: {
        getBindMap().put(bindName, value);
        return m_sqlStyle.createLT(attribute, bindName);
      }
      case AbstractComposerAttributeData.OPERATOR_LE: {
        getBindMap().put(bindName, value);
        return m_sqlStyle.createLE(attribute, bindName);
      }
      case AbstractComposerAttributeData.OPERATOR_EQ: {
        getBindMap().put(bindName, value);
        return m_sqlStyle.createEQ(attribute, bindName);
      }
      case AbstractComposerAttributeData.OPERATOR_GT: {
        getBindMap().put(bindName, value);
        return m_sqlStyle.createGT(attribute, bindName);
      }
      case AbstractComposerAttributeData.OPERATOR_GE: {
        getBindMap().put(bindName, value);
        return m_sqlStyle.createGE(attribute, bindName);
      }
      case AbstractComposerAttributeData.OPERATOR_DATE_IS_IN_DAYS: {
        getBindMap().put(bindName, value);
        return m_sqlStyle.createDateIsInDays(attribute, bindName);
      }
      case AbstractComposerAttributeData.OPERATOR_DATE_IS_IN_GE_DAYS: {
        getBindMap().put(bindName, value);
        return m_sqlStyle.createDateIsInGEDays(attribute, bindName);
      }
      case AbstractComposerAttributeData.OPERATOR_DATE_IS_IN_GE_MONTHS: {
        getBindMap().put(bindName, value);
        return m_sqlStyle.createDateIsInGEMonths(attribute, bindName);
      }
      case AbstractComposerAttributeData.OPERATOR_DATE_IS_IN_LE_DAYS: {
        getBindMap().put(bindName, value);
        return m_sqlStyle.createDateIsInLEDays(attribute, bindName);
      }
      case AbstractComposerAttributeData.OPERATOR_DATE_IS_IN_LE_MONTHS: {
        getBindMap().put(bindName, value);
        return m_sqlStyle.createDateIsInLEMonths(attribute, bindName);
      }
      case AbstractComposerAttributeData.OPERATOR_DATE_IS_IN_LAST_DAYS: {
        getBindMap().put(bindName, value);
        return m_sqlStyle.createDateIsInLastDays(attribute, bindName);
      }
      case AbstractComposerAttributeData.OPERATOR_DATE_IS_IN_LAST_MONTHS: {
        getBindMap().put(bindName, value);
        return m_sqlStyle.createDateIsInLastMonths(attribute, bindName);
      }
      case AbstractComposerAttributeData.OPERATOR_DATE_IS_IN_MONTHS: {
        getBindMap().put(bindName, value);
        return m_sqlStyle.createDateIsInMonths(attribute, bindName);
      }
      case AbstractComposerAttributeData.OPERATOR_DATE_IS_IN_NEXT_DAYS: {
        getBindMap().put(bindName, value);
        return m_sqlStyle.createDateIsInNextDays(attribute, bindName);
      }
      case AbstractComposerAttributeData.OPERATOR_DATE_IS_IN_NEXT_MONTHS: {
        getBindMap().put(bindName, value);
        return m_sqlStyle.createDateIsInNextMonths(attribute, bindName);
      }
      case AbstractComposerAttributeData.OPERATOR_DATE_IS_NOT_TODAY: {
        return m_sqlStyle.createDateIsNotToday(attribute);
      }
      case AbstractComposerAttributeData.OPERATOR_DATE_IS_TODAY: {
        return m_sqlStyle.createDateIsToday(attribute);
      }
      case AbstractComposerAttributeData.OPERATOR_DATE_TIME_IS_IN_GE_HOURS: {
        getBindMap().put(bindName, value);
        return m_sqlStyle.createDateTimeIsInGEHours(attribute, bindName);
      }
      case AbstractComposerAttributeData.OPERATOR_DATE_TIME_IS_IN_GE_MINUTES: {
        getBindMap().put(bindName, value);
        return m_sqlStyle.createDateTimeIsInGEMinutes(attribute, bindName);
      }
      case AbstractComposerAttributeData.OPERATOR_DATE_TIME_IS_IN_LE_HOURS: {
        getBindMap().put(bindName, value);
        return m_sqlStyle.createDateTimeIsInLEHours(attribute, bindName);
      }
      case AbstractComposerAttributeData.OPERATOR_DATE_TIME_IS_IN_LE_MINUTES: {
        getBindMap().put(bindName, value);
        return m_sqlStyle.createDateTimeIsInLEMinutes(attribute, bindName);
      }
      case AbstractComposerAttributeData.OPERATOR_DATE_TIME_IS_NOT_NOW: {
        return m_sqlStyle.createDateTimeIsNotNow(attribute);
      }
      case AbstractComposerAttributeData.OPERATOR_DATE_TIME_IS_NOW: {
        return m_sqlStyle.createDateTimeIsNow(attribute);
      }
      case AbstractComposerAttributeData.OPERATOR_ENDS_WITH: {
        getBindMap().put(bindName, m_sqlStyle.toLikePattern(value));
        return m_sqlStyle.createEndsWith(attribute, bindName);
      }
      case AbstractComposerAttributeData.OPERATOR_NOT_ENDS_WITH: {
        getBindMap().put(bindName, m_sqlStyle.toLikePattern(value));
        return m_sqlStyle.createNotEndsWith(attribute, bindName);
      }
      case AbstractComposerAttributeData.OPERATOR_IN: {
        getBindMap().put(bindName, value);
        return m_sqlStyle.createIn(attribute, bindName);
      }
      case AbstractComposerAttributeData.OPERATOR_CONTAINS: {
        getBindMap().put(bindName, m_sqlStyle.toLikePattern(value));
        return m_sqlStyle.createContains(attribute, bindName);
      }
      case AbstractComposerAttributeData.OPERATOR_LIKE: {
        getBindMap().put(bindName, value);
        return m_sqlStyle.createLike(attribute, bindName);
      }
      case AbstractComposerAttributeData.OPERATOR_NOT_IN: {
        getBindMap().put(bindName, value);
        return m_sqlStyle.createNotIn(attribute, bindName);
      }
      case AbstractComposerAttributeData.OPERATOR_NOT_CONTAINS: {
        getBindMap().put(bindName, m_sqlStyle.toLikePattern(value));
        return m_sqlStyle.createNotContains(attribute, bindName);
      }
      case AbstractComposerAttributeData.OPERATOR_NOT_NULL: {
        return m_sqlStyle.createNotNull(attribute);
      }
      case AbstractComposerAttributeData.OPERATOR_NUMBER_NOT_NULL: {
        return m_sqlStyle.createNumberNotNull(attribute);
      }
      case AbstractComposerAttributeData.OPERATOR_NULL: {
        return m_sqlStyle.createNull(attribute);
      }
      case AbstractComposerAttributeData.OPERATOR_NUMBER_NULL: {
        return m_sqlStyle.createNumberNull(attribute);
      }
      case AbstractComposerAttributeData.OPERATOR_STARTS_WITH: {
        getBindMap().put(bindName, m_sqlStyle.toLikePattern(value));
        return m_sqlStyle.createStartsWith(attribute, bindName);
      }
      case AbstractComposerAttributeData.OPERATOR_NOT_STARTS_WITH: {
        getBindMap().put(bindName, m_sqlStyle.toLikePattern(value));
        return m_sqlStyle.createNotStartsWith(attribute, bindName);
      }
      case AbstractComposerAttributeData.OPERATOR_TIME_IS_IN_GE_HOURS: {
        getBindMap().put(bindName, value);
        return m_sqlStyle.createTimeIsInGEHours(attribute, bindName);
      }
      case AbstractComposerAttributeData.OPERATOR_TIME_IS_IN_GE_MINUTES: {
        getBindMap().put(bindName, value);
        return m_sqlStyle.createTimeIsInGEMinutes(attribute, bindName);
      }
      case AbstractComposerAttributeData.OPERATOR_TIME_IS_IN_HOURS: {
        getBindMap().put(bindName, value);
        return m_sqlStyle.createTimeIsInHours(attribute, bindName);
      }
      case AbstractComposerAttributeData.OPERATOR_TIME_IS_IN_LE_HOURS: {
        getBindMap().put(bindName, value);
        return m_sqlStyle.createTimeIsInLEHours(attribute, bindName);
      }
      case AbstractComposerAttributeData.OPERATOR_TIME_IS_IN_LE_MINUTES: {
        getBindMap().put(bindName, value);
        return m_sqlStyle.createTimeIsInLEMinutes(attribute, bindName);
      }
      case AbstractComposerAttributeData.OPERATOR_TIME_IS_IN_MINUTES: {
        getBindMap().put(bindName, value);
        return m_sqlStyle.createTimeIsInMinutes(attribute, bindName);
      }
      case AbstractComposerAttributeData.OPERATOR_TIME_IS_NOW: {
        return m_sqlStyle.createTimeIsNow(attribute);
      }
      case AbstractComposerAttributeData.OPERATOR_TIME_IS_NOT_NOW: {
        return m_sqlStyle.createTimeIsNotNow(attribute);
      }
      default: {
        throw new IllegalArgumentException("invalid operator: " + op);
      }
    }
  }

}
