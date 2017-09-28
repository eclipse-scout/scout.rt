/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.admin.diagnostic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DiagnosticFactory {

  public static final String STATUS_TITLE = "TITLE";
  public static final String STATUS_INFO = "INFO";
  public static final String STATUS_FAILED = "FAILED";
  public static final String STATUS_OK = "OK";
  public static final String STATUS_ACTIVE = "ACTIVE";
  public static final String STATUS_INACTIVE = "INACTIVE";

  private static final List<IDiagnostic> DIAGNOSTIC_STATUS_PROVIDERS;
  private static final Map<String/*action*/, IDiagnostic> DIAGNOSTIC_STATUS_PROVIDER_ACTION_MAP;

  static {
    DIAGNOSTIC_STATUS_PROVIDERS = new ArrayList<>();
    DIAGNOSTIC_STATUS_PROVIDER_ACTION_MAP = new HashMap<>();
  }

  private DiagnosticFactory() {
  }

  public static void addDiagnosticStatusProvider(IDiagnostic statusProvider) {
    synchronized (DIAGNOSTIC_STATUS_PROVIDERS) {
      DIAGNOSTIC_STATUS_PROVIDERS.add(statusProvider);
    }
  }

  public static void removeDiagnosticStatusProvider(IDiagnostic statusProvider) {
    synchronized (DIAGNOSTIC_STATUS_PROVIDERS) {
      DIAGNOSTIC_STATUS_PROVIDERS.remove(statusProvider);
    }
    synchronized (DIAGNOSTIC_STATUS_PROVIDER_ACTION_MAP) {
      DIAGNOSTIC_STATUS_PROVIDER_ACTION_MAP.entrySet().removeIf(entry -> entry.getValue() == statusProvider);
    }
  }

  public static IDiagnostic[] getDiagnosticProviders() {
    synchronized (DIAGNOSTIC_STATUS_PROVIDERS) {
      IDiagnostic[] copy = DIAGNOSTIC_STATUS_PROVIDERS.toArray(new IDiagnostic[DIAGNOSTIC_STATUS_PROVIDERS.size()]);
      return copy;
    }
  }

  public static void addActionToDiagnosticStatusProvider(String action, IDiagnostic statusProvider) {
    synchronized (DIAGNOSTIC_STATUS_PROVIDER_ACTION_MAP) {
      DIAGNOSTIC_STATUS_PROVIDER_ACTION_MAP.put(action, statusProvider);
    }
  }

  public static IDiagnostic getDiagnosticProvider(String action) {
    synchronized (DIAGNOSTIC_STATUS_PROVIDER_ACTION_MAP) {
      return DIAGNOSTIC_STATUS_PROVIDER_ACTION_MAP.get(action);
    }
  }

  public static void addDiagnosticItemToList(List<List<String>> list, String attribute, String value, String status) {
    List<String> newItem = new ArrayList<>();
    newItem.add(attribute);
    newItem.add(value);
    newItem.add(status);
    list.add(newItem);
  }
}
