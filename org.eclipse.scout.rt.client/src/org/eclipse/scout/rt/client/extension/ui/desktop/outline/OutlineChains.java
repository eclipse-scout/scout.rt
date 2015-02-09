package org.eclipse.scout.rt.client.extension.ui.desktop.outline;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.basic.tree.ITreeExtension;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTree;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class OutlineChains {

  private OutlineChains() {
  }

  protected abstract static class AbstractOutlineChain extends AbstractExtensionChain<IOutlineExtension<? extends AbstractOutline>> {

    public AbstractOutlineChain(List<? extends ITreeExtension<? extends AbstractTree>> extensions) {
      super(extensions, IOutlineExtension.class);
    }
  }

  public static class OutlineCreateChildPagesChain extends AbstractOutlineChain {

    public OutlineCreateChildPagesChain(List<? extends ITreeExtension<? extends AbstractTree>> extensions) {
      super(extensions);
    }

    public void execCreateChildPages(final List<IPage> pageList) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IOutlineExtension<? extends AbstractOutline> next) throws ProcessingException {
          next.execCreateChildPages(OutlineCreateChildPagesChain.this, pageList);
        }
      };
      callChain(methodInvocation, pageList);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }
}
