/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.tile.fields;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.form.fields.ModelVariant;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.client.ui.tile.AbstractFormFieldTile;
import org.eclipse.scout.rt.client.ui.tile.fields.AbstractTableTile.TableField;
import org.eclipse.scout.rt.client.ui.tile.fields.AbstractTableTile.TableField.Table;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;

/**
 * @since 5.2
 */
@ClassId("78275a97-05ba-405a-a92b-1c6d6427683b")
public abstract class AbstractTableTile extends AbstractFormFieldTile<TableField> {

  public AbstractTableTile() {
    this(true);
  }

  public AbstractTableTile(boolean callInitializer) {
    super(callInitializer);
  }

  /**
   * If set, this value is applied to the tile field table's "autoResizeColumns" property.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(70)
  @SuppressWarnings("findbugs:NP_BOOLEAN_RETURN_NULL")
  protected Boolean getConfiguredAutoResizeColumns() {
    return null;
  }

  /**
   * If set, this value is applied to the tile field table's "headerVisible" property.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(70)
  @SuppressWarnings("findbugs:NP_BOOLEAN_RETURN_NULL")
  protected Boolean getConfiguredHeaderVisible() {
    return null;
  }

  /**
   * If set, this value is applied to the tile field table's "headerEnabled" property.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(70)
  @SuppressWarnings("findbugs:NP_BOOLEAN_RETURN_NULL")
  protected Boolean getConfiguredHeaderEnabled() {
    return null;
  }

  @ConfigOperation
  @Order(120)
  protected void execAppLinkAction(String ref) {
  }

  @Override
  protected void initTileWidgetConfig() {
    super.initTileWidgetConfig();

    if (getConfiguredAutoResizeColumns() != null) {
      getTileWidget().getTable().setAutoResizeColumns(getConfiguredAutoResizeColumns());
    }
    if (getConfiguredHeaderVisible() != null) {
      getTileWidget().getTable().setHeaderVisible(getConfiguredHeaderVisible());
    }
    if (getConfiguredHeaderEnabled() != null) {
      getTileWidget().getTable().setHeaderEnabled(getConfiguredHeaderEnabled());
    }
  }

  /**
   * Override this internal method only in order to make use of dynamic columns<br>
   * To change the order or specify the insert position use {@link IColumn#setOrder(double)}.
   *
   * @param columns
   *          live and mutable collection of configured columns, not yet initialized
   */
  protected void injectColumnsInternal(OrderedCollection<IColumn<?>> columns) {
  }

  /**
   * Override this internal method only in order to make use of dynamic menus<br>
   * Used to manage menu list and add/remove menus.<br>
   * To change the order or specify the insert position use {@link IMenu#setOrder(double)}.
   *
   * @param menus
   *          live and mutable collection of configured menus
   */
  protected void injectMenusInternal(OrderedCollection<IMenu> menus) {
  }

  @Order(10)
  @ModelVariant("Tile")
  @ClassId("1f8e30e8-6e7d-4bff-aa52-6a6d061d394d")
  public class TableField extends AbstractTableField<Table> {

    @Override
    public String classId() {
      return AbstractTableTile.this.classId() + ID_CONCAT_SYMBOL + ConfigurationUtility.getAnnotatedClassIdWithFallback(getClass(), true);
    }

    @ClassId("787e34b0-e6a7-4ebb-9b24-8e5361280bcf")
    public class Table extends AbstractTable {

      @Override
      protected boolean getConfiguredAutoResizeColumns() {
        return true;
      }

      @Override
      protected void addHeaderMenus(OrderedCollection<IMenu> menus) {
        // do not show any header menus (no gear-wheel menu)
      }

      // Enable header to use resize and move columns but disable sort and header menus in java script TileTableField.js because if getConfiguredHeaderEnabled is disabled whole header functionality is disabled.
      // And if getConfiguredSortEnabled is disabled sorting is not done in CoreTableTile because it sorts over GUI.

      @Override
      protected void execAppLinkAction(String ref) {
        AbstractTableTile.this.execAppLinkAction(ref);
      }

      @Override
      protected void injectColumnsInternal(OrderedCollection<IColumn<?>> columnList) {
        super.injectColumnsInternal(columnList);
        AbstractTableTile.this.injectColumnsInternal(columnList);
      }

      @Override
      protected void injectMenusInternal(OrderedCollection<IMenu> menus) {
        super.injectMenusInternal(menus);
        AbstractTableTile.this.injectMenusInternal(menus);
      }
    }
  }
}
