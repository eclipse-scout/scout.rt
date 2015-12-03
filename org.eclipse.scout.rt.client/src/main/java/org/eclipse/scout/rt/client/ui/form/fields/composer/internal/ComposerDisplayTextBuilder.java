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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.form.fields.composer.node.AttributeNode;
import org.eclipse.scout.rt.client.ui.form.fields.composer.node.EitherOrNode;
import org.eclipse.scout.rt.client.ui.form.fields.composer.node.EntityNode;

public class ComposerDisplayTextBuilder {

  public ComposerDisplayTextBuilder() {
  }

  public void build(ITreeNode node, StringBuilder buf, String prefix) {
    visitAndNodes(node.getChildNodes(), buf, prefix);
  }

  private void visitAndNodes(List<? extends ITreeNode> nodes, StringBuilder buf, String prefix) {
    Iterator<? extends ITreeNode> nodeIt = nodes.iterator();
    ITreeNode node = null;
    boolean skipDoNext = false;
    while (nodeIt.hasNext() || skipDoNext) {
      // to ensure visit first node after an either or...
      if (!skipDoNext) {
        node = nodeIt.next();
      }
      // reset
      skipDoNext = false;

      if (node instanceof EntityNode) {
        visitEntityNode((EntityNode) node, buf, prefix);
      }
      else if (node instanceof AttributeNode) {
        visitAttributeNode((AttributeNode) node, buf, prefix);
      }
      else if (node instanceof EitherOrNode) {
        List<EitherOrNode> eitherOrNodes = new ArrayList<EitherOrNode>();
        eitherOrNodes.add((EitherOrNode) node);
        while (nodeIt.hasNext()) {
          node = nodeIt.next();
          if (node instanceof EitherOrNode) {
            eitherOrNodes.add((EitherOrNode) node);
          }
          else {
            skipDoNext = true;
            break;
          }
        }
        visitOrNodes(eitherOrNodes, buf, prefix);
      }
    }
  }

  private void visitOrNodes(List<? extends EitherOrNode> nodes, StringBuilder buf, String prefix) {
    for (EitherOrNode node : nodes) {
      buf.append(prefix);
      buf.append(node.getCell().getText());
      buf.append("\n");
      // add children
      visitAndNodes(node.getChildNodes(), buf, prefix + " ");
    }
  }

  private void visitEntityNode(EntityNode node, StringBuilder buf, String prefix) {
    buf.append(prefix);
    buf.append(node.getCell().getText());
    buf.append("\n");
    // add children
    visitAndNodes(node.getChildNodes(), buf, prefix + " ");
  }

  private void visitAttributeNode(AttributeNode node, StringBuilder buf, String prefix) {
    buf.append(prefix);
    buf.append(node.getCell().getText());
    buf.append("\n");
    // add children
    visitAndNodes(node.getChildNodes(), buf, prefix + " ");
  }
}
