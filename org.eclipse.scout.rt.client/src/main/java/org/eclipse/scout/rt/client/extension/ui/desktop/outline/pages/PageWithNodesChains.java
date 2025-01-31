/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages;

import java.util.List;

import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithNodes;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class PageWithNodesChains {

  private PageWithNodesChains() {
  }

  protected abstract static class AbstractPageWithNodesChain extends AbstractExtensionChain<IPageWithNodesExtension<? extends AbstractPageWithNodes>> {

    public AbstractPageWithNodesChain(List<? extends IPageExtension<? extends AbstractPage>> extensions) {
      super(extensions, IPageWithNodesExtension.class);
    }
  }

  public static class PageWithNodesCreateChildPagesChain extends AbstractPageWithNodesChain {

    public PageWithNodesCreateChildPagesChain(List<? extends IPageExtension<? extends AbstractPage>> extensions) {
      super(extensions);
    }

    public void execCreateChildPages(final List<IPage<?>> pageList) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IPageWithNodesExtension<? extends AbstractPageWithNodes> next) {
          next.execCreateChildPages(PageWithNodesCreateChildPagesChain.this, pageList);
        }
      };
      callChain(methodInvocation);
    }
  }
}
