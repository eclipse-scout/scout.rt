/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui.tile.fields;

import org.eclipse.scout.rt.client.ui.form.fields.ModelVariant;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractButton;
import org.eclipse.scout.rt.client.ui.tile.fields.AbstractButtonTile.Button;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;

/**
 * @since 5.2
 */
@ClassId("d5a8ec1e-92bd-4d5a-bbcf-0c7ecb9b5994")
public abstract class AbstractButtonTile extends AbstractFormFieldTile<Button> {

  public AbstractButtonTile() {
    this(true);
  }

  public AbstractButtonTile(boolean callInitializer) {
    super(callInitializer);
  }

  /**
   * If set, this value is applied to the tile field's "iconId" property.
   */
  @ConfigProperty(ConfigProperty.STRING)
  @Order(70)
  protected String getConfiguredIconId() {
    return null;
  }

  /**
   * If set, this value is applied to the tile field's "preventDoubleClick" property.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(200)
  @SuppressWarnings("findbugs:NP_BOOLEAN_RETURN_NULL")
  protected Boolean getConfiguredPreventDoubleClick() {
    return null;
  }

  @ConfigOperation
  @Order(190)
  protected void execClickAction() {
  }

  @Override
  protected void initTileWidgetConfig() {
    super.initTileWidgetConfig();

    if (getConfiguredIconId() != null) {
      getTileWidget().setIconId(getConfiguredIconId());
    }
    if (getConfiguredPreventDoubleClick() != null) {
      getTileWidget().setPreventDoubleClick(getConfiguredPreventDoubleClick());
    }
  }

  @Order(10)
  @ModelVariant("Tile")
  @ClassId("724c526a-9423-4e98-884e-504639948448")
  public class Button extends AbstractButton {

    @Override
    public String classId() {
      return AbstractButtonTile.this.classId() + ID_CONCAT_SYMBOL + ConfigurationUtility.getAnnotatedClassIdWithFallback(getClass(), true);
    }

    @Override
    protected boolean getConfiguredProcessButton() {
      return false;
    }

    @Override
    protected void execClickAction() {
      AbstractButtonTile.this.execClickAction();
    }
  }
}
