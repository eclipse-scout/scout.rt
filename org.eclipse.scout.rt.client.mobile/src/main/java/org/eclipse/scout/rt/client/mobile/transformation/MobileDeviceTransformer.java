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
package org.eclipse.scout.rt.client.mobile.transformation;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.scout.rt.client.mobile.navigation.AbstractMobileBackAction;
import org.eclipse.scout.rt.client.mobile.navigation.IBreadCrumbsNavigation;
import org.eclipse.scout.rt.client.mobile.navigation.IBreadCrumbsNavigationService;
import org.eclipse.scout.rt.client.mobile.ui.action.ButtonWrappingAction;
import org.eclipse.scout.rt.client.mobile.ui.desktop.MobileDesktopUtility;
import org.eclipse.scout.rt.client.mobile.ui.form.AbstractMobileForm;
import org.eclipse.scout.rt.client.mobile.ui.form.fields.button.IMobileButton;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.controls.ITableControl;
import org.eclipse.scout.rt.client.ui.basic.table.controls.SearchFormTableControl;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.form.FormUtility;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.IFormFieldVisitor;
import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.booleanfield.IBooleanField;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.placeholder.IPlaceholderField;
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.ISequenceBox;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBox;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;

/**
 * @since 3.9.0
 */
public class MobileDeviceTransformer implements IDeviceTransformer {
  private final Map<IForm, WeakReference<IForm>> m_transformedForms = new WeakHashMap<>();
  private final Map<IOutline, WeakReference<IOutline>> m_transformedOutlines = new WeakHashMap<>();
  private IDesktop m_desktop;
  private ToolFormHandler m_toolFormHandler;
  private boolean m_gridDataDirty;
  private DeviceTransformationConfig m_deviceTransformationConfig;

  public MobileDeviceTransformer(IDesktop desktop) {
    if (desktop == null) {
      desktop = ClientSessionProvider.currentSession().getDesktop();
    }
    m_desktop = desktop;
    if (m_desktop == null) {
      throw new IllegalArgumentException("No desktop found. Cannot create device transformer.");
    }

    m_deviceTransformationConfig = createDeviceTransformationConfig();
    initTransformationConfig();

    m_toolFormHandler = createToolFormHandler(desktop);

    IBreadCrumbsNavigation breadCrumbsNavigation = BEANS.get(IBreadCrumbsNavigationService.class).getBreadCrumbsNavigation();
    if (breadCrumbsNavigation != null) {
      breadCrumbsNavigation.trackDisplayViewId(IForm.VIEW_ID_CENTER);
    }
  }

  public MobileDeviceTransformer() {
    this(null);
  }

  protected ToolFormHandler createToolFormHandler(IDesktop desktop) {
    return new ToolFormHandler(getDesktop());
  }

  public ToolFormHandler getToolFormHandler() {
    return m_toolFormHandler;
  }

  protected DeviceTransformationConfig createDeviceTransformationConfig() {
    return new DeviceTransformationConfig();
  }

  @Override
  public DeviceTransformationConfig getDeviceTransformationConfig() {
    return m_deviceTransformationConfig;
  }

  protected void initTransformationConfig() {
    List<IDeviceTransformation> transformations = new LinkedList<IDeviceTransformation>();

    transformations.add(MobileDeviceTransformation.MOVE_FIELD_LABEL_TO_TOP);
    transformations.add(MobileDeviceTransformation.MAKE_FIELD_SCALEABLE);
    transformations.add(MobileDeviceTransformation.MAKE_MAINBOX_SCROLLABLE);
    transformations.add(MobileDeviceTransformation.REDUCE_GROUPBOX_COLUMNS_TO_ONE);
    transformations.add(MobileDeviceTransformation.HIDE_PLACEHOLDER_FIELD);
    transformations.add(MobileDeviceTransformation.DISABLE_FORM_CANCEL_CONFIRMATION);
    transformations.add(MobileDeviceTransformation.DISPLAY_FORM_HEADER);
    transformations.add(MobileDeviceTransformation.ADD_MISSING_BACK_ACTION_TO_FORM_HEADER);
    transformations.add(MobileDeviceTransformation.DISPLAY_PAGE_TABLE);

    for (IDeviceTransformation transformation : transformations) {
      getDeviceTransformationConfig().enableTransformation(transformation);
    }
  }

  @Override
  public List<String> getAcceptedViewIds() {
    List<String> viewIds = new LinkedList<String>();
    viewIds.add(IForm.VIEW_ID_CENTER);

    return viewIds;
  }

  @Override
  public void notifyTablePageLoaded(IPageWithTable<?> tablePage) {
    if (m_toolFormHandler != null) {
      m_toolFormHandler.notifyTablePageLoaded(tablePage);
    }
  }

  /**
   * Remove keystrokes
   */
  @Override
  public void adaptDesktopActions(Collection<IAction> actions) {
    for (Iterator<IAction> iterator = actions.iterator(); iterator.hasNext();) {
      IAction action = iterator.next();
      if (action instanceof IKeyStroke) {
        iterator.remove();
      }
    }
  }

  @Override
  public void adaptDesktopOutlines(OrderedCollection<IOutline> outlines) {
  }

  @Override
  public void notifyDesktopClosing() {
  }

  @Override
  public void transformDesktop() {
    getDesktop().setDisplayStyle(IDesktop.DISPLAY_STYLE_COMPACT);
  }

  @Override
  public void transformForm(IForm form) {
    if (getDeviceTransformationConfig().isFormExcluded(form)) {
      return;
    }
    List<IDeviceTransformationHook> hooks = DeviceTransformationHooks.getFormTransformationHooks(form.getClass());
    if (hooks != null) {
      for (IDeviceTransformationHook hook : hooks) {
        hook.beforeFormTransformation(form);
      }
    }

    m_gridDataDirty = false;

    if (getDeviceTransformationConfig().isTransformationEnabled(MobileDeviceTransformation.DISABLE_FORM_CANCEL_CONFIRMATION)) {
      form.setAskIfNeedSave(false);
    }
    if (getDeviceTransformationConfig().isTransformationEnabled(MobileDeviceTransformation.DISPLAY_FORM_HEADER)) {
      AbstractMobileForm.setHeaderVisible(form, true);
    }
    if (form.getDisplayHint() == IForm.DISPLAY_HINT_VIEW) {
      transformView(form);
    }
    transformFormFields(form);

    if (isGridDataDirty()) {
      FormUtility.rebuildFieldGrid(form, true);
      m_gridDataDirty = false;
    }
  }

  protected void transformView(IForm form) {
    form.setDisplayViewId(IForm.VIEW_ID_CENTER);
  }

  @Override
  public void transformOutline(IOutline outline) {
    if (outline == null || outline.getRootNode() == null) {
      return;
    }
    WeakReference<IOutline> outlineRef = m_transformedOutlines.get(outline);
    if (outlineRef != null) {
      // Already transformed
      return;
    }

    if (getDeviceTransformationConfig().isTransformationEnabled(MobileDeviceTransformation.DISPLAY_OUTLINE_ROOT_NODE)) {
      // Necessary to enable drilldown from top of the outline
      outline.setRootNodeVisible(true);
    }
    outline.setNavigateButtonsVisible(false);
    outline.setLazyExpandingEnabled(false);
    outline.setAutoToggleBreadcrumbStyle(false);
    outline.setDisplayStyle(ITree.DISPLAY_STYLE_BREADCRUMB);
    m_transformedOutlines.put(outline, new WeakReference<IOutline>(outline));
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

    for (ITableControl control : page.getTable().getTableControls()) {
      if (!(control instanceof SearchFormTableControl)) {
        // TODO CGU Maybe some controls could be useful, like group ware or tile preview, how to distinguish?
        control.setVisibleGranted(false);
      }
    }
  }

  @Override
  public void transformPageDetailTable(ITable table) {
    if (table == null) {
      if (getDeviceTransformationConfig().isTransformationEnabled(MobileDeviceTransformation.DISPLAY_PAGE_TABLE)) {
        // Do not allow closing the outline table because it breaks the navigation
        makeSurePageDetailTableIsVisible();
      }
    }
  }

  @Override
  public boolean acceptFormAddingToDesktop(IForm form) {
    return true;
  }

  private void makeSurePageDetailTableIsVisible() {
    final IOutline outline = getDesktop().getOutline();
    if (outline == null) {
      return;
    }
    final IPage<?> activePage = outline.getActivePage();
    if (activePage == null || activePage.isTableVisible() || isPageDetailTableAllowedToBeClosed(activePage)) {
      return;
    }
    activePage.setTableVisible(true);
    outline.setDetailTable(activePage.getTable());
  }

  protected boolean isPageDetailTableAllowedToBeClosed(IPage<?> activePage) {
    return activePage.isLeaf();
  }

  protected void transformFormFields(IForm form) {
    WeakReference<IForm> formRef = m_transformedForms.get(form);
    if (formRef != null) {
      return;
    }

    form.visitFields(new IFormFieldVisitor() {
      @Override
      public boolean visitField(IFormField field, int level, int fieldIndex) {
        if (getDeviceTransformationConfig().isFieldExcluded(field)) {
          return true;
        }

        transformFormField(field);

        return true;
      }
    });

    if (!getDeviceTransformationConfig().isFieldExcluded(form.getRootGroupBox())) {
      transformMainBox(form.getRootGroupBox());
    }

    //mark form as modified
    m_transformedForms.put(form, new WeakReference<IForm>(form));
  }

  protected void transformFormField(IFormField field) {
    List<IDeviceTransformationHook> hooks = DeviceTransformationHooks.getFormFieldTransformationHooks(field.getClass());
    if (hooks != null) {
      for (IDeviceTransformationHook hook : hooks) {
        hook.beforeFormFieldTransformation(field);
      }
    }

    if (getDeviceTransformationConfig().isTransformationEnabled(MobileDeviceTransformation.MOVE_FIELD_LABEL_TO_TOP, field)) {
      moveLabelToTop(field);
    }

    if (getDeviceTransformationConfig().isTransformationEnabled(MobileDeviceTransformation.MAKE_FIELD_SCALEABLE, field)) {
      makeFieldScalable(field);
    }

    if ((field instanceof ICompositeField)) {
      ((ICompositeField) field).setStatusVisible(false, false);
    }
    else {
      field.setStatusVisible(false);
    }
    field.setStatusPosition(IFormField.STATUS_POSITION_TOP);

    if (field instanceof IGroupBox) {
      transformGroupBox((IGroupBox) field);
    }
    else if (field instanceof IPlaceholderField) {
      transformPlaceholderField((IPlaceholderField) field);
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
    //Since a sequencebox contains several fields it's very risky to modify the gridData because it could make the fields too big or too small.
    if (field.getParentField() instanceof ISequenceBox) {
      return;
    }

    //Make sure weightX is set to 1 so the field grows and shrinks and does not break the 1 column layout
    GridData gridDataHints = field.getGridDataHints();
    if (gridDataHints.weightX == 0) {
      gridDataHints.weightX = 1;
      field.setGridDataHints(gridDataHints);

      markGridDataDirty();
    }
  }

  protected void moveLabelToTop(IFormField field) {
    if (field instanceof IGroupBox) {
      return;
    }

    if (IFormField.LABEL_POSITION_ON_FIELD == field.getLabelPosition()) {
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
  }

  protected void transformMainBox(IGroupBox groupBox) {
    if (getDeviceTransformationConfig().isTransformationEnabled(MobileDeviceTransformation.MAKE_MAINBOX_SCROLLABLE, groupBox)) {
      makeGroupBoxScrollable(groupBox);
    }
  }

  protected void makeGroupBoxScrollable(IGroupBox groupBox) {
    //Detail forms will be displayed as inner forms on page forms.
    //Make sure these inner forms are not scrollable because the page form already is
    if (groupBox.getForm() == getDesktop().getPageDetailForm()) {
      if (groupBox.isScrollable().isTrue()) {
        groupBox.setScrollable(false);
        markGridDataDirty();
      }
      return;
    }

    if (!groupBox.isScrollable().isTrue()) {
      groupBox.setScrollable(true);

      //GridDataHints have been modified by setScrollable. Update the actual gridData with those hints.
      markGridDataDirty();
    }
  }

  protected void transformGroupBox(IGroupBox groupBox) {
    if (getDeviceTransformationConfig().isTransformationEnabled(MobileDeviceTransformation.REDUCE_GROUPBOX_COLUMNS_TO_ONE, groupBox)) {
      groupBox.setGridColumnCountHint(1);
    }
  }

  /**
   * Makes placeholder fields invisible since they just waste space on 1 column layouts
   */
  protected void transformPlaceholderField(IPlaceholderField field) {
    if (getDeviceTransformationConfig().isTransformationEnabled(MobileDeviceTransformation.HIDE_PLACEHOLDER_FIELD, field)) {
      field.setVisible(false);
    }
  }

  protected IDesktop getDesktop() {
    return m_desktop;
  }

  @Override
  public boolean acceptMobileTabBoxTransformation(ITabBox tabBox) {
    IGroupBox mainBox = tabBox.getForm().getRootGroupBox();
    if (tabBox.getParentField() == mainBox) {
      return !(mainBox.getControlFields().get(0) == tabBox);
    }

    return false;
  }

  /**
   * Adds a close button to the tool form.
   * <p>
   * Adds a back button if there is no other button on the left side which is able to close the form.
   */
  @Override
  public void adaptFormHeaderLeftActions(IForm form, List<IMenu> menuList) {
    if (MobileDesktopUtility.isToolForm(form) && !containsCloseAction(menuList)) {
      menuList.add(new ToolFormCloseAction(form));
    }

    if (getDeviceTransformationConfig().isTransformationEnabled(MobileDeviceTransformation.ADD_MISSING_BACK_ACTION_TO_FORM_HEADER, form) && !containsCloseAction(menuList)) {
      menuList.add(new P_BackAction());
    }
  }

  @Override
  public void adaptFormHeaderRightActions(IForm form, List<IMenu> menuList) {
  }

  protected boolean containsCloseAction(List<IMenu> menuList) {
    if (menuList == null) {
      return false;
    }

    for (IMenu action : menuList) {
      if (action instanceof ToolFormCloseAction) {
        return true;
      }
      else if (action instanceof ButtonWrappingAction) {
        IButton wrappedButton = ((ButtonWrappingAction) action).getWrappedButton();
        switch (wrappedButton.getSystemType()) {
          case IButton.SYSTEM_TYPE_CANCEL:
          case IButton.SYSTEM_TYPE_CLOSE:
          case IButton.SYSTEM_TYPE_OK:
          case IMobileButton.SYSTEM_TYPE_BACK:
            if (wrappedButton.isVisible() && wrappedButton.isEnabled()) {
              return true;
            }
        }
      }
    }

    return false;
  }

  protected boolean isGridDataDirty() {
    return m_gridDataDirty;
  }

  protected void markGridDataDirty() {
    m_gridDataDirty = true;
  }

  private class P_BackAction extends AbstractMobileBackAction {

  }
}
