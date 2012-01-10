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
package org.eclipse.scout.rt.ui.rap.form.fields.tabbox;

import org.eclipse.rwt.lifecycle.UICallBack;
import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.scout.commons.TypeCastUtility;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.ISearchForm;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBox;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.ui.rap.form.fields.groupbox.RwtScoutGroupBox;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;

public class RwtScoutTabItem extends RwtScoutGroupBox implements IRwtScoutTabItem {

  private final ITabBox m_scoutParentObject;

  private final Composite m_tabboxButtonbar;
  private final Composite m_tabboxContainer;

  private final String m_variantInActive;
  private final String m_variantActive;
  private final String m_variantInActiveMarked;
  private final String m_variantActiveMarked;

  private Composite m_tabItem;
  private boolean m_uiFocus;
  private Button m_tabButton;

  public RwtScoutTabItem(ITabBox scoutParentObject, Composite tabboxButtonbar, Composite tabboxContainer, String variantInActive, String variantActive, String variantInActiveMarked, String variantActiveMarked) {
    m_scoutParentObject = scoutParentObject;
    m_tabboxButtonbar = tabboxButtonbar;
    m_tabboxContainer = tabboxContainer;
    m_variantInActive = variantInActive;
    m_variantActive = variantActive;
    m_variantInActiveMarked = variantInActiveMarked;
    m_variantActiveMarked = variantActiveMarked;
  }

  @Override
  protected void initializeUi(Composite parent) {
    m_tabButton = getUiEnvironment().getFormToolkit().createButton(m_tabboxButtonbar, "", SWT.NONE);

    m_tabButton.setData(WidgetUtil.CUSTOM_VARIANT, m_variantInActive);
    m_tabButton.addSelectionListener(new SelectionAdapter() {
      private static final long serialVersionUID = 1L;

      @Override
      public void widgetSelected(SelectionEvent e) {
        handleUiSelection();
        setActiveButton(m_tabButton);
      }
    });
    RowData rowData = new RowData();
    m_tabButton.setLayoutData(rowData);

    m_tabItem = getUiEnvironment().getFormToolkit().createComposite(m_tabboxContainer, SWT.NONE);
    m_tabItem.setData(WidgetUtil.CUSTOM_VARIANT, m_variantInActive);
    m_tabItem.setLayout(new FillLayout());

    super.initializeUi(m_tabItem);
  }

  @Override
  protected void disposeImpl() {
    super.disposeImpl();
    if (m_tabButton != null && !m_tabButton.isDisposed()) {
      m_tabButton.dispose();
      m_tabButton = null;
    }
    if (m_tabItem != null && !m_tabItem.isDisposed()) {
      m_tabItem.dispose();
      m_tabItem = null;
    }
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    IGroupBox scoutField = getScoutObject();
    if (scoutField != null) {
      setEmptyFromScout(scoutField.isEmpty());
    }
  }

  private boolean isValidWidget(Widget w) {
    if (w == null || w.isDisposed()) {
      return false;
    }
    if (w instanceof Control) {
      Control c = (Control) w;
      if (c.getParent() == null || c.getParent().isDisposed()) {
        return false;
      }
    }
    return true;
  }

  private boolean setActiveButton(Button tabButton) {
    if (!isValidWidget(tabButton)) {
      return false;
    }
    Control[] children = tabButton.getParent().getChildren();
    for (Control child : children) {
      makeButtonInActive(child);
    }
    makeButtonActive(tabButton);
    return true;
  }

  private void makeButtonInActive(Control tabButton) {
    if (!isValidWidget(tabButton)) {
      return;
    }
    Object oldVariant = tabButton.getData(WidgetUtil.CUSTOM_VARIANT);
    if (oldVariant == m_variantActiveMarked || oldVariant == m_variantInActiveMarked) {
      tabButton.setData(WidgetUtil.CUSTOM_VARIANT, m_variantInActiveMarked);
    }
    else {
      tabButton.setData(WidgetUtil.CUSTOM_VARIANT, m_variantInActive);
    }
  }

  private void makeButtonActive(Control tabButton) {
    if (!isValidWidget(tabButton)) {
      return;
    }
    Object oldVariant = tabButton.getData(WidgetUtil.CUSTOM_VARIANT);
    if (oldVariant == m_variantActiveMarked || oldVariant == m_variantInActiveMarked) {
      tabButton.setData(WidgetUtil.CUSTOM_VARIANT, m_variantActiveMarked);
    }
    else {
      tabButton.setData(WidgetUtil.CUSTOM_VARIANT, m_variantActive);
    }
  }

  private void setButtonMarker(Button tabButton, boolean marked) {
    if (!isValidWidget(tabButton)) {
      return;
    }
    Object oldVariant = tabButton.getData(WidgetUtil.CUSTOM_VARIANT);
    if (marked) {
      if (oldVariant == m_variantActive) {
        tabButton.setData(WidgetUtil.CUSTOM_VARIANT, m_variantActiveMarked);
      }
      else if (oldVariant == m_variantInActive) {
        tabButton.setData(WidgetUtil.CUSTOM_VARIANT, m_variantInActiveMarked);
      }
      else {
        // already marked
      }
    }
    else {
      if (oldVariant == m_variantActiveMarked) {
    tabButton.setData(WidgetUtil.CUSTOM_VARIANT, m_variantActive);
  }
      else if (oldVariant == m_variantInActiveMarked) {
        tabButton.setData(WidgetUtil.CUSTOM_VARIANT, m_variantInActive);
      }
      else {
        // already non-marked
      }
    }
  }

  protected void handleUiSelection() {
    //notify Scout
    UICallBack.activate(m_scoutParentObject.getClass().getName() + m_scoutParentObject.hashCode());
    Runnable t = new Runnable() {
      @Override
      public void run() {
        m_scoutParentObject.getUIFacade().setSelectedTabFromUI(getScoutObject());
      }
    };
    getUiEnvironment().invokeScoutLater(t, 0);
  }

  public Button getTabButton() {
    return m_tabButton;
  }

  protected Composite getTabItem() {
    return m_tabItem;
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (name.equals(IFormField.PROP_EMPTY)) {
      setEmptyFromScout(TypeCastUtility.castValue(newValue, boolean.class));
    }
  }

  @Override
  protected void setSaveNeededFromScout(boolean b) {
    if (getScoutObject().getForm() instanceof ISearchForm) {
      setButtonMarker(m_tabButton, b);
    }
  }

  protected void setEmptyFromScout(boolean b) {
    if (!(getScoutObject().getForm() instanceof ISearchForm)) {
      setButtonMarker(m_tabButton, !b);
    }
  }

  @Override
  protected void setLabelFromScout(String s) {
    String label = s;
    if (label == null) {
      label = "";
    }
    getTabButton().setText(label);
  }

  @Override
  protected void setFontFromScout(FontSpec scoutFont) {
    super.setFontFromScout(scoutFont);
    m_tabItem.setFont(getUiEnvironment().getFont(scoutFont, m_tabItem.getFont()));
  }

  public boolean setUiFocus() {
    return setActiveButton(m_tabButton);
  }
}
