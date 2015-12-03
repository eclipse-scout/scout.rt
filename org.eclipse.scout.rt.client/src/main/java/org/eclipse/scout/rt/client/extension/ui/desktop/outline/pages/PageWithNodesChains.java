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
package org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages;

import java.util.List;

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

    public void execCreateChildPages(final List<IPage<?>> pageList) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IPageWithNodesExtension<? extends AbstractPageWithNodes> next) {
          next.execCreateChildPages(PageWithNodesCreateChildPagesChain.this, pageList);
        }
      };
      callChain(methodInvocation, pageList);
    }
  }
}
