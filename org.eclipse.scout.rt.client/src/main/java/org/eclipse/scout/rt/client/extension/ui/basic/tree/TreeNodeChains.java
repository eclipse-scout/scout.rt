/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.basic.tree;

import java.util.List;

import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTreeNode;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class TreeNodeChains {

  private TreeNodeChains() {
  }

  protected abstract static class AbstractTreeNodeChain extends AbstractExtensionChain<ITreeNodeExtension<? extends AbstractTreeNode>> {

    public AbstractTreeNodeChain(List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions) {
      super(extensions, ITreeNodeExtension.class);
    }
  }

  public static class TreeNodeDecorateCellChain extends AbstractTreeNodeChain {

    public TreeNodeDecorateCellChain(List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions) {
      super(extensions);
    }

    public void execDecorateCell(final Cell cell) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITreeNodeExtension<? extends AbstractTreeNode> next) {
          next.execDecorateCell(TreeNodeDecorateCellChain.this, cell);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class TreeNodeInitTreeNodeChain extends AbstractTreeNodeChain {

    public TreeNodeInitTreeNodeChain(List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions) {
      super(extensions);
    }

    public void execInitTreeNode() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITreeNodeExtension<? extends AbstractTreeNode> next) {
          next.execInitTreeNode(TreeNodeInitTreeNodeChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class TreeNodeDisposeChain extends AbstractTreeNodeChain {

    public TreeNodeDisposeChain(List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions) {
      super(extensions);
    }

    public void execDispose() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITreeNodeExtension<? extends AbstractTreeNode> next) {
          next.execDispose(TreeNodeDisposeChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }
}
