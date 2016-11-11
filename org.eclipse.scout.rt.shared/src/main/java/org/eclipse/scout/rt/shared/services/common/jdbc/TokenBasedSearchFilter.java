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
package org.eclipse.scout.rt.shared.services.common.jdbc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.platform.util.CollectionUtility;

/**
 * Use the {@link TokenBasedSearchFilterService} on the client side to use this class
 * 
 * @deprecated Will be removed in the O release.
 */
@Deprecated
public class TokenBasedSearchFilter extends SearchFilter implements Cloneable {
  private static final long serialVersionUID = 0L;

  private ArrayList<ValueToken> m_valueTokens;
  private ArrayList<WildcardStringToken> m_wildcardStringTokens;
  private ArrayList<AndNodeToken> m_treeTokens;

  public TokenBasedSearchFilter() {
    m_valueTokens = new ArrayList<ValueToken>(1);
    m_wildcardStringTokens = new ArrayList<WildcardStringToken>(1);
    m_treeTokens = new ArrayList<AndNodeToken>(1);
  }

  public void addToken(Integer tokenId, Object... values) {
    if (tokenId == null) {
      return;
    }
    m_valueTokens.add(new ValueToken(tokenId, values));
  }

  public void addWildcardStringToken(Integer tokenId, String value) {
    if (tokenId == null) {
      return;
    }
    m_wildcardStringTokens.add(new WildcardStringToken(tokenId, value));
  }

  public void addTreeToken(AndNodeToken root) {
    if (root == null || root.getChildren().size() == 0) {
      return;
    }
    m_treeTokens.add(root);
  }

  public List<ValueToken> getValueTokens() {
    return CollectionUtility.arrayList(m_valueTokens);
  }

  public List<WildcardStringToken> getWildcardStringTokens() {
    return CollectionUtility.arrayList(m_wildcardStringTokens);
  }

  public List<AndNodeToken> getTreeTokens() {
    return CollectionUtility.arrayList(m_treeTokens);
  }

  @Override
  public void clear() {
    super.clear();
    m_valueTokens.clear();
    m_wildcardStringTokens.clear();
    m_treeTokens.clear();
  }

  @Override
  public Object clone() {
    TokenBasedSearchFilter f = (TokenBasedSearchFilter) super.clone();
    f.m_valueTokens = new ArrayList<ValueToken>(this.m_valueTokens);
    f.m_wildcardStringTokens = new ArrayList<WildcardStringToken>(this.m_wildcardStringTokens);
    f.m_treeTokens = new ArrayList<AndNodeToken>(this.m_treeTokens);
    return f;
  }

  public static class ValueToken implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int m_tokenId;
    private final Object[] m_values;

    public ValueToken(int tokenId, Object[] values) {
      m_tokenId = tokenId;
      m_values = values;
    }

    public int getTokenId() {
      return m_tokenId;
    }

    public Object[] getValues() {
      return m_values;
    }
  }

  public static class WildcardStringToken implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int m_tokenId;
    private final String m_value;

    public WildcardStringToken(int tokenId, String value) {
      m_tokenId = tokenId;
      m_value = value;
    }

    public int getTokenId() {
      return m_tokenId;
    }

    public String getValue() {
      return m_value;
    }
  }

  public static class TreeNodeToken implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<TreeNodeToken> m_children = new ArrayList<TreeNodeToken>(1);
    private boolean m_negative;

    public TreeNodeToken() {
    }

    public void addChild(TreeNodeToken child) {
      if (child != null) {
        m_children.add(child);
      }
    }

    public List<TreeNodeToken> getChildren() {
      return CollectionUtility.arrayList(m_children);
    }

    public boolean isNegative() {
      return m_negative;
    }

    public void setNegative(boolean negative) {
      m_negative = negative;
    }
  }

  public static class AndNodeToken extends TreeNodeToken implements Serializable {
    private static final long serialVersionUID = 1L;

    public AndNodeToken() {
    }
  }

  public static class OrNodeToken extends TreeNodeToken implements Serializable {
    private static final long serialVersionUID = 1L;

    public OrNodeToken() {
    }
  }

  public static class AttributeNodeToken extends TreeNodeToken implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int m_tokenId;
    private final int m_op;
    private final List<? extends Object> m_values;

    public AttributeNodeToken(int tokenId, int op, List<? extends Object> values) {
      m_tokenId = tokenId;
      m_op = op;
      m_values = values;
    }

    public int getTokenId() {
      return m_tokenId;
    }

    public int getOp() {
      return m_op;
    }

    public List<Object> getValues() {
      return CollectionUtility.arrayList(m_values);
    }
  }

  public static class EntityNodeToken extends TreeNodeToken implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int m_tokenId;

    public EntityNodeToken(int tokenId) {
      m_tokenId = tokenId;
    }

    public int getTokenId() {
      return m_tokenId;
    }
  }

}
