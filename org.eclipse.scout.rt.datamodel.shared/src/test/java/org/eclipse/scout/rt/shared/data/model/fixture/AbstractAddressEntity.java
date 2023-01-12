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
import org.eclipse.scout.rt.shared.data.model.AbstractDataModelEntity;

public abstract class AbstractAddressEntity extends AbstractDataModelEntity {
  private static final long serialVersionUID = 1L;

  @Override
  protected String getConfiguredText() {
    return "Address";
  }

  @Order(10)
  public class CityAttribute extends AbstractDataModelAttribute {
    private static final long serialVersionUID = 1L;

    @Override
    protected String getConfiguredText() {
      return "City";
    }

    @Override
    protected int getConfiguredType() {
      return TYPE_STRING;
    }
  }

  @Order(10)
  public class ZipCodeAttribute extends AbstractDataModelAttribute {
    private static final long serialVersionUID = 1L;

    @Override
    protected String getConfiguredText() {
      return "Zip";
    }

    @Override
    protected int getConfiguredType() {
      return TYPE_STRING;
    }
  }
}
