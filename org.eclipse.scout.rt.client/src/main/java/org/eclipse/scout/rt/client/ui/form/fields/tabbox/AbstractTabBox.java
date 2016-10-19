/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields.tabbox;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.extension.ui.action.tree.MoveActionNodesHandler;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.tabbox.ITabBoxExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.tabbox.TabBoxChains.TabBoxTabSelectedChain;
import org.eclipse.scout.rt.client.ui.action.ActionUtility;
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

  @Override
  public int getMarkStrategy() {
    return propertySupport.getPropertyInt(PROP_MARK_STRATEGY);
  }

  @Override
  public void setMarkStrategy(int markStrategy) {
    propertySupport.setPropertyInt(PROP_MARK_STRATEGY, markStrategy);
  }

  @Override
  protected void initConfig() {
    m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(new P_UIFacade(), ModelContext.copyCurrent());
    m_grid = new TabBoxGrid(this);
    setMarkStrategy(getConfiguredMarkStrategy());
    super.initConfig();
    addPropertyChangeListener(PROP_SELECTED_TAB, new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent e) {
        // single observer exec
        try {
          interceptTabSelected(getSelectedTab());
        }
        catch (Exception ex) {
          BEANS.get(ExceptionHandler.class).handle(ex);
        }
      }
    });
    initMenus();
  }

  private void initMenus() {
    List<Class<? extends IMenu>> declaredMenus = getDeclaredMenus();
    List<IMenu> contributedMenus = m_contributionHolder.getContributionsByClass(IMenu.class);
    OrderedCollection<IMenu> menus = new OrderedCollection<IMenu>();
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
      LOG.error("error occured while dynamically contributing menus.", e);
    }
    new MoveActionNodesHandler<IMenu>(menus).moveModelObjects();
    // set container on menus
    IFormFieldContextMenu contextMenu = new FormFieldContextMenu<ITabBox>(this, menus.getOrderedList());
    contextMenu.setContainerInternal(this);
    setContextMenu(contextMenu);
  }

  /*
   * Runtime
   */

  @Override
  public void rebuildFieldGrid() {
    m_grid.validate();
    if (isInitialized() && getForm() != null) {
      getForm().structureChanged(this);
    }
  }

  @Override
  protected void handleFieldVisibilityChanged() {
    // box is only visible when it has at least one visible item
    super.handleFieldVisibilityChanged();

    if (isInitialized()) {
      rebuildFieldGrid();
    }

    if (hasVisibleFieldsInternal()) {
      final IGroupBox selectedBox = getSelectedTab();
      if (selectedBox == null) {
        for (IGroupBox box : getGroupBoxes()) {
          if (box.isVisible()) {
            setSelectedTab(box);
            break;
          }
        }
      }
      else if (!selectedBox.isVisible()) {
        int index = getFieldIndex(selectedBox);
        List<IGroupBox> boxes = getGroupBoxes();
        // next to right side
        for (int i = index + 1; i < getFieldCount(); i++) {
          IGroupBox box = boxes.get(i);
          if (box.isVisible()) {
            setSelectedTab(box);
            break;
          }
        }
        if (getSelectedTab() == selectedBox) {
          // next to left side
          for (int i = index - 1; i >= 0; i--) {
            IGroupBox box = boxes.get(i);
            if (box.isVisible()) {
              setSelectedTab(box);
              break;
            }
          }
        }
      }
    }
  }

  @Override
  public final int getGridColumnCount() {
    return m_grid.getGridColumnCount();
  }

  @Override
  public final int getGridRowCount() {
    return m_grid.getGridRowCount();
  }

  @Override
  public List<IGroupBox> getGroupBoxes() {
    List<IGroupBox> result = new ArrayList<IGroupBox>();
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
    if (box.getParentField() == this) {
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

  @Override
  protected void disposeFieldInternal() {
    super.disposeFieldInternal();
    ActionUtility.disposeActions(getMenus());
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
    return new LocalTabBoxExtension<AbstractTabBox>(this);
  }
}
