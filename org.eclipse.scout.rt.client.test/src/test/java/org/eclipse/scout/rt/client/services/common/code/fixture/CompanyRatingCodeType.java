/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.services.common.code.fixture;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.text.TEXTS;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCode;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCodeTypeWithGeneric;
import org.eclipse.scout.rt.shared.services.common.code.ICode;

/**
 * Example code type for the CodeServiceClientProxyTest
 *
 * @since Scout 4.1-M2
 */
public class CompanyRatingCodeType extends AbstractCodeTypeWithGeneric<Long, Long, ICode<Long>> {

  private static final long serialVersionUID = 1L;
  public static final Long ID = 10100L;

  public CompanyRatingCodeType() {
    super();
  }

  @Override
  protected String getConfiguredText() {
    return TEXTS.get("CompanyRating");
  }

  @Override
  public Long getId() {
    return ID;
  }

  @Order(10)
  public static class ACode extends AbstractCode<Long> {

    private static final long serialVersionUID = 1L;
    public static final Long ID = 10101L;

    @Override
    protected String getConfiguredText() {
      return TEXTS.get("A");
    }

    @Override
    public Long getId() {
      return ID;
    }
  }

  @Order(20)
  public static class BCode extends AbstractCode<Long> {

    private static final long serialVersionUID = 1L;
    public static final Long ID = 10102L;

    @Override
    protected String getConfiguredText() {
      return TEXTS.get("B");
    }

    @Override
    public Long getId() {
      return ID;
    }
  }

  @Order(30)
  public static class CCode extends AbstractCode<Long> {

    private static final long serialVersionUID = 1L;
    public static final Long ID = 10103L;

    @Override
    protected String getConfiguredText() {
      return TEXTS.get("C");
    }

    @Override
    public Long getId() {
      return ID;
    }
  }

  @Order(40)
  public static class DCode extends AbstractCode<Long> {

    private static final long serialVersionUID = 1L;
    public static final Long ID = 10104L;

    @Override
    protected String getConfiguredText() {
      return TEXTS.get("D");
    }

    @Override
    public Long getId() {
      return ID;
    }
  }
}
