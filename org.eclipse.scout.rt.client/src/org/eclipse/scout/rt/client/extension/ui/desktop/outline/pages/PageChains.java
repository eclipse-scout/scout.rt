package org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.basic.tree.ITreeNodeExtension;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTreeNode;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPage;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class PageChains {

  private PageChains() {
  }

  protected abstract static class AbstractPageChain extends AbstractExtensionChain<IPageExtension<? extends AbstractPage>> {

    public AbstractPageChain(List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions) {
      super(extensions, IPageExtension.class);
    }
  }

  public static class PagePageDataLoadedChain extends AbstractPageChain {

    public PagePageDataLoadedChain(List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions) {
      super(extensions);
    }

    public void execPageDataLoaded() throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IPageExtension<? extends AbstractPage> next) throws ProcessingException {
          next.execPageDataLoaded(PagePageDataLoadedChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class PagePageActivatedChain extends AbstractPageChain {

    public PagePageActivatedChain(List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions) {
      super(extensions);
    }

    public void execPageActivated() throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IPageExtension<? extends AbstractPage> next) throws ProcessingException {
          next.execPageActivated(PagePageActivatedChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class PageDataChangedChain extends AbstractPageChain {

    public PageDataChangedChain(List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions) {
      super(extensions);
    }

    public void execDataChanged(final Object... dataTypes) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IPageExtension<? extends AbstractPage> next) throws ProcessingException {
          next.execDataChanged(PageDataChangedChain.this, dataTypes);
        }
      };
      callChain(methodInvocation, dataTypes);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class PageInitPageChain extends AbstractPageChain {

    public PageInitPageChain(List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions) {
      super(extensions);
    }

    public void execInitPage() throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IPageExtension<? extends AbstractPage> next) throws ProcessingException {
          next.execInitPage(PageInitPageChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class PagePageDeactivatedChain extends AbstractPageChain {

    public PagePageDeactivatedChain(List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions) {
      super(extensions);
    }

    public void execPageDeactivated() throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IPageExtension<? extends AbstractPage> next) throws ProcessingException {
          next.execPageDeactivated(PagePageDeactivatedChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class PageDisposePageChain extends AbstractPageChain {

    public PageDisposePageChain(List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions) {
      super(extensions);
    }

    public void execDisposePage() throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IPageExtension<? extends AbstractPage> next) throws ProcessingException {
          next.execDisposePage(PageDisposePageChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }
}
