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

import org.eclipse.rap.rwt.RWT;
import org.eclipse.scout.commons.TypeCastUtility;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.ISearchForm;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBox;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.ui.rap.form.fields.groupbox.RwtScoutGroupBox;
import org.eclipse.scout.rt.ui.rap.keystroke.RwtKeyStroke;
import org.eclipse.scout.rt.ui.rap.util.RwtLayoutUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
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
  private boolean m_selected;
  private boolean m_groupBoxBuilt;
  private boolean m_lazyBuildEnabled;
  private Button m_tabButton;

  private RwtScoutTabItem m_nextTabItem = null;
  private RwtScoutTabItem m_previousTabItem = null;

  public RwtScoutTabItem(ITabBox scoutParentObject, Composite tabboxButtonbar, Composite tabboxContainer, String variantInActive, String variantActive, String variantInActiveMarked, String variantActiveMarked) {
    m_scoutParentObject = scoutParentObject;
    m_tabboxButtonbar = tabboxButtonbar;
    m_tabboxContainer = tabboxContainer;
    m_variantInActive = variantInActive;
    m_variantActive = variantActive;
    m_variantInActiveMarked = variantInActiveMarked;
    m_variantActiveMarked = variantActiveMarked;
    setLazyBuildEnabled(true);
  }

  @Override
  protected void initializeUi(Composite parent) {
    createTabButton();
    createTabItem();
  }

  protected void createTabButton() {
    m_tabButton = getUiEnvironment().getFormToolkit().createButton(m_tabboxButtonbar, "", SWT.NONE);

    m_tabButton.setData(RWT.CUSTOM_VARIANT, m_variantInActive);
    m_tabButton.addSelectionListener(new SelectionAdapter() {
      private static final long serialVersionUID = 1L;

      @Override
      public void widgetSelected(SelectionEvent e) {
        handleUiSelection();
        select();
      }
    });
    registerCompositeOnWidget(m_tabButton, this);

    getUiEnvironment().addKeyStroke(m_tabButton, new P_KeyListener(SWT.ARROW_LEFT), false);
    getUiEnvironment().addKeyStroke(m_tabButton, new P_KeyListener(SWT.ARROW_RIGHT), false);

    RowData rowData = new RowData();
    m_tabButton.setLayoutData(rowData);
  }

  protected void createTabItem() {
    m_tabItem = getUiEnvironment().getFormToolkit().createComposite(m_tabboxContainer, SWT.NONE);
    m_tabItem.setData(RWT.CUSTOM_VARIANT, m_variantInActive);
    m_tabItem.setLayout(new FillLayout());
    if (!isLazyBuildEnabled() || isSelected()) {
      buildGroupbox();
    }
    else {
      //A container is necessary otherwise isUiDisposed will return true
      setUiContainer(m_tabItem);
    }
  }

  public void recreateTabButton() {
    disposeTabButton();
    createTabButton();

    //Reinit properties
    attachScout();
    if (isSelected()) {
      select();
    }
    else {
      unselect();
    }
  }

  protected void buildGroupbox() {
    if (isGroupBoxBuilt()) {
      return;
    }

    super.initializeUi(m_tabItem);
    m_groupBoxBuilt = true;
    if (isCreated()) {
      // m_tabItem.layout(true, true) is not sufficient since the groupbox may be bigger than the previous one, thus the dialog may have to adjust its size
      RwtLayoutUtility.invalidateLayout(getUiEnvironment(), m_tabItem);
    }
  }

  public boolean isGroupBoxBuilt() {
    return m_groupBoxBuilt;
  }

  @Override
  protected void disposeImpl() {
    super.disposeImpl();
    disposeTabButton();
    disposeTabItem();
  }

  protected void disposeTabButton() {
    if (m_tabButton != null && !m_tabButton.isDisposed()) {
      getUiEnvironment().removeKeyStrokes(m_tabButton);
      m_tabButton.dispose();
      m_tabButton = null;
    }
  }

  protected void disposeTabItem() {
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

  @Override
  protected void detachScout() {
    super.detachScout();
    getUiEnvironment().removeKeyStrokes(m_tabButton);
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

  private void setButtonInactive() {
    m_selected = false;
    if (!isValidWidget(m_tabButton)) {
      return;
    }
    Object oldVariant = m_tabButton.getData(RWT.CUSTOM_VARIANT);
    if (oldVariant == m_variantActiveMarked || oldVariant == m_variantInActiveMarked) {
      m_tabButton.setData(RWT.CUSTOM_VARIANT, m_variantInActiveMarked);
    }
    else {
      m_tabButton.setData(RWT.CUSTOM_VARIANT, m_variantInActive);
    }
  }

  private void setButtonActive() {
    if (!isValidWidget(m_tabButton)) {
      return;
    }
    Object oldVariant = m_tabButton.getData(RWT.CUSTOM_VARIANT);
    if (oldVariant == m_variantActiveMarked || oldVariant == m_variantInActiveMarked) {
      m_tabButton.setData(RWT.CUSTOM_VARIANT, m_variantActiveMarked);
    }
    else {
      m_tabButton.setData(RWT.CUSTOM_VARIANT, m_variantActive);
    }
    m_selected = true;
  }

  private void setButtonMarker(Button tabButton, boolean marked) {
    if (!isValidWidget(tabButton)) {
      return;
    }
    Object oldVariant = tabButton.getData(RWT.CUSTOM_VARIANT);
    if (marked) {
      if (oldVariant == m_variantActive) {
        tabButton.setData(RWT.CUSTOM_VARIANT, m_variantActiveMarked);
      }
      else if (oldVariant == m_variantInActive) {
        tabButton.setData(RWT.CUSTOM_VARIANT, m_variantInActiveMarked);
      }
      else {
        // already marked
      }
    }
    else {
      if (oldVariant == m_variantActiveMarked) {
        tabButton.setData(RWT.CUSTOM_VARIANT, m_variantActive);
      }
      else if (oldVariant == m_variantInActiveMarked) {
        tabButton.setData(RWT.CUSTOM_VARIANT, m_variantInActive);
      }
      else {
        // already non-marked
      }
    }
  }

  protected void handleUiSelectionChanged() {
    handleUiSelection();
    select();
  }

  protected void handleUiSelection() {
    //notify Scout
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
    if (getScoutObject().getParentField() instanceof ITabBox) {
      if (((ITabBox) getScoutObject().getParentField()).getMarkStrategy() == ITabBox.MARK_STRATEGY_SAVE_NEEDED) {
        setButtonMarker(m_tabButton, b);
      }
    }
    else if (getScoutObject().getForm() instanceof ISearchForm) {
      setButtonMarker(m_tabButton, b);
    }
  }

  protected void setEmptyFromScout(boolean b) {
    if (getScoutObject().getParentField() instanceof ITabBox) {
      if (((ITabBox) getScoutObject().getParentField()).getMarkStrategy() == ITabBox.MARK_STRATEGY_EMPTY) {
        setButtonMarker(m_tabButton, !b);
      }
    }
    else if (!(getScoutObject().getForm() instanceof ISearchForm)) {
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

  public void unselect() {
    setButtonInactive();
  }

  public void select() {
    setButtonActive();
    buildGroupbox();
  }

  protected boolean isSelected() {
    return m_selected;
  }

  protected boolean isLazyBuildEnabled() {
    return m_lazyBuildEnabled;
  }

  /**
   * @param lazyBuildEnabled
   *          true to lazily build the content of a tab item, false if not. Default is true.
   */
  public void setLazyBuildEnabled(boolean lazyBuildEnabled) {
    m_lazyBuildEnabled = lazyBuildEnabled;
  }

  protected RwtScoutTabItem getNextTabItem() {
    return m_nextTabItem;
  }

  protected void setNextTabItem(RwtScoutTabItem nextTabItem) {
    m_nextTabItem = nextTabItem;
  }

  protected RwtScoutTabItem getPreviousTabItem() {
    return m_previousTabItem;
  }

  protected void setPreviousTabItem(RwtScoutTabItem previousTabItem) {
    m_previousTabItem = previousTabItem;
  }

  private class P_KeyListener extends RwtKeyStroke {
    public P_KeyListener(int keyCode) {
      super(keyCode);
    }

    public P_KeyListener(int keyCode, int stateMask) {
      super(keyCode, stateMask);
    }

    private void handleKeyPressed(RwtScoutTabItem tabItem) {
      if (tabItem == null) {
        return;
      }
      tabItem.getTabButton().setFocus();
      tabItem.handleUiSelectionChanged();
    }

    @Override
    public void handleUiAction(Event e) {
      switch (e.keyCode) {
        case SWT.ARROW_LEFT:
          handleKeyPressed(getPreviousTabItem());
          e.doit = false;
          break;
        case SWT.ARROW_RIGHT:
          handleKeyPressed(getNextTabItem());
          e.doit = false;
          break;
        default:
          break;
      }
    }
  }
}
