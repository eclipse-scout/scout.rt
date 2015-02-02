package org.eclipse.scout.rt.client.extension.ui.form.fields.treebox;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBox;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;

public final class TreeBoxChains {

  private TreeBoxChains() {
  }

  protected abstract static class AbstractTreeBoxChain<T> extends AbstractExtensionChain<ITreeBoxExtension<T, ? extends AbstractTreeBox<T>>> {

    public AbstractTreeBoxChain(List<? extends ITreeBoxExtension<T, ? extends AbstractTreeBox<T>>> extensions) {
      super(extensions);
    }
  }

  public static class TreeBoxFilterNewNodeChain<T> extends AbstractTreeBoxChain<T> {

    public TreeBoxFilterNewNodeChain(List<? extends ITreeBoxExtension<T, ? extends AbstractTreeBox<T>>> extensions) {
      super(extensions);
    }

    public void execFilterNewNode(final ITreeNode newNode, final int treeLevel) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITreeBoxExtension<T, ? extends AbstractTreeBox<T>> next) throws ProcessingException {
          next.execFilterNewNode(TreeBoxFilterNewNodeChain.this, newNode, treeLevel);
        }
      };
      callChain(methodInvocation, newNode, treeLevel);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class TreeBoxLoadChildNodesChain<T> extends AbstractTreeBoxChain<T> {

    public TreeBoxLoadChildNodesChain(List<? extends ITreeBoxExtension<T, ? extends AbstractTreeBox<T>>> extensions) {
      super(extensions);
    }

    public void execLoadChildNodes(final ITreeNode parentNode) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITreeBoxExtension<T, ? extends AbstractTreeBox<T>> next) throws ProcessingException {
          next.execLoadChildNodes(TreeBoxLoadChildNodesChain.this, parentNode);
        }
      };
      callChain(methodInvocation, parentNode);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class TreeBoxPrepareLookupChain<T> extends AbstractTreeBoxChain<T> {

    public TreeBoxPrepareLookupChain(List<? extends ITreeBoxExtension<T, ? extends AbstractTreeBox<T>>> extensions) {
      super(extensions);
    }

    public void execPrepareLookup(final ILookupCall<T> call, final ITreeNode parent) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITreeBoxExtension<T, ? extends AbstractTreeBox<T>> next) throws ProcessingException {
          next.execPrepareLookup(TreeBoxPrepareLookupChain.this, call, parent);
        }
      };
      callChain(methodInvocation, call, parent);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class TreeBoxFilterLookupResultChain<T> extends AbstractTreeBoxChain<T> {

    public TreeBoxFilterLookupResultChain(List<? extends ITreeBoxExtension<T, ? extends AbstractTreeBox<T>>> extensions) {
      super(extensions);
    }

    public void execFilterLookupResult(final ILookupCall<T> call, final List<ILookupRow<T>> result) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITreeBoxExtension<T, ? extends AbstractTreeBox<T>> next) throws ProcessingException {
          next.execFilterLookupResult(TreeBoxFilterLookupResultChain.this, call, result);
        }
      };
      callChain(methodInvocation, call, result);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

}
