/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui.form.fields.tabbox;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.extension.ui.action.tree.MoveActionNodesHandler;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.tabbox.ITabBoxExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.tabbox.TabBoxChains.TabBoxTabSelectedChain;
import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.MenuUtility;
import org.eclipse.scout.rt.client.ui.action.menu.root.IFormFieldContextMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.internal.FormFieldContextMenu;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractCompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.internal.TabBoxGrid;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ClassId("14555c41-2d65-414a-94b1-d4328cbd818c")
public abstract class AbstractTabBox extends AbstractCompositeField implements ITabBox {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractTabBox.class);

  private ITabBoxUIFacade m_uiFacade;
  private TabBoxGrid m_grid;

  public AbstractTabBox() {
    this(true);
  }

  public AbstractTabBox(boolean callInitializer) {
    super(callInitializer);
  }

  /*
   * Configuration
   */

  @Override
  protected boolean getConfiguredGridUseUiHeight() {
    return true;
  }

  @Override
  protected int getConfiguredGridW() {
    return FULL_WIDTH;
  }

  @ConfigOperation
  @Order(70)
  protected void execTabSelected(IGroupBox selectedBox) {
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  protected int getConfiguredMarkStrategy() {
    return MARK_STRATEGY_EMPTY;
  }

  /**
   * Configures the appearance of the tabs in the tab box. If the style {@link ITabBox#TAB_AREA_STYLE_SPREAD_EVEN} is
   * chosen, the tabs will occupy the whole available space and will push any defined menu into the overflow menu. To
   * counteract that, make the menu not stackable (see {@link AbstractMenu#getConfiguredStackable}).
   *
   * @return one of the following {@link ITabBox#TAB_AREA_STYLE_DEFAULT}, {@link ITabBox#TAB_AREA_STYLE_SPREAD_EVEN}.
   */
  @ConfigProperty(ConfigProperty.STRING)
  protected String getConfiguredTabAreaStyle() {
    return TAB_AREA_STYLE_DEFAULT;
  }

  @Override
  public List<? extends IWidget> getChildren() {
    return CollectionUtility.flatten(super.getChildren(), getMenus());
  }

  @Override
  public int getMarkStrategy() {
    return propertySupport.getPropertyInt(PROP_MARK_STRATEGY);
  }

  @Override
  public void setMarkStrategy(int markStrategy) {
    propertySupport.setPropertyInt(PROP_MARK_STRATEGY, markStrategy);
  }

  @Override
  public String getTabAreaStyle() {
    return propertySupport.getPropertyString(PROP_TAB_AREA_STYLE);
  }

  @Override
  public void setTabAreaStyle(String tabAreaStyle) {
    propertySupport.setPropertyString(PROP_TAB_AREA_STYLE, tabAreaStyle);
  }

  @Override
  protected void initConfig() {
    m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(new P_UIFacade(), ModelContext.copyCurrent());
    m_grid = new TabBoxGrid();
    setMarkStrategy(getConfiguredMarkStrategy());
    setTabAreaStyle(getConfiguredTabAreaStyle());
    super.initConfig();
    initMenus();
  }

  @Override
  protected void initConfigInternal() {
    super.initConfigInternal();
    // add listener after init-config has been executed to ensure no events are fired during init-config.
    addPropertyChangeListener(PROP_SELECTED_TAB, e -> interceptTabSelected(getSelectedTab()));
  }

  private void initMenus() {
    List<Class<? extends IMenu>> declaredMenus = getDeclaredMenus();
    List<IMenu> contributedMenus = m_contributionHolder.getContributionsByClass(IMenu.class);
    OrderedCollection<IMenu> menus = new OrderedCollection<>();
    for (Class<? extends IMenu> menuClazz : declaredMenus) {
      try {
        menus.addOrdered(ConfigurationUtility.newInnerInstance(this, menuClazz));
      }
      catch (Exception e) {
        BEANS.get(ExceptionHandler.class).handle(new ProcessingException("error creating instance of class '" + menuClazz.getName() + "'.", e));
      }
    }
    menus.addAllOrdered(contributedMenus);
    try {
      injectMenusInternal(menus);
    }
    catch (Exception e) {
      LOG.error("Error occurred while dynamically contributing menus.", e);
    }
    new MoveActionNodesHandler<>(menus).moveModelObjects();
    setContextMenu(new FormFieldContextMenu<ITabBox>(this, menus.getOrderedList()));
  }

  /*
   * Runtime
   */

  @Override
  public void rebuildFieldGrid() {
    m_grid.validate(this);
    if (isInitConfigDone() && getForm() != null) {
      getForm().structureChanged(this);
    }
  }

  @Override
  public TabBoxGrid getFieldGrid() {
    return m_grid;
  }

  @Override
  protected void handleChildFieldVisibilityChanged() {
    // box is only visible when it has at least one visible item
    super.handleChildFieldVisibilityChanged();

    if (isInitConfigDone()) {
      rebuildFieldGrid();
    }

    if (hasVisibleFieldsInternal()) {
      setSelectedTab(findNewSelectedTab(getSelectedTab()));
    }
    else {
      setSelectedTab(null);
    }
  }

  protected IGroupBox findNewSelectedTab(final IGroupBox selectedBox) {
    if (selectedBox == null) {
      for (IGroupBox box : getGroupBoxes()) {
        if (box.isVisible()) {
          return box;
        }
      }
    }
    else if (!selectedBox.isVisible()) {
      int index = getFieldIndex(selectedBox);
      List<IGroupBox> boxes = getGroupBoxes();
      // next to right side
      for (int i = index + 1; i < boxes.size(); i++) {
        IGroupBox box = boxes.get(i);
        if (box.isVisible()) {
          return box;
        }
      }
      if (getSelectedTab() == selectedBox) {
        // next to left side
        for (int i = index - 1; i >= 0; i--) {
          IGroupBox box = boxes.get(i);
          if (box.isVisible()) {
            return box;
          }
        }
      }
    }
    return selectedBox;
  }

  @Override
  public List<IGroupBox> getGroupBoxes() {
    List<IGroupBox> result = new ArrayList<>();
    for (IFormField field : getFields()) {
      if (field instanceof IGroupBox) {
        result.add((IGroupBox) field);
      }
      else {
        LOG.warn("Tabboxes only allow instance of IGroupBox as inner fields. '{}' is not instance of IGroupBox!", field.getClass().getName());
      }
    }
    return result;
  }

  @Override
  public void setSelectedTab(IGroupBox box) {
    if (box == null || box.getParentField() == this) {
      propertySupport.setProperty(PROP_SELECTED_TAB, box);
    }
  }

  @Override
  public IGroupBox getSelectedTab() {
    return (IGroupBox) propertySupport.getProperty(PROP_SELECTED_TAB);
  }

  /**
   * Override this internal method only in order to make use of dynamic menus<br>
   * Used to add and/or remove menus<br>
   * To change the order or specify the insert position use {@link IMenu#setOrder(double)}.
   *
   * @param menus
   *          live and mutable collection of configured menus
   */
  protected void injectMenusInternal(OrderedCollection<IMenu> menus) {
  }

  protected void setContextMenu(IFormFieldContextMenu contextMenu) {
    propertySupport.setProperty(PROP_CONTEXT_MENU, contextMenu);
  }

  @Override
  public IFormFieldContextMenu getContextMenu() {
    return (IFormFieldContextMenu) propertySupport.getProperty(PROP_CONTEXT_MENU);
  }

  @Override
  public List<IMenu> getMenus() {
    return getContextMenu().getChildActions();
  }

  @Override
  public <T extends IMenu> T getMenuByClass(Class<T> menuType) {
    return MenuUtility.getMenuByClass(this, menuType);
  }

  protected List<Class<? extends IMenu>> getDeclaredMenus() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<IMenu>> filtered = ConfigurationUtility.filterClasses(dca, IMenu.class);
    return ConfigurationUtility.removeReplacedClasses(filtered);
  }

  @Override
  public ITabBoxUIFacade getUIFacade() {
    return m_uiFacade;
  }

  protected class P_UIFacade implements ITabBoxUIFacade {

    @Override
    public void setSelectedTabFromUI(IGroupBox box) {
      setSelectedTab(box);
    }
  }

  protected final void interceptTabSelected(IGroupBox selectedBox) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    TabBoxTabSelectedChain chain = new TabBoxTabSelectedChain(extensions);
    chain.execTabSelected(selectedBox);
  }

  protected static class LocalTabBoxExtension<OWNER extends AbstractTabBox> extends LocalCompositeFieldExtension<OWNER> implements ITabBoxExtension<OWNER> {

    public LocalTabBoxExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public void execTabSelected(TabBoxTabSelectedChain chain, IGroupBox selectedBox) {
      getOwner().execTabSelected(selectedBox);
    }
  }

  @Override
  protected ITabBoxExtension<? extends AbstractTabBox> createLocalExtension() {
    return new LocalTabBoxExtension<>(this);
  }

  @Override
  public void removeField(IFormField f) {
    super.removeField(f);
    // if tabitem (groupbox) is removed from tabbox, we must also reset the selected tab
    // because otherwise the property would point to a tabitem that is no longer a child
    // of this tabbox
    if (f == getSelectedTab()) {
      setSelectedTab(findNewSelectedTab(null));
    }
  }

  @Override
  public void addField(IFormField f) {
    super.addField(f);
    setSelectedTab(findNewSelectedTab(null));
  }

}
