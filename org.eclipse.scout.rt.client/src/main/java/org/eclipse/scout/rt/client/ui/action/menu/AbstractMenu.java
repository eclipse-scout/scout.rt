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
package org.eclipse.scout.rt.client.ui.action.menu;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.extension.ui.action.IActionExtension;
import org.eclipse.scout.rt.client.extension.ui.action.menu.IMenuExtension;
import org.eclipse.scout.rt.client.extension.ui.action.menu.MenuChains.MenuOwnerValueChangedChain;
import org.eclipse.scout.rt.client.ui.action.AbstractAction;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.IActionVisitor;
import org.eclipse.scout.rt.client.ui.action.tree.AbstractActionNode;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.CompareUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ClassId("e8dbfee4-503c-401e-8579-d0aa8618f59d")
public abstract class AbstractMenu extends AbstractActionNode<IMenu> implements IMenu {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractMenu.class);

  private Object m_ownerValue;

  public AbstractMenu() {
    this(true);
  }

  public AbstractMenu(boolean callInitializer) {
    super(callInitializer);
  }

  /**
   * All menu types this menu should be showed with. For menus which are used in different contexts (Table, Tree,
   * ValueField, ActivityMap) a combination of several menu type definitions can be returned. In case the menu is added
   * on any other component (different from {@link ITable}, {@link ITree}, {@link IValueField} , {@link IActivityMap} )
   * the menu type does not have any affect.
   * <p>
   * If multiple menu types of the same context are returned, the menu is rendered only once, using the most "specific"
   * of the types (which one depends on the context). Example: If the menu types contain TableMenuType.TableHeader and
   * TableMenuType.SingleSelection, the menu is only shown in the table header.
   *
   * @see TableMenuType
   * @see TreeMenuType
   * @see ValueFieldMenuType
   * @see ActivityMapMenuType
   */
  @Order(55)
  @ConfigProperty(ConfigProperty.MENU_TYPE)
  protected Set<? extends IMenuType> getConfiguredMenuTypes() {
    return CollectionUtility.<IMenuType> hashSet(
        TableMenuType.SingleSelection,
        TreeMenuType.SingleSelection,
        ValueFieldMenuType.NotNull,
        CalendarMenuType.CalendarComponent,
        PlannerMenuType.Activity,
        TabBoxMenuType.Header);
  }

  @Override
  public Object getOwnerValue() {
    return m_ownerValue;
  }

  @Override
  public final void handleOwnerValueChanged(Object newValue) {
    m_ownerValue = newValue;
    interceptOwnerValueChanged(newValue);
  }

  /**
   * This method is called after a new valid owner value was stored in the model. The owner is the {@link ITable},
   * {@link ITree} or {@link IValueField} the menu belongs to. To get changes of other fields use a
   * {@link PropertyChangeListener} and add it to a certain other field in the {@link AbstractMenu#execInitAction()}
   * method.
   * <h3>NOTE</h3> <b> This method is only called for menus without submenus (leafs) on a {@link ITable}, {@link ITree},
   * {@link IValueField}! For all other fields this method will NEVER be called.</b> <br>
   * The method is only called, if the the current owner value matches the menu type. If the menu is not visible due to
   * it's type, execOwnerValueChanged is not called: E.g.
   * <ul>
   * <li>Menu with type SingleSelection: only called, if the selection is a single selection
   * <li>Menu with type MultiSelection: only called, if the selection is a multi selection
   * <li>Menu with type EmptySpace: only called, if the selection is a on empty space
   * </ul>
   *
   * @param newOwnerValue
   *          depending on the owner the newOwnerValue differs.
   *          <ul>
   *          <li>for {@link ITree} it is the current selection {@link Set} of {@link ITreeNode}'s.</li>
   *          <li>for {@link ITable} it is the current selection {@link List} of {@link ITableRow}'s.</li>
   *          <li>for {@link IValueField} it is the current value.</li>
   *          </ul>
   */
  @ConfigOperation
  @Order(50)
  protected void execOwnerValueChanged(Object newOwnerValue) {

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
    setMenuTypes(getConfiguredMenuTypes());
  }

  @Override
  public void addChildActions(Collection<? extends IMenu> actionList) {
    super.addChildActions(actionList);
    if (CollectionUtility.hasElements(actionList)) {
      afterChildMenusAdd(actionList);
    }
  }

  @Override
  public void removeChildActions(Collection<? extends IMenu> actionList) {
    super.removeChildActions(actionList);
    if (CollectionUtility.hasElements(actionList)) {
      afterChildMenusRemove(actionList);
    }
  }

  protected void afterChildMenusAdd(Collection<? extends IMenu> newChildMenus) {
    if (CollectionUtility.hasElements(newChildMenus)) {
      final Object ownerValue = m_ownerValue;
      IActionVisitor visitor = new IActionVisitor() {
        @Override
        public int visit(IAction action) {
          if (action instanceof IMenu) {
            IMenu menu = (IMenu) action;
            try {
              if (!CompareUtility.equals(menu.getOwnerValue(), ownerValue)) {
                menu.handleOwnerValueChanged(ownerValue);
              }
            }
            catch (RuntimeException e) {
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

  protected void afterChildMenusRemove(Collection<? extends IMenu> childMenusToRemove) {
    if (CollectionUtility.hasElements(childMenusToRemove)) {
      IActionVisitor visitor = new IActionVisitor() {
        @Override
        public int visit(IAction action) {
          if (action instanceof IMenu) {
            IMenu menu = (IMenu) action;
            try {
              menu.handleOwnerValueChanged(null);
            }
            catch (RuntimeException e) {
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

  protected final void interceptOwnerValueChanged(Object newOwnerValue) {
    List<? extends IActionExtension<? extends AbstractAction>> extensions = getAllExtensions();
    MenuOwnerValueChangedChain chain = new MenuOwnerValueChangedChain(extensions);
    chain.execOwnerValueChanged(newOwnerValue);
  }

  protected static class LocalMenuExtension<OWNER extends AbstractMenu> extends LocalActionNodeExtension<IMenu, OWNER> implements IMenuExtension<OWNER> {

    public LocalMenuExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public void execOwnerValueChanged(MenuOwnerValueChangedChain chain, Object newOwnerValue) {
      getOwner().execOwnerValueChanged(newOwnerValue);
    }
  }

  @Override
  protected IMenuExtension<? extends AbstractMenu> createLocalExtension() {
    return new LocalMenuExtension<AbstractMenu>(this);
  }

}
