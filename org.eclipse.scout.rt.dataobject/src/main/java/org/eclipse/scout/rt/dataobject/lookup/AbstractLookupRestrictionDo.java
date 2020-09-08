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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.DoValue;

/**
 * Abstract base class for lookup call restrictions
 *
 * @param <SELF>
 *          Type reference to concrete sub-class, used to implement with() methods returning concrete sub-class type
 * @param <ID>
 *          Lookup row id type
 */
public abstract class AbstractLookupRestrictionDo<SELF extends AbstractLookupRestrictionDo<SELF, ID>, ID> extends DoEntity {

  /**
   * A subclass should implement this method to specify the concrete attribute type.
   *
   * @see AbstractLookupRestrictionDo#createIdsAttribute(AbstractLookupRestrictionDo)
   */
  public abstract DoList<ID> ids();

  public DoValue<String> text() {
    return doValue("text");
  }

  public DoValue<Boolean> active() {
    return doValue("active");
  }

  public DoValue<Integer> maxRowCount() {
    return doValue("maxRowCount");
  }

  /* **************************************************************************
   * HELPER METHODS
   * *************************************************************************/

  @SuppressWarnings("unchecked")
  protected SELF self() {
    return (SELF) this;
  }

  protected static <ID> DoList<ID> createIdsAttribute(AbstractLookupRestrictionDo<?, ID> self) {
    return self.doList("ids");
  }

  /* **************************************************************************
   * CONVENIENCE METHODS
   * *************************************************************************/

  public List<ID> getIds() {
    return ids().get();
  }

  public Boolean getActive() {
    return active().get();
  }

  public SELF withIds(Collection<? extends ID> ids) {
    ids().clear();
    ids().get().addAll(ids);
    return self();
  }

  public SELF withIds(@SuppressWarnings("unchecked") ID... ids) {
    return withIds(Arrays.asList(ids));
  }

  public SELF withText(String text) {
    text().set(text);
    return self();
  }

  public SELF withActive(Boolean active) {
    active().set(active);
    return self();
  }

  public SELF withMaxRowCount(Integer maxRowCount) {
    maxRowCount().set(maxRowCount);
    return self();
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public String getText() {
    return text().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Integer getMaxRowCount() {
    return maxRowCount().get();
  }
}
