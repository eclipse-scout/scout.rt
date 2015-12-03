/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
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
import org.eclipse.scout.rt.shared.data.model.IDataModelAttribute;

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
      return IDataModelAttribute.TYPE_LONG;
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
      return IDataModelAttribute.TYPE_INTEGER;
    }

  }
}
