package org.eclipse.scout.rt.client.extension.ui.form.fields.treefield;

import java.util.Collection;
import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.treefield.AbstractTreeField;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class TreeFieldChains {

  private TreeFieldChains() {
  }

  protected abstract static class AbstractTreeFieldChain extends AbstractExtensionChain<ITreeFieldExtension<? extends AbstractTreeField>> {

    public AbstractTreeFieldChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions, ITreeFieldExtension.class);
    }
  }

  public static class TreeFieldSaveChain extends AbstractTreeFieldChain {

    public TreeFieldSaveChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execSave(final Collection<? extends ITreeNode> insertedNodes, final Collection<? extends ITreeNode> updatedNodes, final Collection<? extends ITreeNode> deletedNodes) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITreeFieldExtension<? extends AbstractTreeField> next) {
          next.execSave(TreeFieldSaveChain.this, insertedNodes, updatedNodes, deletedNodes);
        }
      };
      callChain(methodInvocation, insertedNodes, updatedNodes, deletedNodes);
    }
  }

  public static class TreeFieldSaveDeletedNodeChain extends AbstractTreeFieldChain {

    public TreeFieldSaveDeletedNodeChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execSaveDeletedNode(final ITreeNode row) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITreeFieldExtension<? extends AbstractTreeField> next) throws ProcessingException {
          next.execSaveDeletedNode(TreeFieldSaveDeletedNodeChain.this, row);
        }
      };
      callChain(methodInvocation, row);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class TreeFieldSaveUpdatedNodeChain extends AbstractTreeFieldChain {

    public TreeFieldSaveUpdatedNodeChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execSaveUpdatedNode(final ITreeNode row) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITreeFieldExtension<? extends AbstractTreeField> next) throws ProcessingException {
          next.execSaveUpdatedNode(TreeFieldSaveUpdatedNodeChain.this, row);
        }
      };
      callChain(methodInvocation, row);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class TreeFieldLoadChildNodesChain extends AbstractTreeFieldChain {

    public TreeFieldLoadChildNodesChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execLoadChildNodes(final ITreeNode parentNode) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITreeFieldExtension<? extends AbstractTreeField> next) throws ProcessingException {
          next.execLoadChildNodes(TreeFieldLoadChildNodesChain.this, parentNode);
        }
      };
      callChain(methodInvocation, parentNode);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class TreeFieldSaveInsertedNodeChain extends AbstractTreeFieldChain {

    public TreeFieldSaveInsertedNodeChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execSaveInsertedNode(final ITreeNode row) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITreeFieldExtension<? extends AbstractTreeField> next) throws ProcessingException {
          next.execSaveInsertedNode(TreeFieldSaveInsertedNodeChain.this, row);
        }
      };
      callChain(methodInvocation, row);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }
}
