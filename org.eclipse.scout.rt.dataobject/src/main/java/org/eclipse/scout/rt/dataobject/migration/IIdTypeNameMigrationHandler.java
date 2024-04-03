/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.migration;

import java.util.Map;

import org.eclipse.scout.rt.platform.ApplicationScoped;

// TODO PBZ Add Javadoc, check naming, add example impl
@ApplicationScoped
public interface IIdTypeNameMigrationHandler {

  /**
   * Return map of id type name translations.
   * <p>
   * Example renames the id with type name "example.Lorem" to "example.Ipsum".
   *
   * <pre>
   * return Map.of("example.Lorem", "example.Ipsum");
   * </pre>
   */
  Map<String, String> getIdTypeNameTranslations();
}
