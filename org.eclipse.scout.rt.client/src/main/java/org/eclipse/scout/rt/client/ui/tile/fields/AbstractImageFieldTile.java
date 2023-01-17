/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.tile.fields;

import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.imagefield.AbstractImageField;
import org.eclipse.scout.rt.client.ui.tile.fields.AbstractImageFieldTile.ImageField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;

/**
 * @since 5.2
 */
@ClassId("5b8227fe-ab75-47c8-84f1-0e7bb27bdfd0")
public abstract class AbstractImageFieldTile extends AbstractFormFieldTile<ImageField> {

  public AbstractImageFieldTile() {
    this(true);
  }

  public AbstractImageFieldTile(boolean callInitializer) {
    super(callInitializer);
  }

  /**
   * If set, this value is applied to the tile field graph's "autoFit" property.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(70)
  @SuppressWarnings("findbugs:NP_BOOLEAN_RETURN_NULL")
  protected Boolean getConfiguredAutoFit() {
    return null;
  }

  /**
   * If set, this value is applied to the tile field graph's "verticalAlignment" property.
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(70)
  protected Integer getConfiguredVerticalAlignment() {
    return null;
  }

  /**
   * If set, this value is applied to the tile field graph's "horizontalAlignment" property.
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(70)
  protected Integer getConfiguredHorizontalAlignment() {
    return null;
  }

  /**
   * If set, this value is applied to the tile field graph's "scrollBarEnabled" property.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(70)
  @SuppressWarnings("findbugs:NP_BOOLEAN_RETURN_NULL")
  protected Boolean getConfiguredScrollBarEnabled() {
    return null;
  }

  @Override
  protected void initTileWidgetConfig() {
    super.initTileWidgetConfig();

    if (getConfiguredAutoFit() != null) {
      getTileWidget().setAutoFit(getConfiguredAutoFit());
    }
    if (getConfiguredVerticalAlignment() != null) {
      GridData gd = getTileWidget().getGridDataHints();
      gd.verticalAlignment = getConfiguredVerticalAlignment();
      getTileWidget().setGridDataHints(gd);
    }
    if (getConfiguredHorizontalAlignment() != null) {
      GridData gd = getTileWidget().getGridDataHints();
      gd.horizontalAlignment = getConfiguredHorizontalAlignment();
      getTileWidget().setGridDataHints(gd);
    }
    if (getConfiguredScrollBarEnabled() != null) {
      getTileWidget().setScrollBarEnabled(getConfiguredScrollBarEnabled());
    }
  }

  @Order(10)
  @ClassId("9ae6d050-09bb-4474-b879-f1e44db267a8")
  public class ImageField extends AbstractImageField {

    @Override
    public String classId() {
      return AbstractImageFieldTile.this.classId() + ID_CONCAT_SYMBOL + ConfigurationUtility.getAnnotatedClassIdWithFallback(getClass(), true);
    }
  }
}
