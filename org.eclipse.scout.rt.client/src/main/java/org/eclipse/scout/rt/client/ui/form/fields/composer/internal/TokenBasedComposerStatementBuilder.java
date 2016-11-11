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
package org.eclipse.scout.rt.client.ui.form.fields.composer.internal;

import java.util.List;

import org.eclipse.scout.rt.client.services.common.search.TokenBasedSearchFilterService;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.form.fields.composer.node.AttributeNode;
import org.eclipse.scout.rt.client.ui.form.fields.composer.node.EitherOrNode;
import org.eclipse.scout.rt.client.ui.form.fields.composer.node.EntityNode;
import org.eclipse.scout.rt.shared.services.common.jdbc.TokenBasedSearchFilter.AndNodeToken;
import org.eclipse.scout.rt.shared.services.common.jdbc.TokenBasedSearchFilter.AttributeNodeToken;
import org.eclipse.scout.rt.shared.services.common.jdbc.TokenBasedSearchFilter.EntityNodeToken;
import org.eclipse.scout.rt.shared.services.common.jdbc.TokenBasedSearchFilter.OrNodeToken;
import org.eclipse.scout.rt.shared.services.common.jdbc.TokenBasedSearchFilter.TreeNodeToken;

/**
 * @deprecated Will be removed in the O release.
 */
@SuppressWarnings("deprecation")
@Deprecated
public class TokenBasedComposerStatementBuilder {

  private final TokenBasedSearchFilterService m_service;

  public TokenBasedComposerStatementBuilder(TokenBasedSearchFilterService service) {
    m_service = service;
  }

  public AndNodeToken build(ITreeNode node) {
    AndNodeToken rootTok = new AndNodeToken();
    visitAndNodes(rootTok, node.getChildNodes());
    return rootTok;
  }

  private void visitAndNodes(TreeNodeToken parentTok, List<? extends ITreeNode> nodesList) {
    ITreeNode[] nodes = nodesList.toArray(new ITreeNode[nodesList.size()]);
    int i = 0;
    while (i < nodes.length) {
      if (nodes[i] instanceof EntityNode) {
        EntityNodeToken eTok = visitEntityNode((EntityNode) nodes[i]);
        if (eTok != null) {
          parentTok.addChild(eTok);
        }
        i++;
      }
      else if (nodes[i] instanceof AttributeNode) {
        AttributeNodeToken aTok = visitAttributeNode((AttributeNode) nodes[i]);
        if (aTok != null) {
          parentTok.addChild(aTok);
        }
        i++;
      }
      else if (nodes[i] instanceof EitherOrNode) {
        int k = i;
        while (k + 1 < nodes.length && (nodes[k + 1] instanceof EitherOrNode) && !((EitherOrNode) nodes[k + 1]).isBeginOfEitherOr()) {
          k++;
        }
        EitherOrNode[] eNodes = new EitherOrNode[k - i + 1];
        System.arraycopy(nodes, i, eNodes, 0, eNodes.length);
        OrNodeToken orTok = new OrNodeToken();
        visitOrNodes(orTok, eNodes);
        if (orTok.getChildren().size() >= 2) {
          parentTok.addChild(orTok);
        }
        else if (orTok.getChildren().size() == 1) {
          parentTok.addChild(orTok.getChildren().get(0));
        }
        i = k + 1;
      }
    }
  }

  private void visitOrNodes(TreeNodeToken parentTok, EitherOrNode[] nodes) {
    // check if only one condition
    for (EitherOrNode node : nodes) {
      AndNodeToken andTok = new AndNodeToken();
      visitAndNodes(andTok, node.getChildNodes());
      if (andTok.getChildren().size() >= 2) {
        andTok.setNegative(node.isNegative());
        parentTok.addChild(andTok);
      }
      else if (andTok.getChildren().size() == 1) {
        TreeNodeToken firstTok = andTok.getChildren().get(0);
        firstTok.setNegative(node.isNegative());
        parentTok.addChild(firstTok);
      }
    }
  }

  private EntityNodeToken visitEntityNode(EntityNode node) {
    Integer tokenId = m_service.resolveTokenIdByClass(node.getEntity().getClass());
    if (tokenId == null) {
      return null;
    }
    EntityNodeToken eTok = new EntityNodeToken(tokenId);
    eTok.setNegative(node.isNegative());
    // add children
    AndNodeToken andTok = new AndNodeToken();
    visitAndNodes(andTok, node.getChildNodes());
    if (andTok.getChildren().size() > 0) {
      eTok.addChild(andTok);
    }
    return eTok;
  }

  private AttributeNodeToken visitAttributeNode(AttributeNode node) {
    Integer tokenId = m_service.resolveTokenIdByClass(node.getAttribute().getClass());
    if (tokenId == null) {
      return null;
    }
    AttributeNodeToken aTok = new AttributeNodeToken(tokenId, node.getOp().getOperator(), node.getValues());
    return aTok;
  }

}
