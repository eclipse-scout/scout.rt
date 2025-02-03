/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.action.menu;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.eclipse.scout.rt.client.extension.ui.action.IActionExtension;
import org.eclipse.scout.rt.client.extension.ui.action.menu.IMenuExtension;
import org.eclipse.scout.rt.client.extension.ui.action.menu.MenuChains.MenuOwnerValueChangedChain;
import org.eclipse.scout.rt.client.ui.action.AbstractAction;
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
import org.eclipse.scout.rt.platform.util.ObjectUtility;

@ClassId("e8dbfee4-503c-401e-8579-d0aa8618f59d")
public abstract class AbstractMenu extends AbstractActionNode<IMenu> implements IMenu {

  private static final Set<? extends IMenuType> DEFAULT_MENU_TYPES = Set.of(
      TableMenuType.SingleSelection,
      TreeMenuType.SingleSelection,
      ValueFieldMenuType.NotNull,
      CalendarMenuType.CalendarComponent,
      PlannerMenuType.Activity,
      TabBoxMenuType.Header,
      ImageFieldMenuType.Image,
      ImageFieldMenuType.ImageId,
      ImageFieldMenuType.ImageUrl);

  private Object m_ownerValue;

  public AbstractMenu() {
    this(true);
  }

  public AbstractMenu(boolean callInitializer) {
    super(callInitializer);
  }

  /**
   * All menu types this menu should be showed with. For menus which are used in different contexts (Table, Tree,
   * ValueField) a combination of several menu type definitions can be returned. In case the menu is added on any other
   * component (different from {@link ITable}, {@link ITree}, {@link IValueField} ) the menu type does not have any
   * effect.
   * <p>
   * If multiple menu types of the same context are returned, the menu is rendered only once, using the most "specific"
   * of the types (which one depends on the context). Example: If the menu types contain TableMenuType.TableHeader and
   * TableMenuType.SingleSelection, the menu is only shown in the table header.
   *
   * @see TableMenuType
   * @see TreeMenuType
   * @see ValueFieldMenuType
   */
  @Order(55)
  @ConfigProperty(ConfigProperty.MENU_TYPE)
  protected Set<? extends IMenuType> getConfiguredMenuTypes() {
    return DEFAULT_MENU_TYPES;
  }

  /**
   * Configures whether two or more consecutive clicks on the menu within a short period of time (e.g. double click)
   * should be prevented by the UI.
   * <p>
   * The default is <code>false</code>.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(56)
  protected boolean getConfiguredPreventDoubleClick() {
    return false;
  }

  /**
   * Configures if the menu is stackable. A stackable menu will be stacked in a dropdown menu if there is not enough
   * space in the menubar. This property is usually set to false for right aligned menus with only an icon.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(57)
  protected boolean getConfiguredStackable() {
    return true;
  }

  /**
   * Configures if the menu is shrinkable. A shrinkable menu will be displayed without text but only with its configured
   * icon if there is not enough space in the menubar.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(58)
  protected boolean getConfiguredShrinkable() {
    return false;
  }

  /**
   * Configures the behavior when the sub-menu icon is visible. A menu must always have child-actions in order to
   * display the sub-menu icon. By default the sub-menu icon is shown only when the menu has text. You may change that
   * behavior by configuring another visibility.
   */
  @ConfigProperty(ConfigProperty.STRING)
  @Order(59)
  protected String getConfiguredSubMenuVisibility() {
    return SUB_MENU_VISIBILITY_DEFAULT;
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
   * {@link PropertyChangeListener} and add it to a certain other field in the {@link #execInitAction()} method. <br>
   * <br>
   * <b>Note:</b> This method is only called for menus on a {@link ITable}, {@link ITree}, {@link IValueField}! For all
   * other fields this method will NEVER be called.<br>
   * The method is only called, if the the current owner value matches the menu type. If the menu is not visible due to
   * it's type, execOwnerValueChanged is not called: E.g.
   * <ul>
   * <li>Menu with type SingleSelection: only called, if the selection is a single selection
   * <li>Menu with type MultiSelection: only called, if the selection is a multi selection
   * <li>Menu with type EmptySpace: only called, if the selection is a on empty space
   * </ul>
   *
   * @param newOwnerValue
   *     depending on the owner the newOwnerValue differs.
   *     <ul>
   *     <li>for {@link ITree} it is the current selection {@link Set} of {@link ITreeNode}'s.</li>
   *     <li>for {@link ITable} it is the current selection {@link List} of {@link ITableRow}'s.</li>
   *     <li>for {@link IValueField} it is the current value.</li>
   *     </ul>
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
    List<ITableRow> rows = new ArrayList<>(input.size());
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
    List<ITreeNode> rows = new ArrayList<>(input.size());
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
    if (isStoreConfigValues()) {
      setMenuTypes(getConfiguredMenuTypes());
      setPreventDoubleClick(getConfiguredPreventDoubleClick());
      setStackable(getConfiguredStackable());
      setShrinkable(getConfiguredShrinkable());
      setSubMenuVisibility(getConfiguredSubMenuVisibility());
    }
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
      Consumer<IMenu> visitor = menu -> {
        if (ObjectUtility.notEquals(menu.getOwnerValue(), ownerValue)) {
          menu.handleOwnerValueChanged(ownerValue);
        }
      };
      for (IMenu m : newChildMenus) {
        m.visit(visitor, IMenu.class);
      }
    }
  }

  protected void afterChildMenusRemove(Collection<? extends IMenu> childMenusToRemove) {
    // no default action
  }

  @Override
  public Set<IMenuType> getMenuTypes() {
    return CollectionUtility.hashSet(propertySupport.getProperty(PROP_MENU_TYPES, this::getConfiguredMenuTypes));
  }

  public void setMenuTypes(Set<? extends IMenuType> menuTypes) {
    propertySupport.setProperty(PROP_MENU_TYPES, CollectionUtility.<IMenuType> hashSet(menuTypes));
  }

  @Override
  public boolean isPreventDoubleClick() {
    return propertySupport.getProperty(PROP_PREVENT_DOUBLE_CLICK, this::getConfiguredPreventDoubleClick);
  }

  @Override
  public void setPreventDoubleClick(boolean preventDoubleClick) {
    propertySupport.setPropertyBool(PROP_PREVENT_DOUBLE_CLICK, preventDoubleClick);
  }

  @Override
  public boolean isStackable() {
    return propertySupport.getProperty(PROP_STACKABLE, this::getConfiguredStackable);
  }

  @Override
  public void setStackable(boolean stackable) {
    propertySupport.setPropertyBool(PROP_STACKABLE, stackable);
  }

  @Override
  public boolean isShrinkable() {
    return propertySupport.getProperty(PROP_SHRINKABLE, this::getConfiguredShrinkable);
  }

  @Override
  public void setShrinkable(boolean shrinkable) {
    propertySupport.setPropertyBool(PROP_SHRINKABLE, shrinkable);
  }

  @Override
  public String getSubMenuVisibility() {
    return propertySupport.getProperty(PROP_SUB_MENU_VISIBILITY, this::getConfiguredSubMenuVisibility);
  }

  @Override
  public void setSubMenuVisibility(String subMenuVisibility) {
    propertySupport.setProperty(PROP_SUB_MENU_VISIBILITY, subMenuVisibility);
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
    return new LocalMenuExtension<>(this);
  }
}
