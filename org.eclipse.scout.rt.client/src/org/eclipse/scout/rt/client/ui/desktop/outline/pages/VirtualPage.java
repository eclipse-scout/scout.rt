/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.desktop.outline.pages;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.basic.tree.IVirtualTreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.VirtualTreeNode;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.form.IForm;

/**
 * For performance optimizations, child pages are only loaded when needed.
 * Until then they are virtualized using VirtualPage objects.
 * <p>
 * A call to {@link IPage#getChildPage(int)}, {@link IPage#getChildPages()} or selecting a virtual page effectively
 * constructs the child page.
 * <p>
 * This construction involves calling
 * {@link AbstractPageWithTable#execCreateChildPage(org.eclipse.scout.rt.client.ui.basic.table.ITableRow)} resp.
 * {@link AbstractPageWithNodes#execCreateChildPages(java.util.Collection)}
 */
public class VirtualPage extends VirtualTreeNode implements IPage, IVirtualTreeNode {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(VirtualPage.class);

  public VirtualPage() {
  }

  public void initPage() throws ProcessingException {
  }

  @SuppressWarnings("deprecation")
  public String getBookmarkIdentifier() {
    return null;
  }

  @Override
  public String getUserPreferenceContext() {
    return null;
  }

  public IOutline getOutline() {
    return (IOutline) getTree();
  }

  public IPage getParentPage() {
    return (IPage) getParentNode();
  }

  public IPage getChildPage(final int childIndex) {
    return null;
  }

  public IPage[] getChildPages() {
    return new IPage[0];
  }

  public void pageActivatedNotify() {
  }

  public void pageDeactivatedNotify() {
  }

  public IForm getDetailForm() {
    return null;
  }

  public void setDetailForm(IForm form) {
  }

  public void dataChanged(Object... dataTypes) {
  }

  public final void reloadPage() throws ProcessingException {
  }

  public boolean isTableVisible() {
    return false;
  }

  public void setTableVisible(boolean b) {
  }

}
