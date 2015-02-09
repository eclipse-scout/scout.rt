package org.eclipse.scout.rt.client.extension.ui.form.fields.treebox;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBox;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;

public final class TreeBoxChains {

  private TreeBoxChains() {
  }

  protected abstract static class AbstractTreeBoxChain<T> extends AbstractExtensionChain<ITreeBoxExtension<T, ? extends AbstractTreeBox<T>>> {

    public AbstractTreeBoxChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions, ITreeBoxExtension.class);
    }
  }

  public static class TreeBoxFilterNewNodeChain<T> extends AbstractTreeBoxChain<T> {

    public TreeBoxFilterNewNodeChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
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

    public TreeBoxLoadChildNodesChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
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

    public TreeBoxPrepareLookupChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
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

    public TreeBoxFilterLookupResultChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
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

  public static class TreeBoxAutoCheckChildNodesChain<T> extends AbstractTreeBoxChain<T> {

    public TreeBoxAutoCheckChildNodesChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execAutoCheckChildNodes(final ITreeNode node, final boolean value) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITreeBoxExtension<T, ? extends AbstractTreeBox<T>> next) {
          next.execAutoCheckChildNodes(TreeBoxAutoCheckChildNodesChain.this, node, value);
        }
      };
      callChain(methodInvocation, node, value);
    }
  }
}
