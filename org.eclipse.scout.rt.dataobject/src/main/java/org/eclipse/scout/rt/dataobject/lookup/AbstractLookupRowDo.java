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

/**
 * Abstract base class for lookup rows with generic key type T.
 *
 * @param <SELF>
 *          Type reference to concrete sub-class, used to implement with() methods returning concrete sub-class type
 * @param <ID>
 *          Lookup row id type
 */
public abstract class AbstractLookupRowDo<SELF extends AbstractLookupRowDo<SELF, ID>, ID> extends DoEntity {

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
    return doValue("text");
  }

  public DoValue<Boolean> enabled() {
    return doValue("enabled");
  }

  public DoValue<Boolean> active() {
    return doValue("active");
  }

  public DoValue<ID> parentId() {
    return doValue("parentId");
  }

  /* **************************************************************************
   * HELPER METHODS
   * *************************************************************************/

  @SuppressWarnings("unchecked")
  protected SELF self() {
    return (SELF) this;
  }

  protected static <ID> DoValue<ID> createIdAttribute(AbstractLookupRowDo<?, ID> self) {
    return self.doValue("id");
  }

  /* **************************************************************************
   * CONVENIENCE METHODS
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

  public SELF withParentId(ID parentId) {
    parentId().set(parentId);
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
  public Boolean isEnabled() {
    return enabled().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Boolean isActive() {
    return active().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public ID getParentId() {
    return parentId().get();
  }
}
