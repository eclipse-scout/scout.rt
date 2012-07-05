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
package org.eclipse.scout.rt.client.mobile.transformation;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.mobile.ui.action.ButtonWrappingAction;
import org.eclipse.scout.rt.client.mobile.ui.desktop.MobileDesktopUtility;
import org.eclipse.scout.rt.client.mobile.ui.form.outline.MobileOutlineTableForm;
import org.eclipse.scout.rt.client.mobile.ui.forms.OutlineChooserForm;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.view.IViewButton;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutlineTableForm;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutlineTreeForm;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithNodes;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.form.FormUtility;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.IFormFieldVisitor;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.booleanfield.IBooleanField;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.ISequenceBox;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.ISmartField;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBox;

/**
 * @since 3.9.0
 */
public class AbstractDeviceTransformer implements IDeviceTransformer {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractDeviceTransformer.class);

  private final Map<IForm, WeakReference<IForm>> m_modifiedForms = new WeakHashMap<IForm, WeakReference<IForm>>();
  private OutlineChooserForm m_outlineChooserForm;
  private MobileOutlineTableForm m_outlineTableForm;
  private IDesktop m_desktop;

  public AbstractDeviceTransformer(IDesktop desktop) {
    if (desktop == null) {
      desktop = ClientSyncJob.getCurrentSession().getDesktop();
    }
    m_desktop = desktop;
    if (m_desktop == null) {
      throw new IllegalArgumentException("No desktop found. Cannot create device transformer.");
    }
  }

  public AbstractDeviceTransformer() {
    this(null);
  }

  @Override
  public void tablePageLoaded(IPageWithTable<?> tablePage) throws ProcessingException {
  }

  @Override
  public void desktopGuiAttached() throws ProcessingException {
    showOutlineChooserForm();
    showOutlineTableForm();
  }

  @Override
  public void desktopGuiDetached() throws ProcessingException {
    if (m_outlineTableForm != null) {
      m_outlineTableForm.doClose();
    }
    if (m_outlineChooserForm != null) {
      m_outlineChooserForm.doClose();
    }
  }

  /**
   * Remove outline buttons, keystrokes and menus
   */
  @Override
  public void adaptDesktopActions(Collection<IAction> actions) {
    for (Iterator<IAction> iterator = actions.iterator(); iterator.hasNext();) {
      IAction action = iterator.next();
      if (action instanceof IViewButton || action instanceof IKeyStroke || action instanceof IMenu) {
        iterator.remove();
      }
    }
  }

  protected void showOutlineChooserForm() throws ProcessingException {
    if (m_outlineChooserForm == null) {
      m_outlineChooserForm = new OutlineChooserForm();
    }

    if (!getDesktop().isShowing(m_outlineChooserForm)) {
      if (!m_outlineChooserForm.isFormOpen()) {
        m_outlineChooserForm.startView();
      }
      else {
        getDesktop().addForm(m_outlineChooserForm);
      }
    }
  }

  protected void showOutlineTableForm() throws ProcessingException {
    MobileOutlineTableForm mobileOutlineTableForm = new MobileOutlineTableForm();
    if (shouldOutlineTableStatusBeHidden()) {
      mobileOutlineTableForm.getOutlineTableField().setTableStatusVisible(false);
    }

    mobileOutlineTableForm.startView();
  }

  protected boolean shouldOutlineTableStatusBeHidden() {
    return true;
  }

  @Override
  public void transformForm(IForm form) throws ProcessingException {
    form.setAskIfNeedSave(false);
    transformDisplayHintSettings(form);
    transformFormFields(form);
  }

  protected void transformDisplayHintSettings(IForm form) {

  }

  @Override
  public void transformOutline(IOutline outline) {
    if (outline == null || outline.getRootNode() == null) {
      return;
    }

    //Necessary to enable drilldown from top of the outline
    outline.setRootNodeVisible(true);
  }

  @Override
  public void transformPageDetailTable(ITable table) {
    if (table == null) {
      //Do not allow closing the outline table because it breaks the navigation
      makeSurePageDetailTableIsVisible();
    }
  }

  @Override
  public boolean acceptForm(IForm form) {
    if (isFormAddingForbidden(form)) {
      return false;
    }

    return true;
  }

  protected boolean isFormAddingForbidden(IForm form) {
    if (form instanceof IOutlineTreeForm) {
      return true;
    }

    if (form instanceof IOutlineTableForm && !(form instanceof MobileOutlineTableForm)) {
      return true;
    }

    return false;
  }

  private void makeSurePageDetailTableIsVisible() {
    final IOutline outline = getDesktop().getOutline();
    if (outline == null) {
      return;
    }

    final IPage activePage = outline.getActivePage();
    if (activePage == null || activePage.isTableVisible() || isPageDetailTableAllowedToBeClosed(activePage)) {
      return;
    }

    activePage.setTableVisible(true);

    if (activePage instanceof IPageWithNodes) {
      outline.setDetailTable(((IPageWithNodes) activePage).getInternalTable());
    }
    else if (activePage instanceof IPageWithTable<?>) {
      outline.setDetailTable(((IPageWithTable) activePage).getTable());
    }
  }

  protected boolean isPageDetailTableAllowedToBeClosed(IPage activePage) {
    return false;
  }

  protected void transformFormFields(IForm form) throws ProcessingException {
    WeakReference<IForm> formRef = m_modifiedForms.get(form);
    if (formRef != null) {
      return;
    }

    form.visitFields(new IFormFieldVisitor() {
      @Override
      public boolean visitField(IFormField field, int level, int fieldIndex) {
        transformFormField(field);

        return true;
      }
    });

    transformMainBox(form.getRootGroupBox());

    //mark form as modified
    m_modifiedForms.put(form, new WeakReference<IForm>(form));
  }

  private void transformFormField(IFormField field) {
    moveLabelToTop(field);

    if (field instanceof IGroupBox) {
      transformGroupBox((IGroupBox) field);
    }
    else if (field instanceof ISmartField) {
      transformSmartField((ISmartField) field);
    }

  }

  protected void moveLabelToTop(IFormField field) {
    if (IFormField.LABEL_POSITION_ON_FIELD == field.getLabelPosition()) {
      return;
    }

    //Do not modify the labels inside a sequencebox
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

  private void transformMainBox(IGroupBox groupBox) throws ProcessingException {
    if (!groupBox.isScrollable()) {
      groupBox.setScrollable(true);

      //GridDataHints have been modified by setScrollable. Update the actual gridData with those hints.
      FormUtility.rebuildFieldGrid(groupBox.getForm(), true);
    }
  }

  private void transformGroupBox(IGroupBox groupBox) {
    groupBox.setGridColumnCountHint(1);
  }

  private void transformSmartField(ISmartField<?> field) {
    if (field.getBrowseMaxRowCount() > getSmartFieldBrowseMaxRowCount()) {
      field.setBrowseMaxRowCount(getSmartFieldBrowseMaxRowCount());
    }
  }

  /**
   * Used to keep the row count small which speeds up the list.
   */
  protected int getSmartFieldBrowseMaxRowCount() {
    return 20;
  }

  protected IDesktop getDesktop() {
    return m_desktop;
  }

  @Override
  public boolean acceptMobileTabBoxTransformation(ITabBox tabBox) {
    IGroupBox mainBox = tabBox.getForm().getRootGroupBox();
    if (tabBox.getParentField() == mainBox) {
      return !(mainBox.getControlFields()[0] == tabBox);
    }

    return false;
  }

  @Override
  public void adaptFormHeaderLeftActions(IForm form, List<IMenu> menuList) {
    if (MobileDesktopUtility.isToolForm(form) && !containsCloseAction(menuList)) {
      menuList.add(new ToolFormCloseAction(form));
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
            if (wrappedButton.isVisible() && wrappedButton.isEnabled()) {
              return true;
            }
        }
      }
    }

    return false;
  }

}
