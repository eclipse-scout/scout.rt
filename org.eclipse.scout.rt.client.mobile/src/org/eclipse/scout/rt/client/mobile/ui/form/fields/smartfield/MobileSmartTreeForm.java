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
package org.eclipse.scout.rt.client.mobile.ui.form.fields.smartfield;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.List;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.mobile.transformation.DeviceTransformationConfig;
import org.eclipse.scout.rt.client.mobile.transformation.DeviceTransformationUtility;
import org.eclipse.scout.rt.client.mobile.transformation.MobileDeviceTransformation;
import org.eclipse.scout.rt.client.mobile.ui.form.fields.button.AbstractBackButton;
import org.eclipse.scout.rt.client.mobile.ui.form.fields.smartfield.MobileSmartTreeForm.MainBox.GroupBox.FilterField;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeAdapter;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;
import org.eclipse.scout.rt.client.ui.form.FormEvent;
import org.eclipse.scout.rt.client.ui.form.FormListener;
import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.ISmartField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.SmartTreeForm;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.eclipse.scout.service.SERVICES;

/**
 * @since 3.9.0
 */
public class MobileSmartTreeForm extends SmartTreeForm {
  private P_SmartFieldListener m_smartFieldListener;
  private boolean m_acceptingProposal;

  public MobileSmartTreeForm(ISmartField<?> smartField) throws ProcessingException {
    super(smartField);
  }

  @Override
  protected void initConfig() throws ProcessingException {
    super.initConfig();

    String title = getSmartField().getLabel();
    if (StringUtility.hasText(title)) {
      title = TEXTS.get("MobileSmartFormTitle", title);
    }
    else {
      title = TEXTS.get("MobileSmartFormTitleDefault");
    }
    setTitle(title);
    getResultTreeField().getTree().setCheckable(true);
    getResultTreeField().getTree().addTreeListener(new P_TreeListener());

    GridData treeFieldGridDataHints = getResultTreeField().getGridDataHints();
    treeFieldGridDataHints.useUiHeight = false;
    treeFieldGridDataHints.useUiWidth = false;
    treeFieldGridDataHints.h = 2;
    treeFieldGridDataHints.fillVertical = true;
    getResultTreeField().setGridDataHints(treeFieldGridDataHints);

    addFormListener(new P_FormListener());
    if (m_smartFieldListener == null) {
      m_smartFieldListener = new P_SmartFieldListener();
      getSmartField().addPropertyChangeListener(m_smartFieldListener);
    }
  }

  @Override
  protected void execDisposeForm() throws ProcessingException {
    super.execDisposeForm();

    if (m_smartFieldListener != null) {
      getSmartField().removePropertyChangeListener(m_smartFieldListener);
      m_smartFieldListener = null;
    }
  }

  @Override
  protected int getConfiguredDisplayHint() {
    return DISPLAY_HINT_DIALOG;
  }

  @Override
  protected boolean getConfiguredModal() {
    return true;
  }

  @Override
  protected boolean getConfiguredAskIfNeedSave() {
    return true;
  }

  @Override
  protected void injectResultTreeMenus(List<IMenu> menuList) {
    super.injectResultTreeMenus(menuList);

    List<IMenu> smartFieldMenus = Arrays.asList(getSmartField().getMenus());
    menuList.addAll(smartFieldMenus);
  }

  @Override
  protected void execResultTreeNodeClick(ITreeNode node) throws ProcessingException {
    // nop. Clicking a node must NOT close the form.
  }

  private void acceptProposal() throws ProcessingException {
    m_acceptingProposal = true;
    try {
      LookupRow lookupRow = getAcceptedProposal();
      if (lookupRow != null) {
        getSmartField().acceptProposal(lookupRow);
      }
    }
    finally {
      m_acceptingProposal = false;
    }
  }

  @Override
  public LookupRow getAcceptedProposal() throws ProcessingException {
    LookupRow row = getSelectedLookupRow();
    if (row != null && row.isEnabled()) {
      return row;
    }
    else if (getSmartField().isAllowCustomText()) {
      return new CustomTextLookupRow(getFilterField().getValue());
    }
    else {
      // With the mobile smartfield deleting a value is only possible by not selecting any value.
      // The deletion of the value is achieved by returning an empty lookup row.
      return ISmartField.EMPTY_LOOKUP_ROW;
    }
  }

  public FilterField getFilterField() {
    return getFieldByClass(FilterField.class);
  }

  public class MainBox extends SmartTreeForm.MainBox {

    @Override
    protected void execInitField() throws ProcessingException {
      super.execInitField();

      //It's sufficient if table is scrollable, form itself does not need to be
      DeviceTransformationConfig config = DeviceTransformationUtility.getDeviceTransformationConfig();
      if (config != null) {
        config.excludeFieldTransformation(this, MobileDeviceTransformation.MAKE_MAINBOX_SCROLLABLE);
      }
    }

    @Override
    protected int getConfiguredHeightInPixel() {
      return 400;
    }

    @Order(1)
    public class GroupBox extends AbstractGroupBox {

      @Override
      protected boolean getConfiguredBorderVisible() {
        return true;
      }

      @Order(1)
      public class FilterField extends AbstractStringField {

        @Override
        protected boolean getConfiguredLabelVisible() {
          return false;
        }

        @Override
        protected boolean getConfiguredValidateOnAnyKey() {
          return true;
        }

        @Override
        protected void execChangedValue() throws ProcessingException {
          setSearchText(getValue());
          update(false, false);
        }

      }
    }

    @Order(99)
    public class BackButton extends AbstractBackButton {

    }
  }

  private void handleTreeNodesUpdated(ITreeNode[] nodes) {
    if (nodes != null && nodes.length > 0) {
      try {
        //Accept proposal if a node gets checked. This makes sure the smartfield menus work.
        acceptProposal();
      }
      catch (ProcessingException e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(e);
      }
    }
  }

  private class P_TreeListener extends TreeAdapter {

    @Override
    public void treeChanged(TreeEvent e) {
      switch (e.getType()) {
        case TreeEvent.TYPE_NODES_UPDATED: {
          handleTreeNodesUpdated(e.getNodes());
          break;
        }
      }
    }
  }

  /**
   * Updates the {@link FilterField} with the display text of the smartfield if the value gets changed externally (e.g.
   * by a smartfield menu action).
   */
  private class P_SmartFieldListener implements PropertyChangeListener {

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if (ISmartField.PROP_VALUE.equals(evt.getPropertyName())) {
        if (!m_acceptingProposal) {
          getFilterField().setValue(getSmartField().getDisplayText());
        }
      }
    }

  }

  private class P_FormListener implements FormListener {
    @Override
    public void formChanged(FormEvent e) throws ProcessingException {
      switch (e.getType()) {
        case FormEvent.TYPE_CLOSED: {
          if (e.getForm() != MobileSmartTreeForm.this) {
            return;
          }

          removeFormListener(this);
          if (getCloseSystemType() == IButton.SYSTEM_TYPE_OK) {
            LookupRow row = getAcceptedProposal();
            if (row instanceof CustomTextLookupRow) {
              // Setting the value is done by AbstractSmartField.P_ProposalFormListener
              // Unfortunately, if the value is not valid, the display text is not updated as well.
              // That's why it is set here
              getSmartField().setDisplayText(row.getText());
            }
          }
          break;
        }
      }
    }
  }

}
