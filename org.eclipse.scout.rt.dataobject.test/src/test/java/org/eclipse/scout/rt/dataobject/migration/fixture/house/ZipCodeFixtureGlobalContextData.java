/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.migration.fixture.house;

import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.scout.rt.dataobject.migration.IDataObjectMigrationGlobalContextData;

// not a Bean, manually created
public class ZipCodeFixtureGlobalContextData implements IDataObjectMigrationGlobalContextData {

  private final ConcurrentHashMap<String, String> m_zipCodeToCityMap = new ConcurrentHashMap<>();

  public ZipCodeFixtureGlobalContextData() {
    m_zipCodeToCityMap.put("20001", "Washington");
    m_zipCodeToCityMap.put("37201", "Nashville");
    m_zipCodeToCityMap.put("02101", "Boston");
  }

  public String getCity(String zipCode) {
    return m_zipCodeToCityMap.get(zipCode);
  }
}
