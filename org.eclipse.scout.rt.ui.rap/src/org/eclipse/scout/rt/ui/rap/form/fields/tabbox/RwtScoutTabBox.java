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

import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.scout.commons.OptimisticLock;
import org.eclipse.scout.commons.RunnableWithData;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBox;
import org.eclipse.scout.rt.ui.rap.form.fields.RwtScoutFieldComposite;
import org.eclipse.scout.rt.ui.rap.util.RwtLayoutUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class RwtScoutTabBox extends RwtScoutFieldComposite<ITabBox> implements IRwtScoutTabBox {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(RwtScoutTabBox.class);

  private HashMap<Composite, RwtScoutTabItem> m_tabs;
  private Listener m_uiTabFocusListener;
  private P_TabListener m_tabListener = new P_TabListener();
  private OptimisticLock m_selectedTabLock;
  private OptimisticLock m_rebuildItemsLock = new OptimisticLock();

  private Composite m_tabboxButtonbar;
  private StackLayout m_stackLayout;
  private Composite m_tabboxContainer;

  private RwtScoutTabItem m_focusedItem;

  public RwtScoutTabBox() {
    m_selectedTabLock = new OptimisticLock();
  }

  @Override
  protected void initializeUi(Composite parent) {
    Composite container = getUiEnvironment().getFormToolkit().createComposite(parent, SWT.TOP);
    container.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_TABBOX_CONTAINER);

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
    if (m_uiTabFocusListener == null) {
      m_uiTabFocusListener = new P_RwtFocusListener();
    }
    m_tabboxContainer.addListener(SWT.FocusIn, m_uiTabFocusListener);
    m_tabboxContainer.addListener(SWT.FocusOut, m_uiTabFocusListener);
  }

  protected Composite createTabboxButtonBar(Composite parent) {
    Composite tabboxButtonBar = getUiEnvironment().getFormToolkit().createComposite(parent);
    tabboxButtonBar.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_TABBOX_CONTAINER);
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
      LinkedList<RwtScoutTabItem> tabList = new LinkedList<RwtScoutTabItem>();
      for (IGroupBox box : getScoutObject().getGroupBoxes()) {
        if (box.isVisible()) {
          RwtScoutTabItem item = new RwtScoutTabItem(getScoutObject(), m_tabboxButtonbar, m_tabboxContainer, VARIANT_TABBOX_BUTTON, VARIANT_TABBOX_BUTTON_ACTIVE, VARIANT_TABBOX_BUTTON_MARKED, VARIANT_TABBOX_BUTTON_ACTIVE_MARKED);
          item.createUiField(getUiField(), box, getUiEnvironment());
          m_tabs.put(item.getTabItem(), item);
          tabList.add(item);
        }
      }

      // link the tab items together.
      if (tabList.size() > 0) {
        RwtScoutTabItem previousItem = null;
        for (RwtScoutTabItem curItem : tabList) {
          if (previousItem != null) {
            curItem.setPreviousTabItem(previousItem);
            previousItem.setNextTabItem(curItem);
          }
          previousItem = curItem;
        }

        /* the previous item of the first one is linked to the last one 
         * the next item of the last one is linked to the first one.
         */
        RwtScoutTabItem firstItem = tabList.getFirst();
        RwtScoutTabItem lastItem = tabList.getLast();
        firstItem.setPreviousTabItem(lastItem);
        lastItem.setNextTabItem(firstItem);
      }
      
      setSelectedTabFromScout();
    }
    finally {
      getUiContainer().setRedraw(true);
      m_rebuildItemsLock.release();
    }
  }

  @Override
  public Composite getUiField() {
    return (Composite) super.getUiField();
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
      Composite foundItem = null;
      for (Map.Entry<Composite, RwtScoutTabItem> e : m_tabs.entrySet()) {
        IGroupBox test = e.getValue().getScoutObject();
        if (test == selectedTab) {
          foundItem = e.getKey();
          break;
        }
      }
      if (foundItem != null) {
        m_stackLayout.topControl = foundItem;
        m_tabboxContainer.layout();

        m_focusedItem = m_tabs.get(foundItem);
        m_focusedItem.setUiFocus();
        m_tabboxButtonbar.layout();
      }
    }
    finally {
      m_selectedTabLock.release();
    }
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

  private class P_RwtFocusListener implements Listener {
    private static final long serialVersionUID = 1L;

    @Override
    public void handleEvent(Event event) {
      RwtScoutTabItem item = m_tabs.get(m_stackLayout.topControl);
      switch (event.type) {
        case SWT.FocusOut:
          if (m_focusedItem != null) {
            m_focusedItem = null;
          }
          break;
        case SWT.FocusIn:
          if (item.setUiFocus()) {
            m_focusedItem = item;
          }
          break;
        default:
          break;
      }
    }
  }

  private class P_TabListener implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if (evt.getPropertyName().equals(IGroupBox.PROP_VISIBLE)) {
        try {
          if (m_rebuildItemsLock.acquire()) {
            RunnableWithData t = new RunnableWithData() {
              @Override
              public void run() {
                rebuildItems();
              }
            };
            getUiEnvironment().invokeUiLater(t);
          }
        }
        finally {
          m_rebuildItemsLock.release();
        }
      }
    }
  }
}
