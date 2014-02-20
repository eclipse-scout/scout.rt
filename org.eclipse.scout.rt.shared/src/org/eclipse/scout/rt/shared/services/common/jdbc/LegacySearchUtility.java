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
package org.eclipse.scout.rt.shared.services.common.jdbc;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.NVPair;

/**
 * @deprecated Will be removed in the M-Release.
 */
@Deprecated
@SuppressWarnings("deprecation")
public final class LegacySearchUtility {

  private LegacySearchUtility() {
  }

  public static Map<String, Object> getBindMap(SearchFilter filter) {
    if (filter instanceof LegacySearchFilter) {
      return ((LegacySearchFilter) filter).getBindMap();
    }
    else {
      return new HashMap<String, Object>();
    }
  }

  public static void addBind(SearchFilter filter, String name, Object value) {
    if (filter instanceof LegacySearchFilter) {
      ((LegacySearchFilter) filter).addBind(name, value);
    }
    else {
      throw new IllegalArgumentException("expected a filter of " + LegacySearchFilter.class + "; got " + (filter != null ? filter.getClass() : null));
    }
  }

  public static void setWhere(SearchFilter filter, String sql, NVPair... customBinds) {
    if (filter instanceof LegacySearchFilter) {
      ((LegacySearchFilter) filter).setWhere(sql, customBinds);
    }
    else {
      throw new IllegalArgumentException("expected a filter of " + LegacySearchFilter.class + "; got " + (filter != null ? filter.getClass() : null));
    }
  }

  public static void setWhereToken(SearchFilter filter, String sql) {
    if (filter instanceof LegacySearchFilter) {
      ((LegacySearchFilter) filter).setWhereToken(sql);
    }
    else {
      throw new IllegalArgumentException("expected a filter of " + LegacySearchFilter.class + "; got " + (filter != null ? filter.getClass() : null));
    }
  }

  public static void setWhereToken(SearchFilter filter, String sql, Object valueForS) {
    if (filter instanceof LegacySearchFilter) {
      ((LegacySearchFilter) filter).setWhereToken(sql, valueForS);
    }
    else {
      throw new IllegalArgumentException("expected a filter of " + LegacySearchFilter.class + "; got " + (filter != null ? filter.getClass() : null));
    }
  }

  public static void addWhereToken(SearchFilter filter, String sql) {
    if (filter instanceof LegacySearchFilter) {
      ((LegacySearchFilter) filter).addWhereToken(sql);
    }
    else {
      throw new IllegalArgumentException("expected a filter of " + LegacySearchFilter.class + "; got " + (filter != null ? filter.getClass() : null));
    }
  }

  public static void addWhereToken(SearchFilter filter, String sql, Object valueForS) {
    if (filter instanceof LegacySearchFilter) {
      ((LegacySearchFilter) filter).addWhereToken(sql, valueForS);
    }
    else {
      throw new IllegalArgumentException("expected a filter of " + LegacySearchFilter.class + "; got " + (filter != null ? filter.getClass() : null));
    }
  }

  public static void addWhere(SearchFilter filter, String sql, NVPair... customBinds) {
    if (filter instanceof LegacySearchFilter) {
      ((LegacySearchFilter) filter).addWhere(sql, customBinds);
    }
    else {
      throw new IllegalArgumentException("expected a filter of " + LegacySearchFilter.class + "; got " + (filter != null ? filter.getClass() : null));
    }
  }

  public static String getWhere(SearchFilter filter) {
    if (filter instanceof LegacySearchFilter) {
      return ((LegacySearchFilter) filter).getWhere();
    }
    else {
      return "";
    }
  }

  public static String getWherePlain(SearchFilter filter) throws ProcessingException {
    if (filter instanceof LegacySearchFilter) {
      return ((LegacySearchFilter) filter).getWherePlain();
    }
    else {
      return "";
    }
  }
}
