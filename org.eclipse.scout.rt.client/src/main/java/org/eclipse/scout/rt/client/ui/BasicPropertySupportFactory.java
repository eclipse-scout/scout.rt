/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.client.ui.action.AbstractAction;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.CheckableStyle;
import org.eclipse.scout.rt.client.ui.basic.table.GroupingStyle;
import org.eclipse.scout.rt.client.ui.basic.table.HeaderCell;
import org.eclipse.scout.rt.client.ui.basic.table.HierarchicalStyle;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractBasicField;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.client.ui.form.fields.IBasicField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.IStringField;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.IOrdered;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.reflect.AbstractPropertyObserver;
import org.eclipse.scout.rt.platform.reflect.AbstractPropertyObserver.StoreConfigValuesConfigProperty;
import org.eclipse.scout.rt.platform.reflect.BasicPropertySupport;
import org.eclipse.scout.rt.platform.reflect.DefaultValueMap;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ImmutablePair;
import org.eclipse.scout.rt.platform.util.TriState;

/**
 * Factory to create a {@link BasicPropertySupport} for any {@link AbstractPropertyObserver}.
 * <p>
 * The newly created property support will always be backed by an underlying empty {@link Map}, this is either a regular {@link HashMap} or a {@link DefaultValueMap} which is size-optimized for the specific property observer if it is filled
 * with the default values for this observer. In any case this does not result in different behavior, the only difference will be in performance and memory consumption.
 * </p>
 */
@ApplicationScoped
public class BasicPropertySupportFactory {

  public Map<Class<? extends AbstractPropertyObserver>, Map<String, Object>> m_defaultValuesByClass = new HashMap<>();

  public BasicPropertySupport createFor(AbstractPropertyObserver holder) {
    // order matters
    Map<String, Object> defaultValues = switch (holder) {
      case AbstractMenu ignored -> m_defaultValuesByClass.computeIfAbsent(AbstractMenu.class, k -> createDefaultValueMapForAbstractMenu());
      case AbstractColumn ignored -> m_defaultValuesByClass.computeIfAbsent(AbstractColumn.class, k -> createDefaultValueMapForAbstractColumn());
      case AbstractButton ignored -> m_defaultValuesByClass.computeIfAbsent(AbstractButton.class, k -> createDefaultValueMapForAbstractButton());
      case AbstractGroupBox ignored -> m_defaultValuesByClass.computeIfAbsent(AbstractGroupBox.class, k -> createDefaultValueMapForAbstractGroupBox());
      case AbstractBasicField<?> ignored -> m_defaultValuesByClass.computeIfAbsent(AbstractBasicField.class, k -> createDefaultValueMapForAbstractBasicField());
      case AbstractValueField<?> ignored -> m_defaultValuesByClass.computeIfAbsent(AbstractValueField.class, k -> createDefaultValueMapForAbstractValueField());
      case AbstractFormField ignored -> m_defaultValuesByClass.computeIfAbsent(AbstractFormField.class, k -> createDefaultValueMapForAbstractFormField());
      case AbstractAction ignored -> m_defaultValuesByClass.computeIfAbsent(AbstractAction.class, k -> createDefaultValueMapForAbstractAction());
      case AbstractTable ignored -> m_defaultValuesByClass.computeIfAbsent(AbstractTable.class, k -> createDefaultValueMapForAbstractTable());
      case AbstractWidget ignored -> m_defaultValuesByClass.computeIfAbsent(AbstractWidget.class, k -> createDefaultValueMapForAbstractWidget()); // fallback: AbstractWidget
      case HeaderCell ignored -> m_defaultValuesByClass.computeIfAbsent(HeaderCell.class, k -> createDefaultValueMapForHeaderCell());
      default -> null;
    };
    return new BasicPropertySupport(holder, defaultValues != null ? createDefaultValueMap(defaultValues) : new HashMap<>());
  }

  protected Map<String, Object> createDefaultValueMap(Map<String, Object> defaultValues) {
    return new DefaultValueMap(defaultValues, true);
  }

  protected Map<String, Object> createDefaultValueMapForHeaderCell() {
    return Collections.unmodifiableMap(CollectionUtility.hashMap(
        new ImmutablePair<>(HeaderCell.PROP_TEXT, null),
        new ImmutablePair<>(HeaderCell.PROP_TOOLTIP_HTML_ENABLED, false),
        new ImmutablePair<>(HeaderCell.PROP_ICON_ID, null),
        new ImmutablePair<>(HeaderCell.PROP_CSS_CLASS, null),
        new ImmutablePair<>(HeaderCell.PROP_HTML_ENABLED, false),
        new ImmutablePair<>(HeaderCell.PROP_MENU_ENABLED, true),
        new ImmutablePair<>(HeaderCell.PROP_HORIZONTAL_ALIGNMENT, -1)
    ));
  }

  protected Map<String, Object> createDefaultValueMapForAbstractColumn() {
    return Collections.unmodifiableMap(CollectionUtility.hashMap(
        new ImmutablePair<>(IColumn.PROP_AUTO_OPTIMIZE_WIDTH, false),
        new ImmutablePair<>(IColumn.PROP_HORIZONTAL_ALIGNMENT, -1),
        new ImmutablePair<>(IColumn.PROP_VISIBLE, true),
        new ImmutablePair<>(IColumn.PROP_ORDER, IOrdered.DEFAULT_ORDER),
        new ImmutablePair<>(IColumn.PROP_WIDTH, 60),
        new ImmutablePair<>(IColumn.PROP_MIN_WIDTH, 60),
        new ImmutablePair<>(IColumn.PROP_AUTO_OPTIMIZE_MAX_WIDTH, -1),
        new ImmutablePair<>(IColumn.PROP_FIXED_WIDTH, false),
        new ImmutablePair<>(IColumn.PROP_FIXED_POSITION, false),
        new ImmutablePair<>(IColumn.PROP_EDITABLE, false),
        new ImmutablePair<>(IFormField.PROP_MANDATORY, false),
        new ImmutablePair<>(IColumn.PROP_VIEW_COLUMN_INDEX_HINT, -1),
        new ImmutablePair<>(IColumn.PROP_CSS_CLASS, null),
        new ImmutablePair<>(IStringField.PROP_WRAP_TEXT, false),
        new ImmutablePair<>(IColumn.PROP_HTML_ENABLED, false),
        new ImmutablePair<>(IColumn.PROP_UI_SORT_POSSIBLE, false),
        new ImmutablePair<>(IColumn.PROP_NODE_COLUMN_CANDIDATE, true)
    ));
  }

  protected Map<String, Object> createDefaultValueMapForAbstractButton() {
    HashMap<String, Object> defaultValues = new HashMap<>(createDefaultValueMapForAbstractFormField());
    defaultValues.put(IButton.PROP_INHERIT_ACCESSIBILITY, true);
    defaultValues.put(IButton.PROP_DISABLED_STYLE, IButton.DISPLAY_STYLE_DEFAULT);
    defaultValues.put(IButton.PROP_DEFAULT_BUTTON, null);
    defaultValues.put(IButton.PROP_ICON_ID, null);
    defaultValues.put(IButton.PROP_KEY_STROKE, null);
    defaultValues.put(IButton.PROP_KEY_STROKE_SCOPE_CLASS, null);
    defaultValues.put(IButton.PROP_PREVENT_DOUBLE_CLICK, false);
    defaultValues.put(IButton.PROP_STACKABLE, true);
    defaultValues.put(IButton.PROP_SHRINKABLE, false);
    defaultValues.put(IFormField.PROP_STATUS_VISIBLE, false); // default is overwritten for button
    return Collections.unmodifiableMap(defaultValues);
  }

  protected Map<String, Object> createDefaultValueMapForAbstractGroupBox() {
    HashMap<String, Object> defaultValues = new HashMap<>(createDefaultValueMapForAbstractFormField());
    defaultValues.put(IGroupBox.PROP_SUB_LABEL, null);
    defaultValues.put(IGroupBox.PROP_EXPANDABLE, false);
    defaultValues.put(IGroupBox.PROP_EXPANDED, true);
    defaultValues.put(IGroupBox.PROP_CACHE_EXPANDED, false);
    defaultValues.put(IGroupBox.PROP_BORDER_VISIBLE, true);
    defaultValues.put(IGroupBox.PROP_BORDER_DECORATION, IGroupBox.BORDER_DECORATION_AUTO);
    defaultValues.put(IGroupBox.PROP_GRID_COLUMN_COUNT, 2); // default value -1 => 2
    defaultValues.put(IGroupBox.PROP_BACKGROUND_IMAGE_NAME, null);
    defaultValues.put(IGroupBox.PROP_BACKGROUND_IMAGE_HORIZONTAL_ALIGNMENT, 0);
    defaultValues.put(IGroupBox.PROP_BACKGROUND_IMAGE_VERTICAL_ALIGNMENT, 0);
    defaultValues.put(IGroupBox.PROP_EMPTY, false); // usually group boxes are not empty (even though they are initially empty)
    defaultValues.put(IGroupBox.PROP_SCROLLABLE, TriState.UNDEFINED);
    defaultValues.put(IGroupBox.PROP_SELECTION_KEYSTROKE, null);
    defaultValues.put(IGroupBox.PROP_MENU_BAR_POSITION, IGroupBox.MENU_BAR_POSITION_AUTO);
    defaultValues.put(IGroupBox.PROP_MENU_BAR_ELLIPSIS_POSITION, IGroupBox.MENU_BAR_ELLIPSIS_POSITION_RIGHT);
    defaultValues.put(IGroupBox.PROP_RESPONSIVE, TriState.UNDEFINED);
    return Collections.unmodifiableMap(defaultValues);
  }

  protected Map<String, Object> createDefaultValueMapForAbstractBasicField() {
    HashMap<String, Object> defaultValues = new HashMap<>(createDefaultValueMapForAbstractValueField());
    defaultValues.put(IBasicField.PROP_UPDATE_DISPLAY_TEXT_ON_MODIFY, false);
    defaultValues.put(IBasicField.PROP_UPDATE_DISPLAY_TEXT_ON_MODIFY_DELAY, 250);
    return Collections.unmodifiableMap(defaultValues);
  }

  protected Map<String, Object> createDefaultValueMapForAbstractValueField() {
    HashMap<String, Object> defaultValues = new HashMap<>(createDefaultValueMapForAbstractFormField());
    defaultValues.put(IValueField.PROP_CLEARABLE, IValueField.CLEARABLE_FOCUSED);
    defaultValues.put(IValueField.PROP_AUTO_ADD_DEFAULT_MENUS, true);
    defaultValues.put(IValueField.PROP_DISPLAY_TEXT, "");
    return Collections.unmodifiableMap(defaultValues);
  }

  protected Map<String, Object> createDefaultValueMapForAbstractFormField() {
    HashMap<String, Object> defaultValues = new HashMap<>(createDefaultValueMapForAbstractWidget());
    defaultValues.put(IFormField.PROP_EMPTY, true);
    defaultValues.put(IFormField.PROP_FIELD_STYLE, IFormField.FIELD_STYLE_ALTERNATIVE);
    defaultValues.put(IFormField.PROP_DISABLED_STYLE, IFormField.DISABLED_STYLE_DEFAULT);
    defaultValues.put(IFormField.PROP_VISIBLE, true);
    defaultValues.put(IFormField.PROP_MANDATORY, false);
    defaultValues.put(IFormField.PROP_ORDER, IOrdered.DEFAULT_ORDER);
    defaultValues.put(IFormField.PROP_TOOLTIP_TEXT, null);
    defaultValues.put(IFormField.PROP_TOOLTIP_ANCHOR, IFormField.TOOLTIP_ANCHOR_DEFAULT);
    defaultValues.put(IFormField.PROP_LABEL, null);
    defaultValues.put(IFormField.PROP_LABEL_POSITION, IFormField.LABEL_POSITION_DEFAULT);
    defaultValues.put(IFormField.PROP_LABEL_WIDTH_IN_PIXEL, IFormField.LABEL_WIDTH_DEFAULT);
    defaultValues.put(IFormField.PROP_LABEL_USE_UI_WIDTH, false);
    defaultValues.put(IFormField.PROP_LABEL_VISIBLE, true);
    defaultValues.put(IFormField.PROP_LABEL_HTML_ENABLED, false);
    defaultValues.put(IFormField.PROP_STATUS_VISIBLE, true);
    defaultValues.put(IFormField.PROP_STATUS_POSITION, IFormField.STATUS_POSITION_DEFAULT);
    defaultValues.put(IFormField.PROP_CSS_CLASS, null);
    defaultValues.put(IFormField.PROP_PREVENT_INITIAL_FOCUS, false);
    defaultValues.put(IFormField.PROP_SAVE_NEEDED, false);
    return Collections.unmodifiableMap(defaultValues);
  }

  protected Map<String, Object> createDefaultValueMapForAbstractMenu() {
    HashMap<String, Object> defaultValues = new HashMap<>(createDefaultValueMapForAbstractAction());
    if (Boolean.TRUE.equals(CONFIG.getPropertyValue(StoreConfigValuesConfigProperty.class))) {
      defaultValues.put(IMenu.PROP_MENU_TYPES, AbstractMenu.DEFAULT_MENU_TYPES);
      defaultValues.put(IMenu.PROP_PREVENT_DOUBLE_CLICK, false);
      defaultValues.put(IMenu.PROP_STACKABLE, true);
      defaultValues.put(IMenu.PROP_SHRINKABLE, false);
      defaultValues.put(IMenu.PROP_SUB_MENU_VISIBILITY, IMenu.SUB_MENU_VISIBILITY_DEFAULT);
    }
    return Collections.unmodifiableMap(defaultValues);
  }

  protected Map<String, Object> createDefaultValueMapForAbstractAction() {
    HashMap<String, Object> defaultValues = new HashMap<>(createDefaultValueMapForAbstractWidget());
    defaultValues.put(IAction.PROP_ICON_ID, null);
    defaultValues.put(IAction.PROP_TEXT, null);
    defaultValues.put(IAction.PROP_TEXT_POSITION, IAction.TEXT_POSITION_DEFAULT);
    defaultValues.put(IAction.PROP_HTML_ENABLED, false);
    defaultValues.put(IAction.PROP_TOOLTIP_TEXT, null);
    defaultValues.put(IAction.PROP_KEY_STROKE, null);
    defaultValues.put(IAction.PROP_KEYSTROKE_FIRE_POLICY, IAction.KEYSTROKE_FIRE_POLICY_ACCESSIBLE_ONLY);
    defaultValues.put(IAction.PROP_VISIBLE, true);
    defaultValues.put(IAction.PROP_ORDER, IOrdered.DEFAULT_ORDER);
    defaultValues.put(IAction.PROP_ACTION_STYLE, IMenu.ACTION_STYLE_DEFAULT);
    defaultValues.put(IAction.PROP_CSS_CLASS, null);
    return Collections.unmodifiableMap(defaultValues);
  }

  protected Map<String, Object> createDefaultValueMapForAbstractTable() {
    HashMap<String, Object> defaultValues = new HashMap<>(createDefaultValueMapForAbstractWidget());
    defaultValues.put(ITable.PROP_LOADING, false);
    defaultValues.put(ITable.PROP_GROUPING_STYLE, GroupingStyle.TOP);
    defaultValues.put(ITable.PROP_HIERARCHICAL_STYLE, HierarchicalStyle.DEFAULT);
    defaultValues.put(ITable.PROP_CHECKABLE_STYLE, CheckableStyle.CHECKBOX);
    defaultValues.put(ITable.PROP_TITLE, null);
    defaultValues.put(ITable.PROP_SORT_ENABLED, true);
    defaultValues.put(ITable.PROP_DEFAULT_ICON, null);
    defaultValues.put(ITable.PROP_CSS_CLASS, null);
    defaultValues.put(ITable.PROP_ROW_ICON_VISIBLE, false);
    defaultValues.put(ITable.PROP_ROW_ICON_COLUMN_WIDTH, IColumn.NARROW_MIN_WIDTH);
    defaultValues.put(ITable.PROP_HEADER_VISIBLE, true);
    defaultValues.put(ITable.PROP_HEADER_ENABLED, true);
    defaultValues.put(ITable.PROP_HEADER_MENUS_ENABLED, true);
    defaultValues.put(ITable.PROP_AUTO_RESIZE_COLUMNS, false);
    defaultValues.put(ITable.PROP_CHECKABLE, false);
    defaultValues.put(ITable.PROP_MULTI_CHECK, true);
    defaultValues.put(ITable.PROP_MULTI_SELECT, true);
    defaultValues.put(ITable.PROP_MULTILINE_TEXT, false);
    defaultValues.put(ITable.PROP_KEYBOARD_NAVIGATION, true);
    defaultValues.put(ITable.PROP_DRAG_TYPE, 0);
    defaultValues.put(ITable.PROP_DROP_TYPE, 0);
    defaultValues.put(ITable.PROP_DROP_MAXIMUM_SIZE, ITable.DEFAULT_DROP_MAXIMUM_SIZE);
    defaultValues.put(ITable.PROP_SCROLL_TO_SELECTION, false);
    defaultValues.put(ITable.PROP_TABLE_STATUS_VISIBLE, false);
    defaultValues.put(ITable.PROP_TEXT_FILTER_ENABLED, true);
    defaultValues.put(ITable.PROP_TRUNCATED_CELL_TOOLTIP_ENABLED, TriState.UNDEFINED);
    defaultValues.put(ITable.PROP_CLIENT_UI_PREFERENCES_ENABLED, true);
    defaultValues.put(ITable.PROP_TILE_MODE, false);
    defaultValues.put(ITable.PROP_COMPACT, false);
    defaultValues.put(ITable.PROP_TABLE_CUSTOMIZER, null);
    return Collections.unmodifiableMap(defaultValues);
  }

  protected Map<String, Object> createDefaultValueMapForAbstractWidget() {
    return Collections.unmodifiableMap(CollectionUtility.hashMap(
        new ImmutablePair<>(IWidget.PROP_INIT_CONFIG_DONE, true),
        new ImmutablePair<>(IWidget.PROP_INIT_DONE, true),
        new ImmutablePair<>(AbstractWidget.PROP_ENABLED_BYTE, (byte) -1),
        new ImmutablePair<>(IWidget.PROP_INHERIT_ACCESSIBILITY, true),
        new ImmutablePair<>(IWidget.PROP_CSS_CLASS, null),
        new ImmutablePair<>(IWidget.PROP_DISPOSE_DONE, false)
    ));
  }
}
