package org.eclipse.scout.rt.client.extension.ui.basic.tree;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.IVirtualTreeNode;
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
      callChain(methodInvocation, cell);
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

  public static class TreeNodeResolveVirtualChildNodeChain extends AbstractTreeNodeChain {

    public TreeNodeResolveVirtualChildNodeChain(List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions) {
      super(extensions);
    }

    public ITreeNode execResolveVirtualChildNode(final IVirtualTreeNode node) throws ProcessingException {
      MethodInvocation<ITreeNode> methodInvocation = new MethodInvocation<ITreeNode>() {
        @Override
        protected void callMethod(ITreeNodeExtension<? extends AbstractTreeNode> next) throws ProcessingException {
          setReturnValue(next.execResolveVirtualChildNode(TreeNodeResolveVirtualChildNodeChain.this, node));
        }
      };
      callChain(methodInvocation, node);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }
      return methodInvocation.getReturnValue();
    }
  }
}
