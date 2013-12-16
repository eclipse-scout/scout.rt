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
package org.eclipse.scout.rt.client.ui.form.fields.composer.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.StringUtility.ITagProcessor;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.form.fields.composer.attribute.AbstractComposerAttribute;
import org.eclipse.scout.rt.client.ui.form.fields.composer.entity.AbstractComposerEntity;
import org.eclipse.scout.rt.client.ui.form.fields.composer.node.AttributeNode;
import org.eclipse.scout.rt.client.ui.form.fields.composer.node.EitherOrNode;
import org.eclipse.scout.rt.client.ui.form.fields.composer.node.EntityNode;
import org.eclipse.scout.rt.shared.services.common.jdbc.LegacySearchFilter;

/**
 * @deprecated processing logic belongs to server. Will be removed in the M-Release.
 */
@Deprecated
@SuppressWarnings("deprecation")
public class LegacyComposerStatementBuilder {
  private final Map<String, Object> m_bindMap;
  private Map<String, LegacySearchFilter.ComposerAttributeRef> m_attributeRefMap;
  private long m_bindSeqNo = 0;

  /**
   * @param bindMap
   *          out parameter containing all necessary binds used in the created
   *          statement
   */
  public LegacyComposerStatementBuilder(Map<String, Object> bindMap) {
    m_bindMap = bindMap;
  }

  public LegacySearchFilter.ComposerConstraint build(ITreeNode node) {
    m_attributeRefMap = new HashMap<String, LegacySearchFilter.ComposerAttributeRef>();
    String s = visitAndNodes(node.getChildNodes());
    return new LegacySearchFilter.ComposerConstraint(s, m_attributeRefMap);
  }

  private String visitAndNodes(ITreeNode[] nodes) {
    StringBuilder buf = new StringBuilder();
    int count = 0;
    int i = 0;
    while (i < nodes.length) {
      String s = null;
      if (nodes[i] instanceof EntityNode) {
        s = visitEntityNode((EntityNode) nodes[i]);
        i++;
      }
      else if (nodes[i] instanceof AttributeNode) {
        s = visitAttributeNode((AttributeNode) nodes[i]);
        i++;
      }
      else if (nodes[i] instanceof EitherOrNode) {
        int k = i;
        while (k + 1 < nodes.length && (nodes[k + 1] instanceof EitherOrNode) && !((EitherOrNode) nodes[k + 1]).isBeginOfEitherOr()) {
          k++;
        }
        EitherOrNode[] eNodes = new EitherOrNode[k - i + 1];
        System.arraycopy(nodes, i, eNodes, 0, eNodes.length);
        s = visitOrNodes(eNodes);
        i = k + 1;
      }
      if (s != null) {
        if (count > 0) {
          buf.append(" AND ");
        }
        buf.append(s);
        count++;
      }
    }
    if (count > 0) {
      return buf.toString();
    }
    else {
      return null;
    }
  }

  private String visitOrNodes(EitherOrNode[] nodes) {
    // check if only one condition
    StringBuilder buf = new StringBuilder();
    int count = 0;
    for (EitherOrNode node : nodes) {
      String s = visitAndNodes(node.getChildNodes());
      if (s != null) {
        if (count > 0) {
          buf.append(" OR ");
          if (node.isNegative()) {
            buf.append(" NOT ");
          }
        }
        buf.append("(");
        buf.append(s);
        buf.append(")");
        count++;
      }
    }
    if (count > 0) {
      if (count > 1) {
        buf.insert(0, "(");
        buf.append(")");
        return buf.toString();
      }
      else {
        String s = buf.toString();
        if (s.matches("\\(.*\\)")) {
          return s.substring(1, s.length() - 1).trim();
        }
        else {
          return s;
        }
      }
    }
    else {
      return null;
    }
  }

  private String visitEntityNode(EntityNode node) {
    String stm = ((AbstractComposerEntity) node.getEntity()).getLegacyStatement();
    if (stm == null) {
      stm = "";
    }
    // negation
    if (node.isNegative()) {
      stm = "NOT " + stm;
    }
    // add children
    String s = visitAndNodes(node.getChildNodes());
    if (s != null) {
      s = " AND " + s;
    }
    if (stm.indexOf("<attributes/>") >= 0) {
      stm = StringUtility.replace(stm, "<attributes/>", s);
    }
    else if (stm.indexOf("#W#") >= 0) {
      // legacy
      stm = StringUtility.replace(stm, "#W#", s);
    }
    else if (s != null) {
      stm = stm + s;
    }
    if (stm.length() > 0) {
      return stm;
    }
    else {
      return null;
    }
  }

  private String visitAttributeNode(final AttributeNode node) {
    String originalStatement = ((AbstractComposerAttribute) node.getAttribute()).getLegacyStatement();
    if (originalStatement == null) {
      originalStatement = "";
    }
    // replace the S in the string by the generated unique bind name
    final Map<String, String> bindTranslationTable = localizeBindNames("S");
    String stm = localizeStatement(originalStatement, bindTranslationTable);
    if (!stm.equals(originalStatement)) {
      // the attribute was of the form: P.PNAME = #S# (contains references to S)
      // this is legacy and is not supported for attribute decoration
      m_bindMap.put(bindTranslationTable.get("S"), node.getValues() != null && node.getValues().length > 0 ? node.getValues()[0] : null);
      return stm;
    }
    else {
      // the attribute was of the form: P.PNAME or
      // <attribute>P.PNAME</attribute>
      // make sure there is an attribute tag in the string, if none enclose all
      // by default
      if (stm.indexOf("<attribute>") < 0) {
        stm = "<attribute>" + stm + "</attribute>";
      }
      // tag replacement
      ITagProcessor processor = new ITagProcessor() {
        @Override
        public String processTag(String tagName, String attribute) {
          String key = "${attribute" + getNextBindSeqNo() + "}";
          m_attributeRefMap.put(key, new LegacySearchFilter.ComposerAttributeRef(node.getOp().getOperator(), attribute, bindTranslationTable.get("S"), node.getValues() != null && node.getValues().length > 0 ? node.getValues()[0] : null));
          return key;
        }
      };
      return StringUtility.replaceTags(stm, "attribute", processor);
    }
  }

  private long getNextBindSeqNo() {
    return m_bindSeqNo++;
  }

  /**
   * Replace all bind names by unique bind names so that this statement parts
   * bind names are not conflicting with other parts that use the same statement
   * part. For example S is replaces by __S123.
   */
  private Map<String, String> localizeBindNames(String... bindNames) {
    HashMap<String, String> map = new HashMap<String, String>();
    if (bindNames != null) {
      for (String bindName : bindNames) {
        String locName = "__" + bindName + getNextBindSeqNo();
        map.put(bindName, locName);
      }
    }
    return map;
  }

  /**
   * Replace all bind names by localized bind names
   */
  private String localizeStatement(String stm, Map<String, String> bindTranslationTable) {
    for (Map.Entry<String, String> e : bindTranslationTable.entrySet()) {
      String oldName = e.getKey();
      String newName = e.getValue();
      stm = stm.replaceAll("#" + oldName + "#", "#" + newName + "#");
      stm = stm.replaceAll("\\&" + oldName + "\\&", "&" + newName + "&");
      stm = stm.replaceAll(":" + oldName + "([^A-Za-z0-9])", ":" + newName + "$1");
      stm = stm.replaceAll(":" + oldName + "$", ":" + newName);
    }
    return stm;
  }

}
