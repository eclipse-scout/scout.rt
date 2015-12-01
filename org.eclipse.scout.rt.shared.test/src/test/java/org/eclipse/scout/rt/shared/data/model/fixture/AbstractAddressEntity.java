/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.data.model.fixture;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.data.model.AbstractDataModelAttribute;
import org.eclipse.scout.rt.shared.data.model.AbstractDataModelEntity;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttribute;

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
      return IDataModelAttribute.TYPE_STRING;
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
      return IDataModelAttribute.TYPE_STRING;
    }
  }
}
