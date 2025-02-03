/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.data.model.fixture;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.data.model.AbstractDataModelAttribute;

public abstract class AbstractInternalPersonEntity extends AbstractPersonEntity {

  private static final long serialVersionUID = 1L;

  @Override
  protected String getConfiguredText() {
    return "Employee";
  }

  @Order(10)
  public class SalaryTypeAttribute extends AbstractDataModelAttribute {
    private static final long serialVersionUID = 1L;

    @Override
    protected String getConfiguredText() {
      return "SalaryType";
    }

    @Override
    protected int getConfiguredType() {
      return TYPE_LONG;
    }
  }

  @Order(10)
  public class PartTimeAttribute extends AbstractDataModelAttribute {
    private static final long serialVersionUID = 1L;

    @Override
    protected String getConfiguredText() {
      return "PartTimePercent";
    }

    @Override
    protected int getConfiguredType() {
      return TYPE_INTEGER;
    }
  }
}
