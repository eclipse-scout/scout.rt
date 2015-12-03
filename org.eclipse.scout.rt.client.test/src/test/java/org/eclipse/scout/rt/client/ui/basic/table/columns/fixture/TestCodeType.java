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
package org.eclipse.scout.rt.client.ui.basic.table.columns.fixture;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCode;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCodeType;

public class TestCodeType extends AbstractCodeType<Long, Long> {
  private static final long serialVersionUID = 1L;

  @Override
  public Long getId() {
    return 0L;
  }

  @Order(20)
  public class TestCode extends AbstractCode<Long> {
    private static final long serialVersionUID = 1L;
    public static final long ID = 0L;
    public static final String TEXT = "Test";

    @Override
    protected String getConfiguredText() {
      return TEXT;
    }

    @Override
    public Long getId() {
      return ID;
    }
  }
}
