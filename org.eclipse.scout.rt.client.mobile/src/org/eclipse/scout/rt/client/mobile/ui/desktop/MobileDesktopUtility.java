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
package org.eclipse.scout.rt.client.mobile.ui.desktop;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ClientJob;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.ui.action.tool.IToolButton;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractFormToolButton;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutlineTableForm;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithNodes;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.GridData;

/**
 * @since 3.9.0
 */
public class MobileDesktopUtility {

  public static void activateOutline(IOutline outline) {
    IDesktop desktop = ClientJob.getCurrentSession().getDesktop();
    desktop.setOutlineTableFormVisible(true);

    if (desktop.getOutline() != outline) {
      desktop.setOutline(outline);
    }

    if (!outline.isRootNodeVisible()) {
      throw new IllegalStateException("Root node must be visible for this drill down approach.");
    }

    if (outline.getSelectedNode() == null) {
      outline.selectNode(outline.getRootPage());
    }
  }

  public static boolean isToolForm(IForm form) {
    if (getToolButtonFor(form) != null) {
      return true;
    }

    return false;
  }

  public static IToolButton getToolButtonFor(IForm form) {
    for (IToolButton toolButton : getDesktop().getToolButtons()) {
      if (toolButton instanceof AbstractFormToolButton) {
        IForm toolForm = ((AbstractFormToolButton<?>) toolButton).getForm();
        if (form == toolForm) {
          return toolButton;
        }
      }
    }

    return null;
  }

  public static void openToolForm(IForm form) {
    IToolButton toolButton = getToolButtonFor(form);
    if (toolButton != null) {
      toolButton.setSelected(true);
    }
    //Double check to make sure it really will be added
    if (!getDesktop().isShowing(form)) {
      getDesktop().addForm(form);
    }
  }

  public static void closeToolForm(IForm form) {
    IToolButton toolButton = getToolButtonFor(form);
    if (toolButton != null) {
      toolButton.setSelected(false);
    }
    //Double check to make sure it really will be removed
    if (getDesktop().isShowing(form)) {
      getDesktop().removeForm(form);
    }
  }

  public static void closeAllToolForms() {
    for (IToolButton toolButton : getDesktop().getToolButtons()) {
      if (toolButton.isVisible()) {
        toolButton.setSelected(false);
      }
    }
  }

  public static void closeOpenForms() throws ProcessingException {
    final IForm[] viewStack = getDesktop().getViewStack();
    for (IForm form : viewStack) {
      closeForm(form);
    }
  }

  public static void closeForm(IForm form) throws ProcessingException {
    if (form == null) {
      return;
    }

    if (MobileDesktopUtility.isToolForm(form)) {
      MobileDesktopUtility.closeToolForm(form);
    }
    else if (form.isAutoAddRemoveOnDesktop()) {
      form.doClose();
    }
    else {
      removeFormFromDesktop(form);
    }
  }

  public static void removeFormFromDesktop(IForm form) {
    if (form instanceof IOutlineTableForm) {
      getDesktop().setOutlineTableFormVisible(false);
    }
    else {
      getDesktop().removeForm(form);
    }
  }

  public static void removeFormsFromDesktop(Class<? extends IForm> formClass, String displayViewId, IForm excludedForm) {
    if (displayViewId == null) {
      return;
    }

    IForm[] viewStack = getDesktop().getViewStack();
    for (IForm form : viewStack) {
      if (form != excludedForm && formClass.isInstance(form) && displayViewId.equals(form.getDisplayViewId())) {
        getDesktop().removeForm(form);
      }
    }
  }

  public static void addFormToDesktop(IForm form) {
    if (form instanceof IOutlineTableForm) {
      //Make sure the outline table form is linked with the desktop
      getDesktop().setOutlineTableForm((IOutlineTableForm) form);

      getDesktop().setOutlineTableFormVisible(true);
    }
    else if (isToolForm(form)) {
      openToolForm(form);
    }
    else {
      getDesktop().addForm(form);
    }
  }

  private static IDesktop getDesktop() {
    return ClientSyncJob.getCurrentSession().getDesktop();
  }

  public static boolean isAnyViewVisible(String displayViewId) {
    if (displayViewId == null) {
      return false;
    }

    IForm[] viewStack = getDesktop().getViewStack();
    for (IForm form : viewStack) {
      if (displayViewId.equals(form.getDisplayViewId())) {
        return true;
      }
    }

    return false;
  }

  public static boolean setFormWidthHint(IForm form, int widthHint) {
    GridData gridDataHints = form.getRootGroupBox().getGridDataHints();
    if (gridDataHints.widthInPixel == widthHint) {
      return false;
    }

    gridDataHints.widthInPixel = widthHint;
    form.getRootGroupBox().setGridDataHints(gridDataHints);
    return true;
  }

  public static ITable getPageTable(IPage page) {
    if (page instanceof IPageWithTable) {
      IPageWithTable tablePage = (IPageWithTable) page;
      return tablePage.getTable();
    }
    else if (page instanceof IPageWithNodes) {
      IPageWithNodes nodePage = (IPageWithNodes) page;
      return nodePage.getInternalTable();
    }

    return null;
  }

  public static IPage getPageFor(IPage parentPage, ITableRow tableRow) {
    ITreeNode node = null;
    if (parentPage instanceof IPageWithNodes) {
      node = ((IPageWithNodes) parentPage).getTreeNodeFor(tableRow);
    }
    else if (parentPage instanceof IPageWithTable<?>) {
      node = ((IPageWithTable<?>) parentPage).getTreeNodeFor(tableRow);
    }

    return (IPage) node;
  }

  public static ITableRow getTableRowFor(IPage parentPage, IPage page) {
    ITableRow row = null;
    if (parentPage instanceof IPageWithNodes) {
      row = ((IPageWithNodes) parentPage).getTableRowFor(page);
    }
    else if (parentPage instanceof IPageWithTable<?>) {
      row = ((IPageWithTable) parentPage).getTableRowFor(page);
    }

    return row;
  }

}
