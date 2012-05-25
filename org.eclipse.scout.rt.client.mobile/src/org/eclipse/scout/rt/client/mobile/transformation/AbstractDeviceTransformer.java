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
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.mobile.services.IMobileNavigationService;
import org.eclipse.scout.rt.client.mobile.ui.form.outline.MobileOutlineTableForm;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutlineTableForm;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutlineTreeForm;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithNodes;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.IFormFieldVisitor;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.booleanfield.IBooleanField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.ISequenceBox;
import org.eclipse.scout.service.SERVICES;

/**
 * @since 3.8.0
 */
public class AbstractDeviceTransformer implements IDeviceTransformer {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractDeviceTransformer.class);

  private final Map<IForm, WeakReference<IForm>> m_modifiedForms = new WeakHashMap<IForm, WeakReference<IForm>>();

  @Override
  public void transformDesktop(IDesktop desktop) {

  }

  @Override
  public void transformForm(IForm form) {
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

    outline.setRootNodeVisible(true);
    //Changing the root node cell so that the navigation history looks fine.
    //BTW: I believe that the root node cell text/icon should always have these settings
    //TODO CGU what is this for?
    Cell cell = outline.getRootNode().getCellForUpdate();
    cell.setText(outline.getTitle());
    cell.setIconId(outline.getIconId());
  }

  @Override
  public boolean acceptForm(IForm form) {
    if (isFormAddingForbidden(form)) {
      return false;
    }

    if (isDetailFormAndAddingForbidden(form)) {
      makeSureOutlineTableIsVisible();

      return false;
    }

    return true;
  }

  private boolean isDetailFormAndAddingForbidden(IForm form) {
    IForm pageDetailForm = getDesktop().getPageDetailForm();
    if (form == pageDetailForm) {
      IOutline outline = getDesktop().getOutline();
      return !outline.getActivePage().isLeaf();
    }

    return false;
  }

  private boolean isFormAddingForbidden(IForm form) {
    if (form instanceof IOutlineTreeForm) {
      return !SERVICES.getService(IMobileNavigationService.class).getDeviceNavigator().isOutlineTreeAvailable();
    }

    if (form instanceof IOutlineTableForm && !(form instanceof MobileOutlineTableForm)) {
      return true;
    }

    return false;
  }

  private void makeSureOutlineTableIsVisible() {
    final IOutline outline = getDesktop().getOutline();
    final IPage activePage = outline.getActivePage();
    if (activePage == null || activePage.isTableVisible() || activePage.isLeaf()) {
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

  protected void transformFormFields(IForm form) {
    WeakReference<IForm> formRef = m_modifiedForms.get(form);
    if (formRef != null) {
      return;
    }

    //mark form as modified
    m_modifiedForms.put(form, new WeakReference<IForm>(form));

    transformMainBox(form.getRootGroupBox());

    form.visitFields(new IFormFieldVisitor() {
      @Override
      public boolean visitField(IFormField field, int level, int fieldIndex) {
        transformFormField(field);

        return true;
      }
    });
  }

  private void transformFormField(IFormField field) {
    moveLabelToTop(field);

    if (field instanceof IGroupBox) {
      transformGroupBox((IGroupBox) field);
    }

  }

  private void moveLabelToTop(IFormField field) {
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

  private void transformMainBox(IGroupBox groupBox) {
    groupBox.setScrollable(true);
  }

  private void transformGroupBox(IGroupBox groupBox) {
    groupBox.setGridColumnCountHint(1);
  }

  protected IDesktop getDesktop() {
    return ClientSyncJob.getCurrentSession().getDesktop();
  }

}
