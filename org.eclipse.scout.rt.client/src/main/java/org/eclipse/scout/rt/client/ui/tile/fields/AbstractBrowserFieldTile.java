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

import java.util.EnumSet;

import org.eclipse.scout.rt.client.ui.form.fields.browserfield.AbstractBrowserField;
import org.eclipse.scout.rt.client.ui.form.fields.browserfield.IBrowserField.SandboxPermission;
import org.eclipse.scout.rt.client.ui.tile.fields.AbstractBrowserFieldTile.BrowserField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;

/**
 * @since 5.2
 */
@ClassId("9228f159-bc81-4dba-998c-168e0453865c")
public abstract class AbstractBrowserFieldTile extends AbstractFormFieldTile<BrowserField> {

  public AbstractBrowserFieldTile() {
    this(true);
  }

  public AbstractBrowserFieldTile(boolean callInitializer) {
    super(callInitializer);
  }

  /**
   * If set, this value is applied to the tile field's "sandboxEnabled" property.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(70)
  @SuppressWarnings("findbugs:NP_BOOLEAN_RETURN_NULL")
  protected Boolean getConfiguredSandboxEnabled() {
    return null;
  }

  /**
   * If set, this value is applied to the tile field's "sandboxPermissions" property.
   */
  @ConfigProperty(ConfigProperty.OBJECT)
  @Order(70)
  protected EnumSet<SandboxPermission> getConfiguredSandboxPermissions() {
    return null;
  }

  @Override
  protected void initTileWidgetConfig() {
    super.initTileWidgetConfig();

    if (getConfiguredSandboxEnabled() != null) {
      getTileWidget().setSandboxEnabled(getConfiguredSandboxEnabled());
    }
    if (getConfiguredSandboxPermissions() != null) {
      getTileWidget().setSandboxPermissions(getConfiguredSandboxPermissions());
    }
  }

  @Order(10)
  @ClassId("2cd7e44c-c3fe-4d03-9b21-029e94982c01")
  public class BrowserField extends AbstractBrowserField {

    @Override
    public String classId() {
      return AbstractBrowserFieldTile.this.classId() + ID_CONCAT_SYMBOL + ConfigurationUtility.getAnnotatedClassIdWithFallback(getClass(), true);
    }
  }
}
