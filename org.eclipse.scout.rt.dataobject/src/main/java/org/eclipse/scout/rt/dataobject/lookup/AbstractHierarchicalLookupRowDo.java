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

/**
 * Abstract base class for hierarchical lookup rows with generic key type T.
 *
 * @param <ID>
 *     Lookup row id type
 */
public abstract class AbstractHierarchicalLookupRowDo<ID> extends AbstractLookupRowDo<ID> {

  public static final String PARENT_ID = "parentId";

  public abstract DoValue<ID> parentId();

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public AbstractHierarchicalLookupRowDo<ID> withParentId(ID parentId) {
    parentId().set(parentId);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public ID getParentId() {
    return parentId().get();
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public AbstractHierarchicalLookupRowDo<ID> withId(ID id) {
    id().set(id);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public AbstractHierarchicalLookupRowDo<ID> withText(String text) {
    text().set(text);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public AbstractHierarchicalLookupRowDo<ID> withEnabled(Boolean enabled) {
    enabled().set(enabled);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public AbstractHierarchicalLookupRowDo<ID> withActive(Boolean active) {
    active().set(active);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public AbstractHierarchicalLookupRowDo<ID> withIconId(String iconId) {
    iconId().set(iconId);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public AbstractHierarchicalLookupRowDo<ID> withCssClass(String cssClass) {
    cssClass().set(cssClass);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public AbstractHierarchicalLookupRowDo<ID> withTooltipText(String tooltipText) {
    tooltipText().set(tooltipText);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public AbstractHierarchicalLookupRowDo<ID> withAdditionalTableRowData(IDoEntity additionalTableRowData) {
    additionalTableRowData().set(additionalTableRowData);
    return this;
  }
}
