/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.lookup.fixture;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.lookup.AbstractLookupRowDo;

@TypeName("start.FixtureDataLookupRow")
public class FixtureDataLookupRowDo extends AbstractLookupRowDo<Long> {

  @Override
  public DoValue<Long> id() {
    return doValue(ID);
  }

  public DoValue<String> additionalData() {
    return doValue("additionalData");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public FixtureDataLookupRowDo withId(Long id) {
    id().set(id);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public FixtureDataLookupRowDo withAdditionalData(String additionalData) {
    additionalData().set(additionalData);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getAdditionalData() {
    return additionalData().get();
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public FixtureDataLookupRowDo withText(String text) {
    text().set(text);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public FixtureDataLookupRowDo withEnabled(Boolean enabled) {
    enabled().set(enabled);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public FixtureDataLookupRowDo withActive(Boolean active) {
    active().set(active);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public FixtureDataLookupRowDo withIconId(String iconId) {
    iconId().set(iconId);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public FixtureDataLookupRowDo withCssClass(String cssClass) {
    cssClass().set(cssClass);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public FixtureDataLookupRowDo withTooltipText(String tooltipText) {
    tooltipText().set(tooltipText);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public FixtureDataLookupRowDo withAdditionalTableRowData(IDoEntity additionalTableRowData) {
    additionalTableRowData().set(additionalTableRowData);
    return this;
  }
}
