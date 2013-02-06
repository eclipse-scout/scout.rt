/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.window.desktop.toolbar;

import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.tool.IToolButton;
import org.eclipse.scout.rt.ui.rap.basic.RwtScoutComposite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class RwtScoutToolButton extends RwtScoutComposite<IAction> implements IRwtScoutToolButtonForPatch {

  private final boolean m_iconVisible;
  private final boolean m_textVisible;
  private String m_variantInActive;
  private String m_variantActive;

  public RwtScoutToolButton(boolean textVisible, boolean iconVisible, String variantInActive, String variantActive) {
    m_textVisible = textVisible;
    m_iconVisible = iconVisible;
    m_variantInActive = variantInActive;
    m_variantActive = variantActive;
  }

  @Override
  protected void initializeUi(Composite parent) {
    final Button tabButton = getUiEnvironment().getFormToolkit().createButton(parent, "", SWT.TOGGLE);
    tabButton.setData(WidgetUtil.CUSTOM_VARIANT, m_variantInActive);
    setUiField(tabButton);
    tabButton.addSelectionListener(new SelectionAdapter() {
      private static final long serialVersionUID = 1L;

      @Override
      public void widgetSelected(SelectionEvent e) {
        handleUiSelection();
      }
    });
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    updateIconFromScout();
    updateSelectionFromScout();
    updateTextFromScout();
    updateEnabledFormScout();
    updateVisibleFromScout();
  }

  @Override
  public Button getUiField() {
    return (Button) super.getUiField();
  }

  protected void handleUiSelection() {
    //notify Scout
    final boolean selected = getUiField().getSelection();
    Runnable t = new Runnable() {
      @Override
      public void run() {
        getScoutObject().getUIFacade().setSelectedFromUI(selected);
        getScoutObject().getUIFacade().fireActionFromUI();
      }
    };
    getUiEnvironment().invokeScoutLater(t, 0);
  }

  protected void updateIconFromScout() {
//    if (m_iconVisible) {
//      getUiField().setImage(getUiEnvironment().getIcon(getScoutObject().getIconId()));
//    }
  }

  protected void updateTextFromScout() {
    if (m_textVisible) {
      getUiField().setText(getScoutObject().getText());
    }
  }

  protected void updateSelectionFromScout() {
    Button uiField = getUiField();
    boolean isSelected = getScoutObject().isSelected();
    setCustomVariants(uiField, isSelected);
    uiField.setSelection(isSelected);
  }

  private void setCustomVariants(Button uiField, boolean isSelected) {
    if (isSelected) {
      uiField.setData(WidgetUtil.CUSTOM_VARIANT, m_variantActive);
    }
    else {
      uiField.setData(WidgetUtil.CUSTOM_VARIANT, m_variantInActive);
    }
  }

  private void updateEnabledFormScout() {
    getUiField().setEnabled(getScoutObject().isEnabled());
  }

  private void updateVisibleFromScout() {
    getUiField().setVisible(getScoutObject().isVisible());
    // Instruct layout to exclude button when invisible
    RowData data = new RowData();
    Object o = getUiField().getLayoutData();
    if (o instanceof RowData) {
      data = (RowData) o;
    }
    data.exclude = !getScoutObject().isVisible();
    getUiField().setLayoutData(data);
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    if (IToolButton.PROP_ICON_ID.equals(name)) {
      updateIconFromScout();
    }
    else if (IToolButton.PROP_SELECTED.equals(name)) {
      updateSelectionFromScout();
    }
    else if (IToolButton.PROP_TEXT.equals(name)) {
      updateTextFromScout();
    }
    else if (IToolButton.PROP_ENABLED.equals(name)) {
      updateEnabledFormScout();

    }
    else if (IToolButton.PROP_VISIBLE.equals(name)) {
      updateVisibleFromScout();
    }
  }

}
