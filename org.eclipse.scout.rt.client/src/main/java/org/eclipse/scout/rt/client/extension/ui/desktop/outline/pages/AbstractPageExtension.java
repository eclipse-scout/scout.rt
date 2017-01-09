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

import org.eclipse.scout.rt.client.extension.ui.basic.tree.AbstractTreeNodeExtension;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PageDataChangedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PageDetailFormActivatedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PageDisposePageChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PageInitDetailFormChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PageInitPageChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PageInitTableChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PagePageActivatedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PagePageDataLoadedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PagePageDeactivatedChain;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPage;

public abstract class AbstractPageExtension<OWNER extends AbstractPage> extends AbstractTreeNodeExtension<OWNER> implements IPageExtension<OWNER> {

  public AbstractPageExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execPageDataLoaded(PagePageDataLoadedChain chain) {
    chain.execPageDataLoaded();
  }

  @Override
  public void execPageActivated(PagePageActivatedChain chain) {
    chain.execPageActivated();
  }

  @Override
  public void execDataChanged(PageDataChangedChain chain, Object... dataTypes) {
    chain.execDataChanged(dataTypes);
  }

  @Override
  public void execInitPage(PageInitPageChain chain) {
    chain.execInitPage();
  }

  @Override
  public void execPageDeactivated(PagePageDeactivatedChain chain) {
    chain.execPageDeactivated();
  }

  @Override
  public void execDisposePage(PageDisposePageChain chain) {
    chain.execDisposePage();
  }

  @Override
  public void execInitDetailForm(PageInitDetailFormChain chain) {
    chain.execInitDetailForm();
  }

  @Override
  public void execInitTable(PageInitTableChain chain) {
    chain.execInitTable();
  }

  @Override
  public void execDetailFormActivated(PageDetailFormActivatedChain chain) {
    chain.execDetailFormActivated();
  }
}
