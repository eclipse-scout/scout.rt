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

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.IDoEntity;

/**
 * Abstract base class for lookup rows with generic key type T.
 *
 * @param <ID>
 *          Lookup row id type
 */
public abstract class AbstractLookupRowDo<ID> extends DoEntity {

  public static final String ID = "id";
  public static final String TEXT = "text";
  public static final String ENABLED = "enabled";
  public static final String ACTIVE = "active";
  public static final String ICON_ID = "iconId";

  protected AbstractLookupRowDo() {
    withEnabled(true); // lookup rows are enabled by default
    withActive(true); // lookup rows are active by default
  }

  public abstract DoValue<ID> id();

  public DoValue<String> text() {
    return doValue(TEXT);
  }

  public DoValue<Boolean> enabled() {
    return doValue(ENABLED);
  }

  public DoValue<Boolean> active() {
    return doValue(ACTIVE);
  }

  public DoValue<String> iconId() {
    return doValue(ICON_ID);
  }

  public DoValue<String> cssClass() {
    return doValue("cssClass");
  }

  public DoValue<String> tooltipText() {
    return doValue("tooltipText");
  }

  public DoValue<IDoEntity> additionalTableRowData() {
    return doValue("additionalTableRowData");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public AbstractLookupRowDo<ID> withId(ID id) {
    id().set(id);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public ID getId() {
    return id().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public AbstractLookupRowDo<ID> withText(String text) {
    text().set(text);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getText() {
    return text().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public AbstractLookupRowDo<ID> withEnabled(Boolean enabled) {
    enabled().set(enabled);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Boolean getEnabled() {
    return enabled().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public boolean isEnabled() {
    return nvl(getEnabled());
  }

  @Generated("DoConvenienceMethodsGenerator")
  public AbstractLookupRowDo<ID> withActive(Boolean active) {
    active().set(active);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Boolean getActive() {
    return active().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public boolean isActive() {
    return nvl(getActive());
  }

  @Generated("DoConvenienceMethodsGenerator")
  public AbstractLookupRowDo<ID> withIconId(String iconId) {
    iconId().set(iconId);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getIconId() {
    return iconId().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public AbstractLookupRowDo<ID> withCssClass(String cssClass) {
    cssClass().set(cssClass);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getCssClass() {
    return cssClass().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public AbstractLookupRowDo<ID> withTooltipText(String tooltipText) {
    tooltipText().set(tooltipText);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getTooltipText() {
    return tooltipText().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public AbstractLookupRowDo<ID> withAdditionalTableRowData(IDoEntity additionalTableRowData) {
    additionalTableRowData().set(additionalTableRowData);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public IDoEntity getAdditionalTableRowData() {
    return additionalTableRowData().get();
  }
}
