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
package org.eclipse.scout.rt.client.services.common.code.fixture;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCode;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCodeType;

/**
 * Example code type for the CodeServiceClientProxyTest
 * 
 * @since Scout 4.1-M2
 */
public class CompanyTypeCodeType extends AbstractCodeType<Long, Long> {

  private static final long serialVersionUID = 1L;
  public static final Long ID = 10000L;

  public CompanyTypeCodeType() {
    super();
  }

  @Override
  protected String getConfiguredText() {
    return TEXTS.get("CompanyType");
  }

  @Override
  public Long getId() {
    return ID;
  }

  @Order(10)
  public static class CustomerCode extends AbstractCode<Long> {

    private static final long serialVersionUID = 1L;
    public static final Long ID = 10001L;

    @Override
    protected String getConfiguredText() {
      return TEXTS.get("Customer");
    }

    @Override
    public Long getId() {
      return ID;
    }
  }

  @Order(20)
  public static class SupplierCode extends AbstractCode<Long> {

    private static final long serialVersionUID = 1L;
    public static final Long ID = 10002L;

    @Override
    protected String getConfiguredText() {
      return TEXTS.get("Supplier");
    }

    @Override
    public Long getId() {
      return ID;
    }
  }

  @Order(30)
  public static class OtherCode extends AbstractCode<Long> {

    private static final long serialVersionUID = 1L;
    public static final Long ID = 10003L;

    @Override
    protected String getConfiguredText() {
      return TEXTS.get("Other");
    }

    @Override
    public Long getId() {
      return ID;
    }
  }
}
