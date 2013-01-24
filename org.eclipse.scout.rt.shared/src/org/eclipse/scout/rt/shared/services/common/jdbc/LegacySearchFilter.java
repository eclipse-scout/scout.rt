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
package org.eclipse.scout.rt.shared.services.common.jdbc;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.NVPair;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.service.SERVICES;

public class LegacySearchFilter extends SearchFilter implements Cloneable {
  private static final long serialVersionUID = 0L;

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(LegacySearchFilter.class);

  private int m_sBindSeq;
  private HashMap<String, Object> m_binds = new HashMap<String, Object>();
  private StringBuffer m_where = new StringBuffer("");

  public LegacySearchFilter() {
  }

  /**
   * @return live map of all binds changes to this map are immediately reflected
   *         inside the instance
   */
  public Map<String, Object> getBindMap() {
    return m_binds;
  }

  public void addBind(String name, Object value) {
    if (name != null) {
      m_binds.put(name, value);
    }
  }

  public void addSpecialWhereToken(Object o) throws ProcessingException {
    WhereToken tok = SERVICES.getService(ILegacySqlQueryService.class).resolveSpecialConstraint(o);
    if (tok.getBinds() != null) {
      m_binds.putAll(tok.getBinds());
    }
    addWhereToken(tok.getText(), tok.getS());
  }

  /**
   * clear all existing filter elements and add only this one same as {@link #clear()} followed by
   * {@link #addWhere(String,NVPair...)}
   */
  public void setWhere(String sql, NVPair... customBinds) {
    clear();
    addWhere(sql, customBinds);
  }

  /**
   * clear all existing filter elements and add only this one same as {@link #clear()} followed by
   * {@link #addWhereToken(String)}
   */
  public void setWhereToken(String sql) {
    clear();
    addWhereToken(sql);
  }

  /**
   * clear all existing filter elements and add only this one same as {@link #clear()} followed by
   * {@link #addWhereToken(String,Object)}
   */
  public void setWhereToken(String sql, Object valueForS) {
    clear();
    addWhereToken(sql, valueForS);
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
   * generates a random generic bind name useful for custom replacements of S
   * values
   */
  public String getNextGenericBindName() {
    String bindName = "__" + m_sBindSeq;
    m_sBindSeq++;
    return bindName;
  }

  /**
   * add sql part with bind references to :S and #S# the ADD keyword is added
   * (pre-pended) automatically
   */
  public void addWhereToken(String sql, Object valueForS) {
    if (sql != null) {
      // create generic bind for S
      String bindName = "__" + m_sBindSeq;
      m_sBindSeq++;
      sql = sql.replaceAll(":S([^a-zA-Z0-9_])", ":" + bindName + "$1");
      sql = sql.replaceAll(":S$", ":" + bindName);
      sql = sql.replaceAll("#S#", "#" + bindName + "#");
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
        m_binds.put(p.getName(), p.getValue());
      }
    }
  }

  @Override
  public void clear() {
    super.clear();
    m_where.setLength(0);
    m_binds.clear();
  }

  public String getWhere() {
    return m_where.toString();
  }

  public String getWherePlain() throws ProcessingException {
    return SERVICES.getService(ILegacySqlQueryService.class).createPlainText(getWhere(), getBindMap());
  }

  @Override
  public Object clone() {
    LegacySearchFilter f = (LegacySearchFilter) super.clone();
    f.m_sBindSeq = m_sBindSeq;
    f.m_binds = new HashMap<String, Object>(m_binds);
    f.m_where = new StringBuffer(m_where);
    return f;
  }

  @Override
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("SearchFilter[");
    if (m_where != null && m_where.length() > 0) {
      buf.append(m_where);
      buf.append(" ");
    }
    if (m_binds != null && m_binds.size() > 0) {
      buf.append(m_binds);
    }
    buf.append("]");
    return buf.toString();
  }

  public static class StringLikeConstraint implements Serializable {
    private static final long serialVersionUID = 1L;

    private String m_term;
    private String m_value;

    public StringLikeConstraint(String term, String value) {
      m_term = term;
      m_value = value;
    }

    public String getTerm() {
      return m_term;
    }

    public String getValue() {
      return m_value;
    }
  }

  public static class ComposerConstraint implements Serializable {
    private static final long serialVersionUID = 1L;

    private String m_term;
    private Map<String, ComposerAttributeRef> m_attributeRefMap;

    public ComposerConstraint(String term, Map<String, ComposerAttributeRef> attributeRefMap) {
      m_term = term;
      m_attributeRefMap = attributeRefMap;
    }

    public String getTerm() {
      return m_term;
    }

    public Map<String, ComposerAttributeRef> getAttributeRefMap() {
      return m_attributeRefMap;
    }

  }

  public static class ComposerAttributeRef implements Serializable {
    private static final long serialVersionUID = 1L;

    private int m_op;
    private String m_attribute;
    private String m_bindName;
    private Object m_value;

    public ComposerAttributeRef(int op, String attribute, String bindName, Object value) {
      m_op = op;
      m_attribute = attribute;
      m_bindName = bindName;
      m_value = value;
    }

    public int getOp() {
      return m_op;
    }

    public String getAttribute() {
      return m_attribute;
    }

    public String getBindName() {
      return m_bindName;
    }

    public Object getValue() {
      return m_value;
    }
  }

  public static class WhereToken implements Serializable {
    private static final long serialVersionUID = 1L;

    private String m_text;
    private Object m_s;
    private Map<String, Object> m_binds;

    public WhereToken(String text, Object s, Map<String, Object> binds) {
      m_text = text;
      m_s = s;
      m_binds = binds;
    }

    public String getText() {
      return m_text;
    }

    public Object getS() {
      return m_s;
    }

    public Map<String, Object> getBinds() {
      return m_binds;
    }

  }
}
