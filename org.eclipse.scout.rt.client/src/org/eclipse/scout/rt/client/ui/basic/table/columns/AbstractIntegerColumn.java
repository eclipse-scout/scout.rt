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
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import org.eclipse.scout.commons.annotations.ClassId;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.fields.integerfield.AbstractIntegerField;
import org.eclipse.scout.rt.client.ui.form.fields.integerfield.IIntegerField;

/**
 * Column holding Integer
 */
@ClassId("5ac66db0-da85-454a-bec5-8cffa2d2abef")
public abstract class AbstractIntegerColumn extends AbstractNumberColumn<Integer> implements IIntegerColumn {

  public AbstractIntegerColumn() {
    super();
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
  protected Integer parseValueInternal(ITableRow row, Object rawValue) throws ProcessingException {
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
  protected IIntegerField getEditorField() {
    return new AbstractIntegerField() {
    };
  }

}
