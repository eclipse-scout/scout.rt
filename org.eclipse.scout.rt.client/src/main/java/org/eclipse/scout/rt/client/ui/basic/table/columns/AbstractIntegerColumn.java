/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import org.eclipse.scout.rt.client.extension.ui.basic.table.columns.IIntegerColumnExtension;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.fields.integerfield.AbstractIntegerField;
import org.eclipse.scout.rt.client.ui.form.fields.integerfield.IIntegerField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.exception.ProcessingException;

/**
 * Column holding Integer
 */
@ClassId("5ac66db0-da85-454a-bec5-8cffa2d2abef")
public abstract class AbstractIntegerColumn extends AbstractNumberColumn<Integer> implements IIntegerColumn {

  public AbstractIntegerColumn() {
    this(true);
  }

  public AbstractIntegerColumn(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected int getConfiguredHorizontalAlignment() {
    return 1;
  }

  /*
   * Configuration
   */

  @Override
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(160)
  protected Integer getConfiguredMaxValue() {
    return null;
  }

  @Override
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(170)
  protected Integer getConfiguredMinValue() {
    return null;
  }

  /*
   * Runtime
   */

  @Override
  protected Integer parseValueInternal(ITableRow row, Object rawValue) {
    Integer validValue = null;
    if (rawValue == null) {
      validValue = null;
    }
    else if (rawValue instanceof Integer) {
      validValue = (Integer) rawValue;
    }
    else if (rawValue instanceof Number) {
      validValue = ((Number) rawValue).intValue();
    }
    else {
      throw new ProcessingException("invalid Integer value in column '" + getClass().getSimpleName() + "': " + rawValue + " class=" + rawValue.getClass());
    }
    return validValue;
  }

  @Override
  protected IIntegerField createDefaultEditor() {
    return new AbstractIntegerField() {
    };
  }

  protected static class LocalIntegerColumnExtension<OWNER extends AbstractIntegerColumn> extends LocalNumberColumnExtension<Integer, OWNER> implements IIntegerColumnExtension<OWNER> {

    public LocalIntegerColumnExtension(OWNER owner) {
      super(owner);
    }
  }

  @Override
  protected IIntegerColumnExtension<? extends AbstractIntegerColumn> createLocalExtension() {
    return new LocalIntegerColumnExtension<>(this);
  }

}
