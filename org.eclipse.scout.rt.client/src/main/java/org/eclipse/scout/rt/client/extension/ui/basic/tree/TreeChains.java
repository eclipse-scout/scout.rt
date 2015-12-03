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
package org.eclipse.scout.rt.client.extension.ui.basic.tree;

import java.util.Collection;
import java.util.List;

import org.eclipse.scout.rt.client.ui.MouseButton;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;
import org.eclipse.scout.rt.client.ui.dnd.TransferObject;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class TreeChains {

  private TreeChains() {
  }

  protected abstract static class AbstractTreeChain extends AbstractExtensionChain<ITreeExtension<? extends AbstractTree>> {

    public AbstractTreeChain(List<? extends ITreeExtension<? extends AbstractTree>> extensions) {
      super(extensions, ITreeExtension.class);
    }
  }

  public static class TreeDropChain extends AbstractTreeChain {

    public TreeDropChain(List<? extends ITreeExtension<? extends AbstractTree>> extensions) {
      super(extensions);
    }

    public void execDrop(final ITreeNode node, final TransferObject t) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITreeExtension<? extends AbstractTree> next) {
          next.execDrop(TreeDropChain.this, node, t);
        }
      };
      callChain(methodInvocation, node, t);
    }
  }

  public static class TreeInitTreeChain extends AbstractTreeChain {

    public TreeInitTreeChain(List<? extends ITreeExtension<? extends AbstractTree>> extensions) {
      super(extensions);
    }

    public void execInitTree() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITreeExtension<? extends AbstractTree> next) {
          next.execInitTree(TreeInitTreeChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class TreeDropTargetChangedChain extends AbstractTreeChain {

    public TreeDropTargetChangedChain(List<? extends ITreeExtension<? extends AbstractTree>> extensions) {
      super(extensions);
    }

    public void execDropTargetChanged(final ITreeNode node) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITreeExtension<? extends AbstractTree> next) {
          next.execDropTargetChanged(TreeDropTargetChangedChain.this, node);
        }
      };
      callChain(methodInvocation, node);
    }
  }

  public static class TreeDragNodesChain extends AbstractTreeChain {

    public TreeDragNodesChain(List<? extends ITreeExtension<? extends AbstractTree>> extensions) {
      super(extensions);
    }

    public TransferObject execDrag(final Collection<ITreeNode> nodes) {
      MethodInvocation<TransferObject> methodInvocation = new MethodInvocation<TransferObject>() {
        @Override
        protected void callMethod(ITreeExtension<? extends AbstractTree> next) {
          setReturnValue(next.execDrag(TreeDragNodesChain.this, nodes));
        }
      };
      callChain(methodInvocation, nodes);
      return methodInvocation.getReturnValue();
    }
  }

  public static class TreeNodeActionChain extends AbstractTreeChain {

    public TreeNodeActionChain(List<? extends ITreeExtension<? extends AbstractTree>> extensions) {
      super(extensions);
    }

    public void execNodeAction(final ITreeNode node) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITreeExtension<? extends AbstractTree> next) {
          next.execNodeAction(TreeNodeActionChain.this, node);
        }
      };
      callChain(methodInvocation, node);
    }
  }

  public static class TreeNodeClickChain extends AbstractTreeChain {

    public TreeNodeClickChain(List<? extends ITreeExtension<? extends AbstractTree>> extensions) {
      super(extensions);
    }

    public void execNodeClick(final ITreeNode node, final MouseButton mouseButton) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITreeExtension<? extends AbstractTree> next) {
          next.execNodeClick(TreeNodeClickChain.this, node, mouseButton);
        }
      };
      callChain(methodInvocation, node, mouseButton);
    }
  }

  public static class TreeNodesCheckedChain extends AbstractTreeChain {

    public TreeNodesCheckedChain(List<? extends ITreeExtension<? extends AbstractTree>> extensions) {
      super(extensions);
    }

    public void execNodesChecked(final List<ITreeNode> nodes) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITreeExtension<? extends AbstractTree> next) {
          next.execNodesChecked(TreeNodesCheckedChain.this, nodes);
        }
      };
      callChain(methodInvocation, nodes);
    }
  }

  public static class TreeAutoCheckChildNodesChain extends AbstractTreeChain {

    public TreeAutoCheckChildNodesChain(List<? extends ITreeExtension<? extends AbstractTree>> extensions) {
      super(extensions);
    }

    public void execAutoCheckChildNodes(final List<? extends ITreeNode> nodes) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITreeExtension<? extends AbstractTree> next) {
          next.execAutoCheckChildNodes(TreeAutoCheckChildNodesChain.this, nodes);
        }
      };
      callChain(methodInvocation, nodes);
    }
  }

  public static class TreeHyperlinkActionChain extends AbstractTreeChain {

    public TreeHyperlinkActionChain(List<? extends ITreeExtension<? extends AbstractTree>> extensions) {
      super(extensions);
    }

    public void execHyperlinkAction(final String ref) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITreeExtension<? extends AbstractTree> next) {
          next.execAppLinkAction(TreeHyperlinkActionChain.this, ref);
        }
      };
      callChain(methodInvocation, ref);
    }
  }

  public static class TreeNodesSelectedChain extends AbstractTreeChain {

    public TreeNodesSelectedChain(List<? extends ITreeExtension<? extends AbstractTree>> extensions) {
      super(extensions);
    }

    public void execNodesSelected(final TreeEvent e) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITreeExtension<? extends AbstractTree> next) {
          next.execNodesSelected(TreeNodesSelectedChain.this, e);
        }
      };
      callChain(methodInvocation, e);
    }
  }

  public static class TreeDisposeTreeChain extends AbstractTreeChain {

    public TreeDisposeTreeChain(List<? extends ITreeExtension<? extends AbstractTree>> extensions) {
      super(extensions);
    }

    public void execDisposeTree() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITreeExtension<? extends AbstractTree> next) {
          next.execDisposeTree(TreeDisposeTreeChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class TreeDecorateCellChain extends AbstractTreeChain {

    public TreeDecorateCellChain(List<? extends ITreeExtension<? extends AbstractTree>> extensions) {
      super(extensions);
    }

    public void execDecorateCell(final ITreeNode node, final Cell cell) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITreeExtension<? extends AbstractTree> next) {
          next.execDecorateCell(TreeDecorateCellChain.this, node, cell);
        }
      };
      callChain(methodInvocation, node, cell);
    }
  }

  public static class TreeDragNodeChain extends AbstractTreeChain {

    public TreeDragNodeChain(List<? extends ITreeExtension<? extends AbstractTree>> extensions) {
      super(extensions);
    }

    public TransferObject execDrag(final ITreeNode node) {
      MethodInvocation<TransferObject> methodInvocation = new MethodInvocation<TransferObject>() {
        @Override
        protected void callMethod(ITreeExtension<? extends AbstractTree> next) {
          setReturnValue(next.execDrag(TreeDragNodeChain.this, node));
        }
      };
      callChain(methodInvocation, node);
      return methodInvocation.getReturnValue();
    }
  }
}
