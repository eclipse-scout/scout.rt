/*******************************************************************************
 * Copyright (c) 2012 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.mobile.form.fields.smartfield;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.client.mobile.ui.form.fields.smartfield.MobileSmartFieldProposalFormProvider;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.ISmartField;
import org.eclipse.scout.rt.ui.rap.LogicalGridLayout;
import org.eclipse.scout.rt.ui.rap.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.rap.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.rap.form.fields.RwtScoutValueFieldComposite;
import org.eclipse.scout.rt.ui.rap.form.fields.smartfield.IRwtScoutSmartField;
import org.eclipse.scout.rt.ui.rap.util.RwtLayoutUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

public class RwtScoutMobileSmartField extends RwtScoutValueFieldComposite<ISmartField<?>> implements IRwtScoutSmartField {
  private Composite m_browseIconContainer;
  private Composite m_smartContainer;

  public RwtScoutMobileSmartField() {
  }

  @Override
  protected void initializeUi(Composite parent) {
    Composite container = getUiEnvironment().getFormToolkit().createComposite(parent);
    StatusLabelEx label = getUiEnvironment().getFormToolkit().createStatusLabel(container, getScoutObject());

    m_smartContainer = getUiEnvironment().getFormToolkit().createComposite(container, SWT.BORDER);
    m_smartContainer.setData(RWT.CUSTOM_VARIANT, getSmartfieldVariant());

    Text textField = new Text(m_smartContainer, SWT.NONE);
    getUiEnvironment().getFormToolkit().adapt(textField, false, false);

    // correction to look like a normal text
    textField.setData(RWT.CUSTOM_VARIANT, getSmartfieldVariant());

    m_browseIconContainer = getUiEnvironment().getFormToolkit().createComposite(m_smartContainer);

    setUiContainer(container);
    setUiLabel(label);
    setUiField(textField);

    // layout
    container.setLayout(new LogicalGridLayout(1, 0));

    // m_browseIconContainer and m_browseIconContainer are only necessary to position the icon at the right of the field
    // If Bug 361799 gets fixed this could be replaced by background-position and background-repeat
    m_smartContainer.setLayoutData(LogicalGridDataBuilder.createField(((IFormField) getScoutObject()).getGridData()));
    GridLayout gridLayout = RwtLayoutUtility.createGridLayoutNoSpacing(2, false);
    //Paddings cannot be set in css because then mouse click won't work in that region
    gridLayout.marginLeft = 6;
    gridLayout.marginRight = 6;
    m_smartContainer.setLayout(gridLayout);

    GridData textLayoutData = new GridData(SWT.FILL, SWT.CENTER, true, true);
    textField.setLayoutData(textLayoutData);

    GridData buttonLayoutData = new GridData(SWT.CENTER, SWT.CENTER, false, false);
    buttonLayoutData.heightHint = 20;
    buttonLayoutData.widthHint = 20;
    m_browseIconContainer.setLayoutData(buttonLayoutData);

    textField.addMouseListener(new P_FieldSelectionListener());
    m_browseIconContainer.addMouseListener(new P_FieldSelectionListener());
    m_smartContainer.addMouseListener(new P_FieldSelectionListener());
  }

  protected String getSmartfieldVariant() {
    return VARIANT_SMARTFIELD;
  }

  protected String getSmartfieldDisabledVariant() {
    return VARIANT_SMARTFIELD_DISABLED;
  }

  @Override
  public Text getUiField() {
    return (Text) super.getUiField();
  }

  @Override
  protected void attachScout() {
    super.attachScout();

    setIconIdFromScout(getScoutObject().getIconId());
    getScoutObject().setProposalFormProvider(new MobileSmartFieldProposalFormProvider());
  }

  @Override
  protected void setDisplayTextFromScout(String s) {
    if (!CompareUtility.equals(s, getUiField().getText())) {
      if (s == null) {
        s = "";
      }
      getUiField().setText(s);
    }
  }

  @Override
  protected void setEnabledFromScout(boolean b) {
    super.setEnabledFromScout(b);

    //Textfields are never disabled, see TextFieldEditableSupport.
    getUiField().setEnabled(true);
    getUiField().setEditable(false);

    if (b) {
      m_smartContainer.setData(RWT.CUSTOM_VARIANT, getSmartfieldVariant());
    }
    else {
      m_smartContainer.setData(RWT.CUSTOM_VARIANT, getSmartfieldDisabledVariant());
    }

    updateBrowseIconVariant(b, getScoutObject().getIconId());
  }

  @Override
  protected void setFieldEnabled(Control field, boolean enabled) {
    // nop
  }

  private void updateBrowseIconVariant(boolean enabled, String iconId) {
    if (StringUtility.isNullOrEmpty(iconId)) {
      return;
    }

    String variant = iconId;
    if (!enabled) {
      variant = iconId + "-disabled";
    }
    //Unfortunately, composites don't know :disabled states so we have to create our own
    m_browseIconContainer.setData(RWT.CUSTOM_VARIANT, variant);
  }

  protected void setIconIdFromScout(String s) {
    updateBrowseIconVariant(getScoutObject().isEnabled(), s);
  }

  @Override
  protected void setBackgroundFromScout(String scoutColor) {
    setBackgroundFromScout(scoutColor, m_smartContainer);
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (name.equals(ISmartField.PROP_ICON_ID)) {
      setIconIdFromScout((String) newValue);
    }
  }

  protected void requestProposalSupportFromUi(final String text, final boolean selectCurrentValue) {
    Runnable t = new Runnable() {
      @Override
      public void run() {
        getScoutObject().getUIFacade().openProposalFromUI(text, selectCurrentValue);
      }
    };
    getUiEnvironment().invokeScoutLater(t, 0);
  }

  private class P_FieldSelectionListener extends MouseAdapter {
    private static final long serialVersionUID = 1L;

    @Override
    public void mouseUp(MouseEvent e) {
      if (!getScoutObject().isEnabled()) {
        return;
      }

      requestProposalSupportFromUi(ISmartField.BROWSE_ALL_TEXT, true);
    }
  }

}
