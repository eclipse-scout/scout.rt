/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.lookup;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.TypeName;

/**
 * Generic lookup row implementation for objects using a {@code String} as identifier.
 */
@TypeName("scout.StringLookupRow")
public class StringLookupRowDo extends AbstractHierarchicalLookupRowDo<String> {

  @Override
  public DoValue<String> id() {
    return doValue(ID);
  }

  @Override
  public DoValue<String> parentId() {
    return doValue(PARENT_ID);
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public StringLookupRowDo withId(String id) {
    id().set(id);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public StringLookupRowDo withParentId(String parentId) {
    parentId().set(parentId);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public StringLookupRowDo withText(String text) {
    text().set(text);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public StringLookupRowDo withEnabled(Boolean enabled) {
    enabled().set(enabled);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public StringLookupRowDo withActive(Boolean active) {
    active().set(active);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public StringLookupRowDo withIconId(String iconId) {
    iconId().set(iconId);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public StringLookupRowDo withCssClass(String cssClass) {
    cssClass().set(cssClass);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public StringLookupRowDo withTooltipText(String tooltipText) {
    tooltipText().set(tooltipText);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public StringLookupRowDo withAdditionalTableRowData(IDoEntity additionalTableRowData) {
    additionalTableRowData().set(additionalTableRowData);
    return this;
  }
}
