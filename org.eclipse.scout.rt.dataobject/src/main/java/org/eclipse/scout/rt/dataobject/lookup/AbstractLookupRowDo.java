/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.dataobject.lookup;

import javax.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.IDoEntity;

/**
 * Abstract base class for lookup rows with generic key type T.
 *
 * @param <SELF>
 *          Type reference to concrete sub-class, used to implement with() methods returning concrete sub-class type
 * @param <ID>
 *          Lookup row id type
 */
public abstract class AbstractLookupRowDo<SELF extends AbstractLookupRowDo<SELF, ID>, ID> extends DoEntity {

  public static final String ID = "id";
  public static final String TEXT = "text";
  public static final String ENABLED = "enabled";
  public static final String ACTIVE = "active";

  protected AbstractLookupRowDo() {
    withEnabled(true); // lookup rows are enabled by default
    withActive(true); // lookup rows are active by default
  }

  /**
   * A subclass should implement this method to specify the concrete attribute type.
   *
   * @see AbstractLookupRowDo#createIdAttribute(AbstractLookupRowDo)
   */
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
    return doValue("iconId");
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
   * HELPER METHODS
   * *************************************************************************/

  @SuppressWarnings("unchecked")
  protected SELF self() {
    return (SELF) this;
  }

  protected static <ID> DoValue<ID> createIdAttribute(AbstractLookupRowDo<?, ID> self) {
    return self.doValue(ID);
  }

  /* **************************************************************************
   * CUSTOM CONVENIENCE METHODS
   * *************************************************************************/

  public SELF withId(ID id) {
    id().set(id);
    return self();
  }

  public SELF withText(String text) {
    text().set(text);
    return self();
  }

  public SELF withEnabled(Boolean enabled) {
    enabled().set(enabled);
    return self();
  }

  public SELF withActive(Boolean active) {
    active().set(active);
    return self();
  }

  public SELF withIconId(String iconId) {
    iconId().set(iconId);
    return self();
  }

  public SELF withCssClass(String cssClass) {
    cssClass().set(cssClass);
    return self();
  }

  public SELF withTooltipText(String tooltipText) {
    tooltipText().set(tooltipText);
    return self();
  }

  public SELF withAdditionalTableRowData(IDoEntity additionalTableRowData) {
    additionalTableRowData().set(additionalTableRowData);
    return self();
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public ID getId() {
    return id().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getText() {
    return text().get();
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
  public Boolean getActive() {
    return active().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public boolean isActive() {
    return nvl(getActive());
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getIconId() {
    return iconId().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getCssClass() {
    return cssClass().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getTooltipText() {
    return tooltipText().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public IDoEntity getAdditionalTableRowData() {
    return additionalTableRowData().get();
  }
}
