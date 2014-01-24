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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.scout.commons.OptimisticLock;
import org.eclipse.scout.commons.RunnableWithData;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBox;
import org.eclipse.scout.rt.ui.rap.form.fields.RwtScoutFieldComposite;
import org.eclipse.scout.rt.ui.rap.util.RwtLayoutUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;

public class RwtScoutTabBox extends RwtScoutFieldComposite<ITabBox> implements IRwtScoutTabBox {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(RwtScoutTabBox.class);

  private HashMap<Composite, RwtScoutTabItem> m_tabs;
  private LinkedList<RwtScoutTabItem> m_tabList;
  private P_TabListener m_tabListener = new P_TabListener();
  private OptimisticLock m_selectedTabLock;
  private OptimisticLock m_rebuildItemsLock = new OptimisticLock();

  private Composite m_tabboxButtonbar;
  private StackLayout m_stackLayout;
  private Composite m_tabboxContainer;

  private RwtScoutTabItem m_selectedItem;

  public RwtScoutTabBox() {
    m_selectedTabLock = new OptimisticLock();
  }

  @Override
  protected void initializeUi(Composite parent) {
    Composite container = getUiEnvironment().getFormToolkit().createComposite(parent, SWT.TOP);
    container.setData(RWT.CUSTOM_VARIANT, VARIANT_TABBOX_CONTAINER);

    m_tabboxButtonbar = createTabboxButtonBar(container);
    m_tabboxContainer = getUiEnvironment().getFormToolkit().createComposite(container, SWT.NONE);

    //layout
    container.setLayout(RwtLayoutUtility.createGridLayoutNoSpacing(1, false));

    GridData tabboxButtonBarLayoutdata = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.GRAB_HORIZONTAL | GridData.VERTICAL_ALIGN_END);
    m_tabboxButtonbar.setLayoutData(tabboxButtonBarLayoutdata);

    m_tabboxContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
    m_stackLayout = new StackLayout();
    m_tabboxContainer.setLayout(m_stackLayout);

    setUiLabel(null);
    setUiField(m_tabboxContainer);
    setUiContainer(container);
    // set up tabs
    for (IGroupBox box : getScoutObject().getGroupBoxes()) {
      box.addPropertyChangeListener(m_tabListener);
    }
    rebuildItems();
  }

  protected Composite createTabboxButtonBar(Composite parent) {
    Composite tabboxButtonBar = getUiEnvironment().getFormToolkit().createComposite(parent);
    tabboxButtonBar.setData(RWT.CUSTOM_VARIANT, VARIANT_TABBOX_CONTAINER);
    //layout
    RowLayout layout = new RowLayout(SWT.HORIZONTAL);
    layout.marginBottom = 0;
    layout.marginTop = 5;
    layout.marginLeft = 10;
    tabboxButtonBar.setLayout(layout);

    return tabboxButtonBar;
  }

  @Override
  protected void disposeImpl() {
    super.disposeImpl();
    if (m_tabboxButtonbar != null && !m_tabboxButtonbar.isDisposed()) {
      m_tabboxButtonbar.dispose();
      m_tabboxButtonbar = null;
    }
  }

  protected void rebuildItems() {
    try {
      m_rebuildItemsLock.acquire();
      getUiContainer().setRedraw(false);
      // remove old
      if (m_tabs != null) {
        for (RwtScoutTabItem item : m_tabs.values()) {
          item.dispose();
        }
      }
      m_tabs = new HashMap<Composite, RwtScoutTabItem>();
      m_tabList = new LinkedList<RwtScoutTabItem>();
      for (IGroupBox box : getScoutObject().getGroupBoxes()) {
        if (box.isVisible()) {
          RwtScoutTabItem item = createUiTabItem(box);
          m_tabs.put(item.getTabItem(), item);
          m_tabList.addLast(item);
        }
      }

      linkTabItems();
      setSelectedTabFromScout();
    }
    finally {
      getUiContainer().setRedraw(true);
      m_rebuildItemsLock.release();
    }
  }

  protected RwtScoutTabItem createUiTabItem(IGroupBox groupBox) {
    RwtScoutTabItem item = createUiTabItem();
    item.createUiField(getUiField(), groupBox, getUiEnvironment());
    return item;
  }

  private void linkTabItems() {
    if (m_tabList == null || m_tabList.size() == 0) {
      return;
    }

    RwtScoutTabItem previousItem = null;
    for (RwtScoutTabItem curItem : m_tabList) {
      if (previousItem != null) {
        curItem.setPreviousTabItem(previousItem);
        previousItem.setNextTabItem(curItem);
      }
      previousItem = curItem;
    }

    /* the previous item of the first one is linked to the last one
     * the next item of the last one is linked to the first one.
     */
    RwtScoutTabItem firstItem = m_tabList.getFirst();
    RwtScoutTabItem lastItem = m_tabList.getLast();
    firstItem.setPreviousTabItem(lastItem);
    lastItem.setNextTabItem(firstItem);
  }

  protected RwtScoutTabItem createUiTabItem() {
    return new RwtScoutTabItem(getScoutObject(), getTabboxButtonbar(), getTabboxContainer(), VARIANT_TABBOX_BUTTON, VARIANT_TABBOX_BUTTON_ACTIVE, VARIANT_TABBOX_BUTTON_MARKED, VARIANT_TABBOX_BUTTON_ACTIVE_MARKED);
  }

  @Override
  public Composite getUiField() {
    return (Composite) super.getUiField();
  }

  public Composite getTabboxButtonbar() {
    return m_tabboxButtonbar;
  }

  public Composite getTabboxContainer() {
    return m_tabboxContainer;
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    setSelectedTabFromScout();
  }

  @Override
  protected void detachScout() {
    for (IGroupBox b : getScoutObject().getGroupBoxes()) {
      b.removePropertyChangeListener(m_tabListener);
    }
    super.detachScout();
  }

  @Override
  protected void setEnabledFromScout(boolean b) {
    // void here
  }

  /**
   * scout settings
   */
  protected void setSelectedTabFromScout() {
    try {
      m_selectedTabLock.acquire();
      //
      IGroupBox selectedTab = getScoutObject().getSelectedTab();
      RwtScoutTabItem foundItem = getTabItem(selectedTab);
      if (foundItem != null && foundItem.getTabItem() != null) {
        m_stackLayout.topControl = foundItem.getTabItem();
        m_tabboxContainer.layout();

        m_selectedItem = foundItem;
        for (RwtScoutTabItem item : m_tabList) {
          if (item != m_selectedItem) {
            item.unselect();
          }
        }
        m_selectedItem.select();
        m_tabboxButtonbar.layout();
      }
    }
    finally {
      m_selectedTabLock.release();
    }
  }

  protected void setGroupBoxVisibleFromScout(final IGroupBox groupBox) {
    try {
      if (m_rebuildItemsLock.acquire()) {
        RunnableWithData t = new RunnableWithData() {
          @Override
          public void run() {
            if (groupBox.isVisible()) {
              showGroupBox(groupBox);
            }
            else {
              hideGroupBox(groupBox);
            }
          }
        };
        getUiEnvironment().invokeUiLater(t);
      }
    }
    finally {
      m_rebuildItemsLock.release();
    }
  }

  protected void showGroupBox(IGroupBox groupBox) {
    getUiContainer().setRedraw(false);
    try {
      RwtScoutTabItem item = getTabItem(groupBox);
      if (item == null) {
        item = createUiTabItem(groupBox);
        m_tabs.put(item.getTabItem(), item);
        int index = getVisibleIndex(groupBox);
        m_tabList.add(index, item);
        linkTabItems();

        //recreate buttons to order them correctly (necessary for the row layout)
        for (int i = index + 1; i < m_tabList.size(); i++) {
          m_tabList.get(i).recreateTabButton();
        }

        m_tabboxButtonbar.layout();
      }
    }
    finally {
      getUiContainer().setRedraw(true);
    }
  }

  protected void hideGroupBox(IGroupBox groupBox) {
    getUiContainer().setRedraw(false);
    try {
      RwtScoutTabItem item = getTabItem(groupBox);
      if (item != null) {
        m_tabs.remove(item.getTabItem());
        m_tabList.remove(item);
        linkTabItems();
        item.dispose();

        //recreate buttons otherwise active state is not properly visible
        for (int i = 0; i < m_tabList.size(); i++) {
          m_tabList.get(i).recreateTabButton();
        }

        m_tabboxButtonbar.layout();
      }
    }
    finally {
      getUiContainer().setRedraw(true);
    }
  }

  protected RwtScoutTabItem getTabItem(IGroupBox groupBox) {
    for (Map.Entry<Composite, RwtScoutTabItem> e : m_tabs.entrySet()) {
      IGroupBox test = e.getValue().getScoutObject();
      if (test == groupBox) {
        return e.getValue();
      }
    }
    return null;
  }

  @Override
  protected void updateKeyStrokesFromScout() {
    // nop because the child fields also register the keystrokes of theirs parents
  }

  private int getVisibleIndex(IGroupBox groupBox) {
    int result = -1;
    if (groupBox == null || groupBox.getParentField() == null) {
      return result;
    }
    for (IFormField box : groupBox.getParentField().getFields()) {
      if (box.isVisible()) {
        result++;
        if (box == groupBox) {
          break;
        }
      }
    }
    return result;
  }

  /**
   * scout property observer
   */
  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (name.equals(ITabBox.PROP_SELECTED_TAB)) {
      setSelectedTabFromScout();
    }
  }

  private class P_TabListener implements PropertyChangeListener {
    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
      if (evt.getPropertyName().equals(IGroupBox.PROP_VISIBLE)) {
        IGroupBox groupBox = (IGroupBox) evt.getSource();
        setGroupBoxVisibleFromScout(groupBox);
      }
    }
  }

}
