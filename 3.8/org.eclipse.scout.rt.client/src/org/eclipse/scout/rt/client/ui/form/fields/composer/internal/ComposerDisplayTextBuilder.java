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

import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.form.fields.composer.node.AttributeNode;
import org.eclipse.scout.rt.client.ui.form.fields.composer.node.EitherOrNode;
import org.eclipse.scout.rt.client.ui.form.fields.composer.node.EntityNode;

public class ComposerDisplayTextBuilder {

  public ComposerDisplayTextBuilder() {
  }

  public void build(ITreeNode node, StringBuffer buf, String prefix) {
    visitAndNodes(node.getChildNodes(), buf, prefix);
  }

  private void visitAndNodes(ITreeNode[] nodes, StringBuffer buf, String prefix) {
    int i = 0;
    while (i < nodes.length) {
      if (nodes[i] instanceof EntityNode) {
        visitEntityNode((EntityNode) nodes[i], buf, prefix);
        i++;
      }
      else if (nodes[i] instanceof AttributeNode) {
        visitAttributeNode((AttributeNode) nodes[i], buf, prefix);
        i++;
      }
      else if (nodes[i] instanceof EitherOrNode) {
        int k = i;
        while (k + 1 < nodes.length && (nodes[k + 1] instanceof EitherOrNode) && !((EitherOrNode) nodes[k + 1]).isBeginOfEitherOr()) {
          k++;
        }
        EitherOrNode[] eNodes = new EitherOrNode[k - i + 1];
        System.arraycopy(nodes, i, eNodes, 0, eNodes.length);
        visitOrNodes(eNodes, buf, prefix);
        i = k + 1;
      }
    }
  }

  private void visitOrNodes(EitherOrNode[] nodes, StringBuffer buf, String prefix) {
    for (EitherOrNode node : nodes) {
      buf.append(prefix);
      buf.append(node.getCell().getText());
      buf.append("\n");
      // add children
      visitAndNodes(node.getChildNodes(), buf, prefix + " ");
    }
  }

  private void visitEntityNode(EntityNode node, StringBuffer buf, String prefix) {
    buf.append(prefix);
    buf.append(node.getCell().getText());
    buf.append("\n");
    // add children
    visitAndNodes(node.getChildNodes(), buf, prefix + " ");
  }

  private void visitAttributeNode(AttributeNode node, StringBuffer buf, String prefix) {
    buf.append(prefix);
    buf.append(node.getCell().getText());
    buf.append("\n");
    // add children
    visitAndNodes(node.getChildNodes(), buf, prefix + " ");
  }
}
