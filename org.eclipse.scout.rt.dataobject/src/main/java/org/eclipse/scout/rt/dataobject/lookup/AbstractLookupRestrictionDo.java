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

  public static final String IDS = "ids";
  public static final String TEXT = "text";
  public static final String PARENT_IDS = "parentIds";
  public static final String ACTIVE = "active";
  public static final String ENABLED = "enabled";
  public static final String MAX_ROW_COUNT = "maxRowCount";

  /**
   * A subclass should implement this method to specify the concrete attribute type.
   *
   * @see AbstractLookupRestrictionDo#createIdsAttribute(AbstractLookupRestrictionDo)
   */
  public abstract DoList<ID> ids();

  public DoValue<String> text() {
    return doValue(TEXT);
  }

  public DoValue<Boolean> active() {
    return doValue(ACTIVE);
  }

  public DoValue<Boolean> enabled() {
    return doValue(ENABLED);
  }

  public DoValue<Integer> maxRowCount() {
    return doValue(MAX_ROW_COUNT);
  }

  /* **************************************************************************
   * HELPER METHODS
   * *************************************************************************/

  @SuppressWarnings("unchecked")
  protected SELF self() {
    return (SELF) this;
  }

  protected static <ID> DoList<ID> createIdsAttribute(AbstractLookupRestrictionDo<?, ID> self) {
    return self.doList(IDS);
  }

  /* **************************************************************************
   * CONVENIENCE METHODS
   * *************************************************************************/

  public List<ID> getIds() {
    return ids().get();
  }

  public String getText() {
    return text().get();
  }

  public Boolean getActive() {
    return active().get();
  }

  public Boolean getEnabled() {
    return enabled().get();
  }

  public Integer getMaxRowCount() {
    return maxRowCount().get();
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

  public SELF withEnabled(Boolean enabled) {
    enabled().set(enabled);
    return self();
  }

  public SELF withMaxRowCount(Integer maxRowCount) {
    maxRowCount().set(maxRowCount);
    return self();
  }
}
