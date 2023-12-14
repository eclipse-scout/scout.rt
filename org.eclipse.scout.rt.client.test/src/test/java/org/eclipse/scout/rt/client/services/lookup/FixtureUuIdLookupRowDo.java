/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.services.lookup;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.fixture.FixtureUuId;
import org.eclipse.scout.rt.dataobject.lookup.AbstractLookupRowDo;

@TypeName("scout.FixtureUuIdLookupRow")
public class FixtureUuIdLookupRowDo extends AbstractLookupRowDo<FixtureUuId> {

  @Override
  public DoValue<FixtureUuId> id() {
    return doValue(ID);
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public FixtureUuIdLookupRowDo withId(FixtureUuId id) {
    id().set(id);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public FixtureUuIdLookupRowDo withText(String text) {
    text().set(text);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public FixtureUuIdLookupRowDo withEnabled(Boolean enabled) {
    enabled().set(enabled);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public FixtureUuIdLookupRowDo withActive(Boolean active) {
    active().set(active);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public FixtureUuIdLookupRowDo withIconId(String iconId) {
    iconId().set(iconId);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public FixtureUuIdLookupRowDo withCssClass(String cssClass) {
    cssClass().set(cssClass);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public FixtureUuIdLookupRowDo withTooltipText(String tooltipText) {
    tooltipText().set(tooltipText);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public FixtureUuIdLookupRowDo withAdditionalTableRowData(IDoEntity additionalTableRowData) {
    additionalTableRowData().set(additionalTableRowData);
    return this;
  }
}
