/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.spec.client.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.scout.commons.annotations.Doc.Filtering;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;

/**
 *
 */
public final class FilterUtility {

  private FilterUtility() {
  }

  /**
   * Returns <code>true</code>, if the scoutObject is accepted by all filters.
   * 
   * @param scoutObject
   * @param filters
   *          {@link IDocFilter}s (may be <code>null</code>)
   * @return <code>true</code>, if the scoutObject is accepted by all filters.
   */
  @SuppressWarnings("unchecked")
  public static <T> boolean isAccepted(T scoutObject, List<IDocFilter<T>> filters) {
    if (filters == null) {
      return true;
    }
    if (!isAccepted(scoutObject, filters, Filtering.REJECT, Filtering.TRANSPARENT)) {
      return false;
    }
    // for fields consider hierarchy
    if (scoutObject instanceof IFormField) {
      IFormField superField = ((IFormField) scoutObject).getParentField();
      while (superField != null) {
        // TODO ASA howto generics: is there a better way?
        ArrayList<IDocFilter<IFormField>> fieldFilters = new ArrayList<IDocFilter<IFormField>>();
        for (IDocFilter<T> f : filters) {
          fieldFilters.add((IDocFilter<IFormField>) f);
        }
        if (!isAccepted(superField, fieldFilters, Filtering.REJECT, Filtering.ACCEPT_REJECT_CHILDREN)) {
          return false;
        }
        superField = superField.getParentField();
      }
  
    }
    return true;
  }

  private static <T> boolean isAccepted(T scoutObject, List<IDocFilter<T>> filters, Filtering... notAccepted) {
    for (IDocFilter<T> filter : filters) {
      Filtering filterResult = filter.accept(scoutObject);
      if (Arrays.asList(notAccepted).contains(filterResult)) {
        return false;
      }
    }
    return true;
  }

}
