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
package org.eclipse.scout.rt.client.ui.action.menu;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.IActionVisitor;
import org.eclipse.scout.rt.client.ui.action.tree.AbstractActionNode;
import org.eclipse.scout.rt.client.ui.basic.activitymap.IActivityMap;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;

public abstract class AbstractMenu extends AbstractActionNode<IMenu> implements IMenu {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractMenu.class);

  private boolean m_singleSelectionAction;
  private boolean m_multiSelectionAction;
  private boolean m_emptySpaceAction;
  private boolean m_visibleProperty;
  private Object m_ownerValue;

  public AbstractMenu() {
    this(true);
  }

  public AbstractMenu(boolean callInitializer) {
    super(callInitializer);
  }

  /**
   * All menu types this menu should be showed with. For menus which are used in different contexts (Table, Tree,
   * ValueField, ActivityMap) a combination of several menu type definitions can be returned.
   * In case the menu is added on any other component (different from {@link ITable}, {@link ITree}, {@link IValueField}
   * , {@link IActivityMap} )
   * the menu type does not have any affect.
   * 
   * @see TableMenuType
   * @see TreeMenuType
   * @see ValueFieldMenuType
   * @see ActivityMapMenuType
   */
  @Order(55)
  @ConfigProperty(ConfigProperty.MENU_TYPE)
  protected Set<? extends IMenuType> getConfiguredMenuTypes() {
    return CollectionUtility.<IMenuType> hashSet(TableMenuType.SingleSelection,
        TreeMenuType.SingleSelection,
        ValueFieldMenuType.NotNull, ActivityMapMenuType.Activity);
  }

  /**
   * @deprecated Will be removed in Scout 5.0.<br>
   *             Use {@link TableMenuType#SingleSelection} and/or {@link TreeMenuType#SingleSelection} in
   *             {@link #getConfiguredMenuTypes()} instead.
   */
  @Deprecated
  @Order(60)
  protected boolean getConfiguredSingleSelectionAction() {
    return true;
  }

  /**
   * @deprecated Will be removed in Scout 5.0.<br>
   *             Use {@link TableMenuType#MultiSelection} and/or {@link TreeMenuType#MultiSelection} in
   *             {@link #getConfiguredMenuTypes()} instead.
   */
  @Deprecated
  @Order(70)
  protected boolean getConfiguredMultiSelectionAction() {
    return false;
  }

  /**
   * @deprecated Will be removed in Scout 5.0.<br>
   *             Use {@link TableMenuType#EmptySpace} and/or {@link TreeMenuType#EmptySpace} in
   *             {@link #getConfiguredMenuTypes()} instead.
   */
  @Deprecated
  @Order(90)
  protected boolean getConfiguredEmptySpaceAction() {
    return false;
  }

  @Override
  public final void handleOwnerValueChanged(Object newValue) throws ProcessingException {
    if (!CompareUtility.equals(m_ownerValue, newValue)) {
      m_ownerValue = newValue;
      execOwnerValueChanged(newValue);
    }
  }

  /**
   * This method is called after a new valid owner value was stored in the model. The owner is the {@link ITable},
   * {@link ITree} or {@link IValueField} the menu belongs to. To get changes of other fields use a
   * {@link PropertyChangeListener} an add it to a certain other field in the {@link AbstractMenu#execInitAction()}
   * method. <h3>NOTE</h3> <b>
   * This method is only called for menus without submenus (leafs) on a {@link ITable}, {@link ITree},
   * {@link IValueField}! For all other fields
   * this method will NEVER be called.</b> <br>
   * 
   * @param newOwnerValue
   *          depending on the owner the newOwnerValue differs.
   *          <ul>
   *          <li>for {@link ITree} it is the current selection {@link Set} of {@link ITreeNode}'s.</li>
   *          <li>for {@link ITable} it is the current selection {@link List} of {@link ITableRow}'s.</li>
   *          <li>for {@link IValueField} it is the current value.</li>
   *          </ul>
   * @throws ProcessingException
   */
  @ConfigOperation
  @Order(50.0)
  protected void execOwnerValueChanged(Object newOwnerValue) throws ProcessingException {

  }

  /**
   * this method is called before a menu will be displayed. This method should only be used to update the text, icon or
   * other display styles. <h3>NOTE</h3> <b>Do not change visibility or structure of a
   * menu in this method unless it is no other option available!</b> <br>
   * Menus are considered to listen whatever changes of the application model to update their visibility and structure.
   * This is the only way a GUI layer can reflect menu changes immediately.
   * 
   * @throws ProcessingException
   */
  @ConfigOperation
  @Order(60.0)
  protected void execAboutToShow() throws ProcessingException {

  }

  @Override
  public final void aboutToShow() {
    try {
      aboutToShowInternal();
      execAboutToShow();
    }
    catch (Throwable t) {
      LOG.warn("Action " + getClass().getName(), t);
    }
  }

  /**
   * do not use this method, it is used internally by subclasses
   */
  protected void aboutToShowInternal() {
  }

  /**
   * converts a untyped collection into a type collection of table rows.
   * 
   * @param input
   * @return null if the input is null or not all elements of the input are {@link ITableRow}s.
   */
  protected Collection<ITableRow> convertToTableRows(Collection<?> input) {
    if (input == null) {
      return null;
    }
    List<ITableRow> rows = new ArrayList<ITableRow>(input.size());
    for (Object o : input) {
      if (o instanceof ITableRow) {
        rows.add((ITableRow) o);
      }
    }
    if (rows.size() == input.size()) {
      return rows;
    }
    return null;
  }

  /**
   * converts a untyped collection into a type collection of tree nodes.
   * 
   * @param input
   * @return null if the input is null or not all elements of the input are {@link ITreeNode}s.
   */
  protected Collection<ITreeNode> convertToTreeNodes(Collection<?> input) {
    if (input == null) {
      return null;
    }
    List<ITreeNode> rows = new ArrayList<ITreeNode>(input.size());
    for (Object o : input) {
      if (o instanceof ITreeNode) {
        rows.add((ITreeNode) o);
      }
    }
    if (rows.size() == input.size()) {
      return rows;
    }
    return null;
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setEmptySpaceAction(getConfiguredEmptySpaceAction());
    setSingleSelectionAction(getConfiguredSingleSelectionAction());
    setMultiSelectionAction(getConfiguredMultiSelectionAction());
    if (isSingleSelectionAction() || isMultiSelectionAction() || isEmptySpaceAction()) {
      // ok
    }
    else {
      // legacy case of implicit new menu
      setEmptySpaceAction(true);
    }
    if (!ConfigurationUtility.isMethodOverwrite(AbstractMenu.class, "getConfiguredMenuTypes", new Class[0], this.getClass())) {

      // legacy
      Set<IMenuType> menuTypes = new HashSet<IMenuType>();
      if (isSingleSelectionAction()) {
        menuTypes.add(TableMenuType.SingleSelection);
        menuTypes.add(TreeMenuType.SingleSelection);
        menuTypes.add(ValueFieldMenuType.NotNull);
        menuTypes.add(ActivityMapMenuType.Activity);
        menuTypes.add(CalendarMenuType.CalendarComponent);
      }
      if (isMultiSelectionAction()) {
        menuTypes.add(TableMenuType.MultiSelection);
        menuTypes.add(TreeMenuType.MultiSelection);
        menuTypes.add(ValueFieldMenuType.NotNull);
        menuTypes.add(ActivityMapMenuType.Activity);
        menuTypes.add(CalendarMenuType.CalendarComponent);
      }
      if (isEmptySpaceAction()) {
        menuTypes.add(TableMenuType.EmptySpace);
        menuTypes.add(TreeMenuType.EmptySpace);
        menuTypes.add(ValueFieldMenuType.Null);
        menuTypes.add(ActivityMapMenuType.Selection);
        menuTypes.add(CalendarMenuType.EmptySpace);
      }
      setMenuTypes(menuTypes);
    }
    else {
      setMenuTypes(getConfiguredMenuTypes());
    }
  }

  @Override
  public void addChildActions(List<? extends IMenu> actionList) {
    super.addChildActions(actionList);
    if (CollectionUtility.hasElements(actionList)) {
      afterChildMenusAdd(actionList);
    }
  }

  @Override
  public void removeChildActions(List<? extends IMenu> actionList) {
    super.removeChildActions(actionList);
    if (CollectionUtility.hasElements(actionList)) {
      afterChildMenusRemove(actionList);
    }
  }

  protected void afterChildMenusAdd(List<? extends IMenu> newChildMenus) {
    if (CollectionUtility.hasElements(newChildMenus)) {
      final Object ownerValue = m_ownerValue;
      IActionVisitor visitor = new IActionVisitor() {
        @Override
        public int visit(IAction action) {
          if (action instanceof IMenu) {
            IMenu menu = (IMenu) action;
            try {
              menu.handleOwnerValueChanged(ownerValue);
            }
            catch (ProcessingException e) {
              LOG.error("error during handle owner value changed.", e);
            }
          }
          return CONTINUE;
        }
      };
      for (IMenu m : newChildMenus) {
        m.acceptVisitor(visitor);
      }
    }
  }

  protected void afterChildMenusRemove(List<? extends IMenu> childMenusToRemove) {
    if (CollectionUtility.hasElements(childMenusToRemove)) {
      IActionVisitor visitor = new IActionVisitor() {
        @Override
        public int visit(IAction action) {
          if (action instanceof IMenu) {
            IMenu menu = (IMenu) action;
            try {
              menu.handleOwnerValueChanged(null);
            }
            catch (ProcessingException e) {
              LOG.error("error during handle owner value changed.", e);
            }
          }
          return CONTINUE;
        }
      };
      for (IMenu m : childMenusToRemove) {
        m.acceptVisitor(visitor);
      }
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Set<IMenuType> getMenuTypes() {
    return CollectionUtility.<IMenuType> hashSet((Set<IMenuType>) propertySupport.getProperty(PROP_MENU_TYPES));
  }

  public void setMenuTypes(Set<? extends IMenuType> menuTypes) {
    propertySupport.setProperty(PROP_MENU_TYPES, CollectionUtility.<IMenuType> hashSet(menuTypes));
  }

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public boolean isSingleSelectionAction() {
    return m_singleSelectionAction;
  }

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public void setSingleSelectionAction(boolean b) {
    m_singleSelectionAction = b;
  }

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public boolean isMultiSelectionAction() {
    return m_multiSelectionAction;
  }

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public void setMultiSelectionAction(boolean b) {
    m_multiSelectionAction = b;
  }

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public boolean isEmptySpaceAction() {
    return m_emptySpaceAction;
  }

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public void setEmptySpaceAction(boolean b) {
    m_emptySpaceAction = b;
  }

}
