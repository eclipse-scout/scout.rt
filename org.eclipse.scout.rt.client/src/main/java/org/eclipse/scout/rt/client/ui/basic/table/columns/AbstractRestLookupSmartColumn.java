/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.client.services.lookup.AbstractRestLookupCall;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;

/**
 * Abstract smart column implementation with support for batch lookup calls using a {@link AbstractRestLookupCall}
 * implementation.
 * <p>
 * <b>Note:</b> This implementation uses one single lookup call instance for batch lookups, therefore
 * {@link #execPrepareLookup(ILookupCall, ITableRow)} is never called. Use
 * {@link #execPrepareLookup(AbstractRestLookupCall)} instead.
 */
@ClassId("f8f5dfa4-cca7-4c61-8d40-641e70154641")
public abstract class AbstractRestLookupSmartColumn<VALUE> extends AbstractSmartColumn<VALUE> {

  @Override
  protected abstract Class<? extends AbstractRestLookupCall<?, VALUE>> getConfiguredLookupCall();

  @SuppressWarnings("unchecked")
  @Override
  public AbstractRestLookupCall<?, VALUE> getLookupCall() {
    return (AbstractRestLookupCall<?, VALUE>) super.getLookupCall();
  }

  @Override
  public void updateDisplayTexts(List<ITableRow> rows) {
    if (rows.isEmpty()) {
      return;
    }
    // No need to call remove service when no non-null keys are present
    final Set<VALUE> keys = getValues().stream()
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
    if (keys.isEmpty()) {
      return;
    }
    AbstractRestLookupCall<?, VALUE> call = getLookupCall();
    if (call == null) {
      return;
    }
    try {
      // Create a copy of the prototype call before modifying it
      AbstractRestLookupCall<?, VALUE> lookupCall = call.copy();
      execPrepareLookup(lookupCall);
      lookupCall.setKeys(keys);
      List<? extends ILookupRow<VALUE>> dataByKey = lookupCall.getDataByKey();

      for (ITableRow row : rows) {
        List<? extends ILookupRow<VALUE>> lookupResult = dataByKey.stream()
            .filter(lookupRow -> ObjectUtility.equals(lookupRow.getKey(), row.getCell(this).getValue()))
            .collect(Collectors.toList());
        applyLookupResult(row, lookupResult);
      }
    }
    catch (RuntimeException e) {
      BEANS.get(ExceptionHandler.class).handle(e);
    }
  }

  /**
   * Called before lookup is performed. This method may be used to set any custom restrictions to lookup call.
   */
  protected void execPrepareLookup(AbstractRestLookupCall<?, VALUE> call) {
  }
}
