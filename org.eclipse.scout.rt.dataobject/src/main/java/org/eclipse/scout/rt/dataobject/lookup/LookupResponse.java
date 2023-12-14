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

import java.util.Collection;
import java.util.List;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.platform.BEANS;

@TypeName("scout.LookupResponse")
@SuppressWarnings("unchecked")
public class LookupResponse<T extends AbstractLookupRowDo<?>> extends DoEntity {

  public DoList<T> rows() {
    return doList("rows");
  }

  /**
   * Convenience method to create a {@link LookupResponse} with specified collection of rows.
   */
  public static <T extends AbstractLookupRowDo<?>> LookupResponse<T> create(Collection<T> rows) {
    return BEANS.get(LookupResponse.class).withRows(rows);
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public LookupResponse<T> withRows(Collection<? extends T> rows) {
    rows().updateAll(rows);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public LookupResponse<T> withRows(T... rows) {
    rows().updateAll(rows);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<T> getRows() {
    return rows().get();
  }
}
