/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.data.model;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.text.TEXTS;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCode;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCodeType;

/**
 * CodeType to define the values available in the SmartField of the extended search attribute
 * {@link org.eclipse.scout.rt.shared.data.model.DataModelAttributeOperatorProvider.YearToDate}. Projects in need of
 * more values can implement their own CodeType extending this one an adding the
 * {@link org.eclipse.scout.rt.platform.Replace} annotation.
 */
@ClassId("343e6146-7bc0-49c7-9da2-cdd56dbd1676")
public class YearToDateCodeType extends AbstractCodeType<Long, Integer> {

  private static final long serialVersionUID = 1L;
  public static final long ID = 170873L;

  @Override
  public Long getId() {
    return ID;
  }

  public YearToDateCodeType() {
    this(true);
  }

  protected YearToDateCodeType(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected String getConfiguredText() {
    return TEXTS.get("YearToDate");
  }

  @Override
  protected String getConfiguredTextPlural() {
    return TEXTS.get("YearToDate");
  }

  @Order(-3)
  @ClassId("5767462c-fc9c-43b3-bb01-0f9ef708bab4")
  public static class ThreeYearsBeforeCode extends AbstractCode<Integer> {

    private static final long serialVersionUID = 1L;
    public static final Integer ID = -3;

    @Override
    protected String getConfiguredText() {
      return TEXTS.get("YearToDateThreeYearsBefore");
    }

    @Override
    public Integer getId() {
      return ID;
    }
  }

  @Order(-2)
  @ClassId("eba3b867-9036-4963-9209-24ca9d77be7e")
  public static class TwoYearsBeforeCode extends AbstractCode<Integer> {

    private static final long serialVersionUID = 1L;
    public static final Integer ID = -2;

    @Override
    protected String getConfiguredText() {
      return TEXTS.get("YearToDateTwoYearsBefore");
    }

    @Override
    public Integer getId() {
      return ID;
    }
  }

  @Order(-1)
  @ClassId("abd8f2d8-b1ad-4850-91e6-0633274825f6")
  public static class OneYearBeforeCode extends AbstractCode<Integer> {

    private static final long serialVersionUID = 1L;
    public static final Integer ID = -1;

    @Override
    protected String getConfiguredText() {
      return TEXTS.get("YearToDateLastYear");
    }

    @Override
    public Integer getId() {
      return ID;
    }
  }

  @Order(0)
  @ClassId("74b3ca8b-d33a-4668-95f3-168456d61bf5")
  public static class ThisYearCode extends AbstractCode<Integer> {

    private static final long serialVersionUID = 1L;
    public static final Integer ID = 0;

    @Override
    protected String getConfiguredText() {
      return TEXTS.get("YearToDateThisYear");
    }

    @Override
    public Integer getId() {
      return ID;
    }
  }

  @Order(1)
  @ClassId("3058dc55-c1dc-42fe-8ed0-287f4dc5f171")
  public static class NextYearCode extends AbstractCode<Integer> {

    private static final long serialVersionUID = 1L;
    public static final Integer ID = 1;

    @Override
    protected String getConfiguredText() {
      return TEXTS.get("YearToDateNextYear");
    }

    @Override
    public Integer getId() {
      return ID;
    }
  }
}
