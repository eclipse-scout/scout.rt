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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuType;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.controls.ITableControl;
import org.eclipse.scout.rt.client.ui.basic.table.controls.SearchFormTableControl;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.MenuWrapper;
import org.eclipse.scout.rt.client.ui.desktop.outline.OutlineMenuWrapper;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.ISearchForm;
import org.eclipse.scout.rt.client.ui.form.FormEvent;
import org.eclipse.scout.rt.client.ui.form.FormUtility;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.booleanfield.IBooleanField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.placeholder.IPlaceholderField;
import org.eclipse.scout.rt.client.ui.form.fields.radiobuttongroup.IRadioButtonGroup;
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.ISequenceBox;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBox;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.ui.UserAgentUtility;

/**
 * @since 3.9.0
 */
@Order(5200)
public class MobileDeviceTransformer extends AbstractDeviceTransformer {

  @Override
  public boolean isActive() {
    return UserAgentUtility.isMobileDevice();
  }

  @Override
  protected void initTransformationConfig() {
    enableTransformation(MobileDeviceTransformation.MAKE_DESKTOP_COMPACT);
    enableTransformation(MobileDeviceTransformation.MOVE_FIELD_LABEL_TO_TOP);
    enableTransformation(MobileDeviceTransformation.MOVE_FIELD_STATUS_TO_TOP);
    enableTransformation(MobileDeviceTransformation.MAKE_FIELD_SCALEABLE);
    enableTransformation(MobileDeviceTransformation.MAKE_MAINBOX_SCROLLABLE);
    enableTransformation(MobileDeviceTransformation.MAKE_OUTLINE_ROOT_NODE_VISIBLE);
    enableTransformation(MobileDeviceTransformation.REDUCE_GROUPBOX_COLUMNS_TO_ONE);
    enableTransformation(MobileDeviceTransformation.HIDE_PLACEHOLDER_FIELD);
    enableTransformation(MobileDeviceTransformation.HIDE_FIELD_STATUS);
    enableTransformation(MobileDeviceTransformation.DISABLE_FORM_CANCEL_CONFIRMATION);
    enableTransformation(MobileDeviceTransformation.AUTO_CLOSE_SEARCH_FORM);
    enableTransformation(MobileDeviceTransformation.MAXIMIZE_DIALOG);
    enableTransformation(MobileDeviceTransformation.SET_SEQUENCEBOX_UI_HEIGHT);
    enableTransformation(MobileDeviceTransformation.USE_DIALOG_STYLE_FOR_VIEW);
    enableTransformation(MobileDeviceTransformation.AVOID_DETAIL_FORM_AS_DISPLAY_PARENT);
    enableTransformation(MobileDeviceTransformation.SET_RADIO_BUTTON_GROUP_UI_HEIGHT);
  }

  @Override
  public void transformDesktop() {
    if (isTransformationEnabled(MobileDeviceTransformation.MAKE_DESKTOP_COMPACT)) {
      getDesktop().setDisplayStyle(IDesktop.DISPLAY_STYLE_COMPACT);
    }
  }

  @Override
  public void transformForm(IForm form) {
    // Called for every form (desktop forms, embedded forms).
  }

  @Override
  public void notifyFormAboutToShow(IForm form) {
    transformDesktopForm(form);
  }

  protected void transformDesktopForm(IForm form) {
    if (isFormExcluded(form)) {
      return;
    }
    if (isTransformationEnabled(MobileDeviceTransformation.DISABLE_FORM_CANCEL_CONFIRMATION, form)) {
      form.setAskIfNeedSave(false);
    }
    if (form.getDisplayHint() == IForm.DISPLAY_HINT_VIEW) {
      transformView(form);
    }
    else if (form.getDisplayHint() == IForm.DISPLAY_HINT_DIALOG) {
      transformDialog(form);
    }
  }

  protected void transformView(IForm form) {
    form.setDisplayViewId(IForm.VIEW_ID_CENTER);
    if (isTransformationEnabled(MobileDeviceTransformation.USE_DIALOG_STYLE_FOR_VIEW, form)) {
      // Style the view to make it look like a regular dialog.
      // The desktop header will be made invisible by the ui if the form has a header. This saves some space because desktop header would always be about 60px big.
      form.setHeaderVisible(true);
      // mobile-view: add top border, colorize colored menu bar, same as for dialogs
      form.addCssClass("mobile-view");
      // Use same position as for dialogs
      form.getRootGroupBox().setMenuBarPosition(IGroupBox.MENU_BAR_POSITION_BOTTOM);
    }

    // If a detail form were used as display parent, it would immediately disappear after the bench is displayed.
    // This happens because the detail form is removed along with the navigation because it is embedded into a page, and if a display parent is removed the child forms are removed as well.
    // Since the navigation is only removed in compact mode it only needs to be done if that mode is active.
    if (IDesktop.DISPLAY_STYLE_COMPACT.equals(getDesktop().getDisplayStyle())
        && isTransformationEnabled(MobileDeviceTransformation.AVOID_DETAIL_FORM_AS_DISPLAY_PARENT, form)
        && getDesktop().getPageDetailForm() == form.getDisplayParent()) {
      form.setDisplayParent(getDesktop().getOutline());
    }
  }

  protected void transformDialog(IForm form) {
    if (isTransformationEnabled(MobileDeviceTransformation.MAXIMIZE_DIALOG, form)) {
      form.setMaximized(true);
    }
  }

  @Override
  public void transformOutline(IOutline outline) {
    outline.setNavigateButtonsVisible(false);
    outline.setLazyExpandingEnabled(false);
    outline.setToggleBreadcrumbStyleEnabled(false);
    outline.setDisplayStyle(ITree.DISPLAY_STYLE_BREADCRUMB);
    if (isTransformationEnabled(MobileDeviceTransformation.MAKE_OUTLINE_ROOT_NODE_VISIBLE)) {
      ensureOutlineRootContentVisible(outline);
    }
  }

  protected void ensureOutlineRootContentVisible(IOutline outline) {
    if (outline.getDefaultDetailForm() == null && !outline.isOutlineOverviewVisible()) {
      // No root content available
      return;
    }
    // The root content (default detail form / outline overview) will be embedded into the root node
    // To make this work the root node needs to be visible. We also have to mark is as compact root so that the UI knows where to embed the root content.
    // We also need to make sure that deselecting all nodes actually select the root node
    // The root node will only be visible when it is selected which is done by using CSS, see Outline.less
    outline.setRootNodeVisible(true);
    outline.getRootPage().setCompactRoot(true);
    // Use _UI_TreeListener to make sure the event buffer in JsonTree contains the event with no selection when we select the root node,
    // otherwise changing the selection during a selection event would not be possible
    outline.addUITreeListener(event -> {
      if (event.getNewSelectedNodes().size() == 0) {
        outline.selectNode(outline.getRootNode());
      }
    }, TreeEvent.TYPE_NODES_SELECTED);
    if (outline.getSelectedNodes().size() == 0) {
      outline.selectNode(outline.getRootNode());
    }
  }

  @Override
  public void transformPage(IPage page) {
    if (page instanceof IPageWithTable) {
      transformPageWithTable((IPageWithTable) page);
    }
  }

  public void transformPageWithTable(IPageWithTable page) {
    page.setLeaf(false);
    page.setAlwaysCreateChildPage(true);
  }

  @Override
  public void transformPageTable(ITable table, IPage<?> page) {
    for (ITableControl control : table.getTableControls()) {
      if (!(control instanceof SearchFormTableControl)) {
        control.setVisibleGranted(false);
      }
    }
  }

  @Override
  public void notifyPageDetailFormChanged(IForm form) {
    // Detail forms will be displayed inside a page (tree node)
    // Make sure these inner forms are not scrollable because the outline already is
    IGroupBox mainBox = form.getRootGroupBox();
    if (mainBox.isScrollable().isTrue()) {
      mainBox.setScrollable(false);
      FormUtility.initRootBoxGridData(mainBox);
    }
  }

  @Override
  public void notifyPageDetailTableChanged(ITable table) {
    IPage<?> activePage = getDesktop().getOutline().getActivePage();
    if (activePage == null) {
      return;
    }
    IPage<?> parentPage = activePage.getParentPage();
    if (parentPage == null) {
      return;
    }
    ITable parentTable = parentPage.getTable(false);
    if (parentTable == null) {
      return;
    }

    // Remove empty space menus of the current detail table which are already defined on the parent detail table as single selection menus
    // This prevents duplicate menus because the ui concatenates these menus when a node is shown
    // It is important to only remove outline wrapper menus which are defined on the parent table because the menu could be defined on a page and therefore needs to be displayed
    List<IMenu> newMenus = new ArrayList<>();
    for (IMenu menu : table.getMenus()) {
      if ((menu instanceof OutlineMenuWrapper)) {
        OutlineMenuWrapper menuWrapper = (OutlineMenuWrapper) menu;
        IMenu originalMenu = unwrapOutlineWrapperMenu(menuWrapper);
        if (menuWrapper.getMenuTypes().contains(TableMenuType.EmptySpace)
            && originalMenu.getMenuTypes().contains(TableMenuType.SingleSelection)
            && parentTable.getMenus().contains(originalMenu)) {
          // This menu should be removed -> don't add it to the list of new menus
          continue;
        }
      }
      newMenus.add(menu);
    }
    if (!CollectionUtility.equalsCollection(newMenus, table.getContextMenu().getChildActions())) {
      table.getContextMenu().setChildActions(newMenus);
    }
  }

  protected static IMenu unwrapOutlineWrapperMenu(IMenu menu) {
    return MenuWrapper.unwrapMenu(menu);
  }

  @Override
  public void notifyPageSearchFormInit(final IPageWithTable<ITable> page) {
    if (!isTransformationEnabled(MobileDeviceTransformation.AUTO_CLOSE_SEARCH_FORM)) {
      return;
    }
    ISearchForm searchForm = page.getSearchFormInternal();
    searchForm.addFormListener(e -> {
      if (FormEvent.TYPE_STORE_AFTER == e.getType()) {
        onSearchFormStored(page);
      }
    });
  }

  protected void onSearchFormStored(IPageWithTable<ITable> page) {
    SearchFormTableControl tableControl = page.getTable().getTableControl(SearchFormTableControl.class);
    if (tableControl != null) {
      tableControl.setSelected(false);
    }
  }

  @Override
  public void transformFormField(IFormField field) {
    if (isTransformationEnabled(MobileDeviceTransformation.MOVE_FIELD_LABEL_TO_TOP, field)) {
      moveLabelToTop(field);
    }
    if (isTransformationEnabled(MobileDeviceTransformation.MAKE_FIELD_SCALEABLE, field)) {
      makeFieldScalable(field);
    }
    if (isTransformationEnabled(MobileDeviceTransformation.HIDE_FIELD_STATUS, field)) {
      hideStatus(field);
    }
    if (isTransformationEnabled(MobileDeviceTransformation.MOVE_FIELD_STATUS_TO_TOP, field)) {
      moveStatusToTop(field);
    }

    if (field instanceof IGroupBox) {
      transformGroupBox((IGroupBox) field);
    }
    else if (field instanceof IPlaceholderField) {
      transformPlaceholderField((IPlaceholderField) field);
    }
    else if (field instanceof ISequenceBox) {
      transformSequenceBox((ISequenceBox) field);
    }
    else if (field instanceof IRadioButtonGroup) {
      transformRadioButtonGroup((IRadioButtonGroup) field);
    }
  }

  /**
   * Makes sure weightX is set to 1 which makes the field scalable.
   * <p>
   * Reason:<br>
   * The width of the field should be adjusted according to the display width, otherwise it may be too big to be
   * displayed. <br>
   * Additionally, since we use a one column layout, setting weightX to 0 might destroy the layout because it affects
   * all the fields in the groupBox.
   */
  protected void makeFieldScalable(IFormField field) {
    // Since a sequencebox contains several fields it's very risky to modify the gridData because it could make the fields too big or too small.
    if (field.getParentField() instanceof ISequenceBox) {
      return;
    }

    // Make sure weightX is set to 1 so the field grows and shrinks and does not break the 1 column layout
    GridData gridDataHints = field.getGridDataHints();
    if (gridDataHints.weightX == 0) {
      gridDataHints.weightX = 1;
      field.setGridDataHints(gridDataHints);
      rebuildParentGrid(field);
    }
  }

  protected void moveLabelToTop(IFormField field) {
    if (field instanceof IGroupBox) {
      return;
    }

    if (ObjectUtility.isOneOf(field.getLabelPosition(), IFormField.LABEL_POSITION_ON_FIELD, IFormField.LABEL_POSITION_BOTTOM)) {
      return;
    }

    // Do not modify the labels inside a sequencebox
    if (field.getParentField() instanceof ISequenceBox) {
      return;
    }

    field.setLabelPosition(IFormField.LABEL_POSITION_TOP);

    // The actual label of the boolean field is on the right side and position=top has no effect.
    // Removing the label actually removes the place on the left side so that it gets aligned to the other fields.
    if (field instanceof IBooleanField) {
      field.setLabelVisible(false);
    }
    else if (!StringUtility.hasText(field.getLabel()) && !(field instanceof ITabBox)) {
      // If label is empty and moved to top it will waste space -> don't show it
      field.setLabelVisible(false);
    }
  }

  protected void moveStatusToTop(IFormField field) {
    field.setStatusPosition(IFormField.STATUS_POSITION_TOP);
  }

  protected void hideStatus(IFormField field) {
    if ((field instanceof ICompositeField)) {
      field.setStatusVisible(false, false);
    }
    else {
      field.setStatusVisible(false);
    }
  }

  protected void transformMainBox(IGroupBox groupBox) {
    if (isTransformationEnabled(MobileDeviceTransformation.MAKE_MAINBOX_SCROLLABLE, groupBox)) {
      makeGroupBoxScrollable(groupBox);
    }
  }

  protected void makeGroupBoxScrollable(IGroupBox groupBox) {
    if (!groupBox.isScrollable().isTrue()) {
      groupBox.setScrollable(true);

      // GridDataHints have been modified by setScrollable. Update the actual gridData with those hints.
      if (groupBox.isMainBox()) {
        FormUtility.initRootBoxGridData(groupBox);
      }
      else {
        rebuildParentGrid(groupBox);
      }
    }
  }

  protected void transformGroupBox(IGroupBox groupBox) {
    if (groupBox.isMainBox()) {
      transformMainBox(groupBox);
    }
    if (isTransformationEnabled(MobileDeviceTransformation.REDUCE_GROUPBOX_COLUMNS_TO_ONE, groupBox)) {
      groupBox.setGridColumnCount(1);
    }
    // Transformations already done.
    groupBox.setResponsive(false);
  }

  /**
   * Makes placeholder fields invisible since they just waste space on 1 column layouts
   */
  protected void transformPlaceholderField(IPlaceholderField field) {
    if (isTransformationEnabled(MobileDeviceTransformation.HIDE_PLACEHOLDER_FIELD, field)) {
      field.setVisible(false);
    }
  }

  protected void transformSequenceBox(ISequenceBox box) {
    if (!isTransformationEnabled(MobileDeviceTransformation.SET_SEQUENCEBOX_UI_HEIGHT, box)) {
      return;
    }
    transformUseUiHeight(box);
  }

  protected void transformRadioButtonGroup(IRadioButtonGroup box) {
    if (!isTransformationEnabled(MobileDeviceTransformation.SET_RADIO_BUTTON_GROUP_UI_HEIGHT, box)) {
      return;
    }
    transformUseUiHeight(box);
  }

  /**
   * Make the form field use its UI height. This is necessary for e.g. sequence boxes or radio button groups if the
   * labels of the containing fields are moved to top because in that case a logical row height of 1 is not sufficient
   * anymore.
   */
  protected void transformUseUiHeight(IFormField field) {
    if (field == null) {
      return;
    }
    GridData gridDataHints = field.getGridDataHints();
    if (!gridDataHints.useUiHeight) {
      gridDataHints.useUiHeight = true;
      field.setGridDataHints(gridDataHints);
      rebuildParentGrid(field);
    }
  }
}
