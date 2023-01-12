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
import org.eclipse.scout.rt.shared.data.model.AbstractDataModel;

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=373922
 */
public class CustomDataModel extends AbstractDataModel {
  private static final long serialVersionUID = 1L;

  public CustomDataModel() {
  }

  @Order(10)
  public class CompanyEntity extends AbstractCompanyEntity {
    private static final long serialVersionUID = 1L;

    @Override
    protected boolean getConfiguredVisible() {
      return true;
    }
  }

  @Order(20)
  public class PersonEntity extends AbstractPersonEntity {
    private static final long serialVersionUID = 1L;

    @Override
    protected boolean getConfiguredVisible() {
      return true;
    }
  }

  @Order(130)
  public class InternalPersonEntity extends AbstractInternalPersonEntity {
    private static final long serialVersionUID = 1L;

    @Override
    protected boolean getConfiguredVisible() {
      return true;
    }
  }
}
