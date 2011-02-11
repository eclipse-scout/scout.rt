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
package org.eclipse.scout.rt.client.ui.basic.table;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.ISmartColumn;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.rt.shared.services.lookup.LocalLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.eclipse.scout.service.SERVICES;

/**
 * XXX these methods are from AbstractTable, move them over here in release jun/2011.
 */
public final class TableUtility {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(TableUtility.class);

  private TableUtility() {
  }

  /**
   * synchronous resolving of lookup values to text
   * <p>
   * Note that remote lookup calls are evaluated one by one, no batch processing.
   */
  public static void resolveLookupCall(Map<LookupCall, LookupRow[]> lookupCache, ITableRow row, ISmartColumn<?> col, boolean multilineText) {
    try {
      LookupCall call = col.prepareLookupCall(row);
      if (call != null) {
        LookupRow[] result = null;
        boolean verifiedQuality = verifyLookupCallBeanQuality(call);
        //optimize local calls by caching the results
        if (verifiedQuality) {
          result = lookupCache.get(call);
        }
        if (result == null) {
          result = call.getDataByKey();
          if (verifiedQuality) {
            lookupCache.put(call, result);
          }
        }
        applyLookupResult(row, col, result, multilineText);
      }
    }
    catch (ProcessingException e) {
      if (e.isInterruption()) {
        // nop
      }
      else {
        SERVICES.getService(IExceptionHandlerService.class).handleException(e);
      }
    }
  }

  /**
   * In order to use caching of results on lookup calls, it is crucial that the javabean concepts are valid,
   * especially hashCode and equals (when the subclass has additional member fields).
   * <p>
   * Scout tries to help developers to find problems related to this issue and write a warning in development mode on
   * all local lookup call subclasses that do not overwrite hashCode and equals.
   */
  public static boolean verifyLookupCallBeanQuality(LookupCall call) {
    Class<?> clazz = call.getClass();
    if (LocalLookupCall.class == clazz) {
      return true;
    }
    if (LookupCall.class == clazz) {
      return true;
    }
    if (LocalLookupCall.class.isAssignableFrom(clazz)) {
      Class<?> tmp = clazz;
      while (tmp != LocalLookupCall.class) {
        if (ConfigurationUtility.isMethodOverwrite(LocalLookupCall.class, "equals", new Class[]{Object.class}, tmp)) {
          return true;
        }
        Field[] fields = tmp.getDeclaredFields();
        if (fields != null && fields.length > 0) {
          for (int i = 0; i < fields.length; i++) {
            if ((fields[i].getModifiers() & (Modifier.STATIC | Modifier.FINAL)) == 0) {
              LOG.warn("" + clazz + " subclasses LocalLookupCall with additional member " + fields[i].getName() + " and should therefore override the 'equals' and 'hashCode' methods");
              return false;
            }
          }
        }
        //next
        tmp = tmp.getSuperclass();
      }
      return true;
    }
    if (LookupCall.class.isAssignableFrom(clazz)) {
      Class<?> tmp = clazz;
      while (tmp != LookupCall.class) {
        if (ConfigurationUtility.isMethodOverwrite(LookupCall.class, "equals", new Class[]{Object.class}, tmp)) {
          return true;
        }
        Field[] fields = tmp.getDeclaredFields();
        if (fields != null && fields.length > 0) {
          for (int i = 0; i < fields.length; i++) {
            if ((fields[i].getModifiers() & (Modifier.STATIC | Modifier.FINAL)) == 0) {
              LOG.warn("" + clazz + " subclasses LookupCall with additional member " + fields[i].getName() + " and should therefore override the 'equals' and 'hashCode' methods");
              return false;
            }
          }
        }
        //next
        tmp = tmp.getSuperclass();
      }
      return true;
    }
    return false;
  }

  public static void applyLookupResult(ITableRow row, IColumn<?> col, LookupRow[] result, boolean multilineText) {
    // disable row changed trigger on row
    try {
      row.setRowChanging(true);
      //
      Cell cell = (Cell) row.getCell(col.getColumnIndex());
      if (result.length == 1) {
        cell.setText(result[0].getText());
      }
      else if (result.length > 1) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < result.length; i++) {
          if (i > 0) {
            if (multilineText) buf.append("\n");
            else buf.append(", ");
          }
          buf.append(result[i].getText());
        }
        cell.setText(buf.toString());
      }
      else {
        cell.setText("");
      }
    }
    finally {
      row.setRowPropertiesChanged(false);
      row.setRowChanging(false);
    }
  }
}
