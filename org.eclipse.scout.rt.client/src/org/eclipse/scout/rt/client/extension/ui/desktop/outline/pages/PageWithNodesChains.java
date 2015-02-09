package org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.basic.tree.ITreeNodeExtension;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTreeNode;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithNodes;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class PageWithNodesChains {

  private PageWithNodesChains() {
  }

  protected abstract static class AbstractPageWithNodesChain extends AbstractExtensionChain<IPageWithNodesExtension<? extends AbstractPageWithNodes>> {

    public AbstractPageWithNodesChain(List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions) {
      super(extensions, IPageWithNodesExtension.class);
    }
  }

  public static class PageWithNodesCreateChildPagesChain extends AbstractPageWithNodesChain {

    public PageWithNodesCreateChildPagesChain(List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions) {
      super(extensions);
    }

    public void execCreateChildPages(final List<IPage> pageList) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IPageWithNodesExtension<? extends AbstractPageWithNodes> next) throws ProcessingException {
          next.execCreateChildPages(PageWithNodesCreateChildPagesChain.this, pageList);
        }
      };
      callChain(methodInvocation, pageList);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }
}
