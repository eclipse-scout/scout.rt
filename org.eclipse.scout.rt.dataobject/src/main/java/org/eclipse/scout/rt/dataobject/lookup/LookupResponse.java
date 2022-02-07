/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.dataobject.lookup;

import java.util.Collection;
import java.util.List;

import javax.annotation.Generated;

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
