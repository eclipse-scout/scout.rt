package org.eclipse.scout.rt.client.extension.ui.basic.tree;

import java.net.URL;
import java.util.Collection;
import java.util.List;

import org.eclipse.scout.commons.dnd.TransferObject;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.MouseButton;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;
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

    public void execDrop(final ITreeNode node, final TransferObject t) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITreeExtension<? extends AbstractTree> next) throws ProcessingException {
          next.execDrop(TreeDropChain.this, node, t);
        }
      };
      callChain(methodInvocation, node, t);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class TreeInitTreeChain extends AbstractTreeChain {

    public TreeInitTreeChain(List<? extends ITreeExtension<? extends AbstractTree>> extensions) {
      super(extensions);
    }

    public void execInitTree() throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITreeExtension<? extends AbstractTree> next) throws ProcessingException {
          next.execInitTree(TreeInitTreeChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class TreeDropTargetChangedChain extends AbstractTreeChain {

    public TreeDropTargetChangedChain(List<? extends ITreeExtension<? extends AbstractTree>> extensions) {
      super(extensions);
    }

    public void execDropTargetChanged(final ITreeNode node) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITreeExtension<? extends AbstractTree> next) throws ProcessingException {
          next.execDropTargetChanged(TreeDropTargetChangedChain.this, node);
        }
      };
      callChain(methodInvocation, node);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class TreeDragNodesChain extends AbstractTreeChain {

    public TreeDragNodesChain(List<? extends ITreeExtension<? extends AbstractTree>> extensions) {
      super(extensions);
    }

    public TransferObject execDrag(final Collection<ITreeNode> nodes) throws ProcessingException {
      MethodInvocation<TransferObject> methodInvocation = new MethodInvocation<TransferObject>() {
        @Override
        protected void callMethod(ITreeExtension<? extends AbstractTree> next) throws ProcessingException {
          setReturnValue(next.execDrag(TreeDragNodesChain.this, nodes));
        }
      };
      callChain(methodInvocation, nodes);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }
      return methodInvocation.getReturnValue();
    }
  }

  public static class TreeNodeActionChain extends AbstractTreeChain {

    public TreeNodeActionChain(List<? extends ITreeExtension<? extends AbstractTree>> extensions) {
      super(extensions);
    }

    public void execNodeAction(final ITreeNode node) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITreeExtension<? extends AbstractTree> next) throws ProcessingException {
          next.execNodeAction(TreeNodeActionChain.this, node);
        }
      };
      callChain(methodInvocation, node);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class TreeNodeClickChain extends AbstractTreeChain {

    public TreeNodeClickChain(List<? extends ITreeExtension<? extends AbstractTree>> extensions) {
      super(extensions);
    }

    public void execNodeClick(final ITreeNode node, final MouseButton mouseButton) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITreeExtension<? extends AbstractTree> next) throws ProcessingException {
          next.execNodeClick(TreeNodeClickChain.this, node, mouseButton);
        }
      };
      callChain(methodInvocation, node, mouseButton);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class TreeHyperlinkActionChain extends AbstractTreeChain {

    public TreeHyperlinkActionChain(List<? extends ITreeExtension<? extends AbstractTree>> extensions) {
      super(extensions);
    }

    public void execHyperlinkAction(final URL url, final String path, final boolean local) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITreeExtension<? extends AbstractTree> next) throws ProcessingException {
          next.execHyperlinkAction(TreeHyperlinkActionChain.this, url, path, local);
        }
      };
      callChain(methodInvocation, url, path, local);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class TreeNodesSelectedChain extends AbstractTreeChain {

    public TreeNodesSelectedChain(List<? extends ITreeExtension<? extends AbstractTree>> extensions) {
      super(extensions);
    }

    public void execNodesSelected(final TreeEvent e) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITreeExtension<? extends AbstractTree> next) throws ProcessingException {
          next.execNodesSelected(TreeNodesSelectedChain.this, e);
        }
      };
      callChain(methodInvocation, e);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class TreeDisposeTreeChain extends AbstractTreeChain {

    public TreeDisposeTreeChain(List<? extends ITreeExtension<? extends AbstractTree>> extensions) {
      super(extensions);
    }

    public void execDisposeTree() throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITreeExtension<? extends AbstractTree> next) throws ProcessingException {
          next.execDisposeTree(TreeDisposeTreeChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class TreeDecorateCellChain extends AbstractTreeChain {

    public TreeDecorateCellChain(List<? extends ITreeExtension<? extends AbstractTree>> extensions) {
      super(extensions);
    }

    public void execDecorateCell(final ITreeNode node, final Cell cell) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITreeExtension<? extends AbstractTree> next) throws ProcessingException {
          next.execDecorateCell(TreeDecorateCellChain.this, node, cell);
        }
      };
      callChain(methodInvocation, node, cell);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class TreeDragNodeChain extends AbstractTreeChain {

    public TreeDragNodeChain(List<? extends ITreeExtension<? extends AbstractTree>> extensions) {
      super(extensions);
    }

    public TransferObject execDrag(final ITreeNode node) throws ProcessingException {
      MethodInvocation<TransferObject> methodInvocation = new MethodInvocation<TransferObject>() {
        @Override
        protected void callMethod(ITreeExtension<? extends AbstractTree> next) throws ProcessingException {
          setReturnValue(next.execDrag(TreeDragNodeChain.this, node));
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
