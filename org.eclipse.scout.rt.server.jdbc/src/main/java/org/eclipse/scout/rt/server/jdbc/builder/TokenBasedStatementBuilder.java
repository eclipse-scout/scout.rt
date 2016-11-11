/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.jdbc.builder;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.StringUtility.ITagProcessor;
import org.eclipse.scout.rt.server.jdbc.SQL;
import org.eclipse.scout.rt.server.jdbc.internal.legacy.LegacyStatementBuilder;
import org.eclipse.scout.rt.server.jdbc.style.ISqlStyle;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.shared.services.common.jdbc.TokenBasedSearchFilter;
import org.eclipse.scout.rt.shared.services.common.jdbc.TokenBasedSearchFilter.AndNodeToken;
import org.eclipse.scout.rt.shared.services.common.jdbc.TokenBasedSearchFilter.AttributeNodeToken;
import org.eclipse.scout.rt.shared.services.common.jdbc.TokenBasedSearchFilter.EntityNodeToken;
import org.eclipse.scout.rt.shared.services.common.jdbc.TokenBasedSearchFilter.OrNodeToken;
import org.eclipse.scout.rt.shared.services.common.jdbc.TokenBasedSearchFilter.TreeNodeToken;
import org.eclipse.scout.rt.shared.services.common.jdbc.TokenBasedSearchFilter.ValueToken;
import org.eclipse.scout.rt.shared.services.common.jdbc.TokenBasedSearchFilter.WildcardStringToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds sql based on static defined token fragments.
 * <p>
 * Checks the value type of every bind to avoid unexpected type injections. Makes sure no sql injection is applied.
 * <p>
 * subclass to add token mappings and handle complex tokens in {@link #buildToken(int, Object[])}
 *
 * @deprecated Will be removed in the O release.
 */
@SuppressWarnings("deprecation")
@Deprecated
public class TokenBasedStatementBuilder {
  private static final Logger LOG = LoggerFactory.getLogger(TokenBasedStatementBuilder.class);

  private final ISqlStyle m_sqlStyle;
  private final HashMap<Integer, String> m_tokenMappings = new HashMap<Integer, String>();
  private final AtomicLong m_bindSeq = new AtomicLong();
  private final HashMap<String, Object> m_binds = new HashMap<String, Object>();
  private final StringBuffer m_where = new StringBuffer("");
  /**
   * speed-up due to static caching of default mappings
   */
  private TokenBasedStatementBuilder m_staticBuilder;

  public TokenBasedStatementBuilder() {
    this(null);
  }

  public TokenBasedStatementBuilder(ISqlStyle sqlStyle) {
    m_sqlStyle = sqlStyle != null ? sqlStyle : SQL.getSqlStyle();
  }

  public void setStaticBuilder(TokenBasedStatementBuilder staticBuilder) {
    m_staticBuilder = staticBuilder;
  }

  public TokenBasedStatementBuilder getStaticBuilder() {
    return m_staticBuilder;
  }

  public ISqlStyle getSqlStyle() {
    return m_sqlStyle;
  }

  public void build(SearchFilter filter) {
    if (!(filter instanceof TokenBasedSearchFilter)) {
      LOG.warn("unexpected filter type", new IllegalArgumentException("expected search filter of type " + TokenBasedSearchFilter.class.getSimpleName()));
      return;
    }
    TokenBasedSearchFilter tokenFilter = (TokenBasedSearchFilter) filter;
    for (ValueToken t : tokenFilter.getValueTokens()) {
      checkValueTypes(t.getValues());
      buildToken(t.getTokenId(), t.getValues());
    }
    for (WildcardStringToken t : tokenFilter.getWildcardStringTokens()) {
      if (t.getValue() != null) {
        String s = m_sqlStyle.toLikePattern(t.getValue());
        buildToken(t.getTokenId(), new Object[]{s});
      }
    }
    for (AndNodeToken root : tokenFilter.getTreeTokens()) {
      buildTree(root);
    }
  }

  public void addTokenMapping(int key, String sql) {
    if (m_tokenMappings.put(key, sql) != null) {
      throw new IllegalArgumentException("duplicate token key: " + key);
    }
  }

  public String getTokenMapping(int key) {
    String s = m_tokenMappings.get(key);
    if (s != null) {
      return s;
    }
    if (m_staticBuilder != null) {
      return m_staticBuilder.getTokenMapping(key);
    }
    return null;
  }

  public boolean hasTokenMapping(int key) {
    boolean b = m_tokenMappings.containsKey(key);
    if (b) {
      return b;
    }
    if (m_staticBuilder != null) {
      return m_staticBuilder.hasTokenMapping(key);
    }
    return false;
  }

  /**
   * {@link #checkValueType(Object)}
   */
  public void checkValueTypes(Object[] args) {
    if (args != null) {
      for (Object o : args) {
        checkValueType(o);
      }
    }
  }

  /**
   * Check if value is of basic value type (avoid injection of unexpected complex types)
   * <p>
   * Default allows only Number, String, Date, Boolean, Character, Byte as well as arrays and primitive types of
   * forementioned.
   */
  public void checkValueType(Object o) {
    if (o == null) {
      return;
    }
    Class<?> c = o.getClass();
    while (c != null && c.isArray()) {
      c = c.getComponentType();
    }
    if (c == null) {
      return;
    }
    if (c.isPrimitive()) {
      return;
    }
    if (Number.class.isAssignableFrom(c) ||
        String.class.isAssignableFrom(c) ||
        Date.class.isAssignableFrom(c) ||
        Boolean.class.isAssignableFrom(c) ||
        Character.class.isAssignableFrom(c) ||
        Byte.class.isAssignableFrom(c)) {
      return;
    }
    //illegal type
    throw new IllegalArgumentException("unexpected and therefore illegal parameter type: " + c.getName());
  }

  public void addWhereToken(String sql) {
    if (sql != null) {
      m_where.append(" AND ");
      addWhere(sql);
    }
  }

  /**
   * @param sql
   * @param sValue
   *          is the value for the :S bind
   */
  public void addWhereToken(String sql, Object sValue) {
    if (sql != null) {
      // create generic bind for S
      String bindName = "__" + m_bindSeq.incrementAndGet();
      String sOld = sql;
      sql = sql.replaceAll(":S([^a-zA-Z0-9_])", ":" + bindName + "$1");
      sql = sql.replaceAll(":S$", ":" + bindName);
      sql = sql.replaceAll("#S#", "#" + bindName + "#");
      m_where.append(" AND ");
      m_where.append(sql);
      //add bind if used
      if (!sOld.equals(sql)) {
        m_binds.put(bindName, sValue);
      }
    }
  }

  /**
   * Just adds the (localized) bind value for :S, replaces :S by the new bind name inside the statement and returns the
   * localized s
   *
   * @param sql
   * @param sValue
   *          is the value for the :S bind
   */
  public String prepareWhereToken(String sql, Object sValue) {
    if (sql == null) {
      return null;
    }
    // create generic bind for S
    String bindName = "__" + m_bindSeq.incrementAndGet();
    String oldSql = sql;
    sql = sql.replaceAll(":S([^a-zA-Z0-9_])", ":" + bindName + "$1");
    sql = sql.replaceAll(":S$", ":" + bindName);
    sql = sql.replaceAll("#S#", "#" + bindName + "#");
    //add bind if used
    if (!oldSql.equals(sql)) {
      m_binds.put(bindName, sValue);
    }
    return sql;
  }

  public void addWhere(String sql) {
    if (sql != null) {
      m_where.append(sql);
    }
  }

  public String getWhere() {
    return m_where.toString();
  }

  public Map<String, Object> getBinds() {
    return m_binds;
  }

  /**
   * subclass to handle complex tokens
   */
  protected void buildToken(int key, Object[] args) {
    //default
    String s = getTokenMapping(key);
    if (s == null && !hasTokenMapping(key)) {
      LOG.warn("null FieldMapping for key {}", key);
    }
    if (s != null && s.length() > 0 && !"1=1".equals(s)) {
      Object sValue = (args != null && args.length > 0 ? args[0] : null);
      this.addWhereToken(s, sValue);
    }
  }

  protected void buildTree(AndNodeToken root) {
    String s = buildAndNodes(root.getChildren(), false);
    if (s != null) {
      addWhereToken(s);
    }
  }

  protected String buildNode(TreeNodeToken node) {
    String s = null;
    if (node instanceof EntityNodeToken) {
      s = buildEntityNode((EntityNodeToken) node);
    }
    else if (node instanceof AttributeNodeToken) {
      s = buildAttributeNode((AttributeNodeToken) node);
    }
    else if (node instanceof AndNodeToken) {
      s = buildAndNodes(node.getChildren(), true);
    }
    else if (node instanceof OrNodeToken) {
      s = buildOrNodes(node.getChildren(), true);
    }
    if (s == null) {
      return null;
    }
    if (node.isNegative()) {
      return "NOT (" + s + ")";
    }
    return s;
  }

  protected String buildAndNodes(List<TreeNodeToken> nodes, boolean bracket) {
    StringBuilder buf = new StringBuilder();
    int count = 0;
    for (TreeNodeToken node : nodes) {
      String s = buildNode(node);
      if (s != null) {
        if (count > 0) {
          buf.append(" AND ");
        }
        buf.append(s);
        count++;
      }
    }
    if (count == 0) {
      return null;
    }
    if (bracket) {
      return "(" + buf.toString() + ")";
    }
    return buf.toString();
  }

  protected String buildOrNodes(List<TreeNodeToken> nodes, boolean bracket) {
    StringBuilder buf = new StringBuilder();
    int count = 0;
    for (TreeNodeToken node : nodes) {
      String s = buildNode(node);
      if (s != null) {
        if (count > 0) {
          buf.append(" OR ");
        }
        buf.append(s);
        count++;
      }
    }
    if (count == 0) {
      return null;
    }
    if (bracket) {
      return "(" + buf.toString() + ")";
    }
    return buf.toString();
  }

  protected String buildEntityNode(EntityNodeToken eNode) {
    String stm = getTokenMapping(eNode.getTokenId());
    if (stm == null) {
      LOG.warn("no token defined for entity {}", eNode.getTokenId());
      return null;
    }
    // add children
    String ch = buildAndNodes(eNode.getChildren(), true);
    if (ch != null) {
      ch = " AND " + ch;
    }
    else {
      ch = "";
    }
    if (stm.indexOf("<attributes/>") >= 0) {
      stm = StringUtility.replace(stm, "<attributes/>", ch);
    }
    else if (stm.indexOf("#W#") >= 0) {
      // legacy
      stm = StringUtility.replace(stm, "#W#", ch);
    }
    else if (ch.length() > 0) {
      stm = stm + ch;
    }
    if (stm.length() > 0) {
      return stm;
    }
    else {
      return null;
    }
  }

  protected String buildAttributeNode(final AttributeNodeToken aNode) {
    String baseStatement = getTokenMapping(aNode.getTokenId());
    if (baseStatement == null) {
      LOG.warn("no token defined for attribute {}", aNode.getTokenId());
      return null;
    }
    final Object sValue = CollectionUtility.firstElement(aNode.getValues());
    // replace the S in the string by the generated unique bind name
    String sql = prepareWhereToken(baseStatement, sValue);
    if (!sql.equals(baseStatement)) {
      // the attribute was of the form: P.PNAME = #S# (contains references to S)
      // this is legacy and is not supported for attribute decoration below
      return sql;
    }
    // the attribute was of the form: P.PNAME or
    // <attribute>P.PNAME</attribute>
    // make sure there is an attribute tag in the string, if none enclose all
    // by default
    if (sql.indexOf("<attribute>") < 0) {
      sql = "<attribute>" + sql + "</attribute>";
    }
    // tag replacement
    ITagProcessor processor = new ITagProcessor() {
      @Override
      public String processTag(String tagName, String attribute) {
        return buildAttributeOpValue(attribute, aNode.getOp(), sValue);
      }
    };
    return StringUtility.replaceTags(sql, "attribute", processor);
  }

  protected String buildAttributeOpValue(String att, int op, Object value) {
    LegacyStatementBuilder b = new LegacyStatementBuilder(getSqlStyle());
    String attOpValue = b.resolveComposerAttribute(op, att, "S", value);
    //adds the bind but not the sql text
    String sql = prepareWhereToken(attOpValue, value);
    return sql;
  }

}
