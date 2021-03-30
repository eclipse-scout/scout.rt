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
package org.eclipse.scout.rt.dataobject.migration.fixture.house;

import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.scout.rt.dataobject.migration.IDoStructureMigrationGlobalContextData;

// not a Bean, manually created
public class ZipCodeFixtureGlobalContextData implements IDoStructureMigrationGlobalContextData {

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
