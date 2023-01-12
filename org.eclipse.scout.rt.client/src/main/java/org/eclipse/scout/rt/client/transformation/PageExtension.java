/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.transformation;

import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.AbstractPageExtension;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PageInitPageChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PageInitTableChain;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPage;
import org.eclipse.scout.rt.platform.BEANS;

public class PageExtension extends AbstractPageExtension<AbstractPage> {

  public PageExtension(AbstractPage owner) {
    super(owner);
  }

  @Override
  public void execInitPage(PageInitPageChain chain) {
    super.execInitPage(chain);
    BEANS.get(IDeviceTransformationService.class).getDeviceTransformer().transformPage(getOwner());
  }

  @Override
  public void execInitTable(PageInitTableChain chain) {
    super.execInitTable(chain);
    BEANS.get(IDeviceTransformationService.class).getDeviceTransformer().transformPageTable(getOwner().getTable(), getOwner());
  }
}
