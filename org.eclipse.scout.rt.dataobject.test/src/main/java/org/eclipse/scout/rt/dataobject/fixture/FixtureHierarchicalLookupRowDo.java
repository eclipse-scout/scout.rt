/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.fixture;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.lookup.AbstractHierarchicalLookupRowDo;

@TypeName("scout.FixtureHierarchicalLookupRow")
public class FixtureHierarchicalLookupRowDo extends AbstractHierarchicalLookupRowDo<FixtureUuId> {

  @Override
  public DoValue<FixtureUuId> id() {
    return doValue(ID);
  }

  @Override
  public DoValue<FixtureUuId> parentId() {
    return doValue(PARENT_ID);
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public FixtureHierarchicalLookupRowDo withId(FixtureUuId id) {
    id().set(id);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public FixtureHierarchicalLookupRowDo withParentId(FixtureUuId parentId) {
    parentId().set(parentId);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public FixtureHierarchicalLookupRowDo withText(String text) {
    text().set(text);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public FixtureHierarchicalLookupRowDo withEnabled(Boolean enabled) {
    enabled().set(enabled);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public FixtureHierarchicalLookupRowDo withActive(Boolean active) {
    active().set(active);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public FixtureHierarchicalLookupRowDo withIconId(String iconId) {
    iconId().set(iconId);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public FixtureHierarchicalLookupRowDo withCssClass(String cssClass) {
    cssClass().set(cssClass);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public FixtureHierarchicalLookupRowDo withTooltipText(String tooltipText) {
    tooltipText().set(tooltipText);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public FixtureHierarchicalLookupRowDo withAdditionalTableRowData(IDoEntity additionalTableRowData) {
    additionalTableRowData().set(additionalTableRowData);
    return this;
  }
}
