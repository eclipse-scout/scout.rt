/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.testing.signature;

import java.nio.file.Path;
import java.util.List;

import org.eclipse.scout.rt.dataobject.testing.AbstractDataObjectTestCompletenessTest;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

public class ScoutDataObjectTestCompletenessTest extends AbstractDataObjectTestCompletenessTest {

  @Override
  protected List<Path> getPathExclusions() {
    return CollectionUtility.arrayList(
        Path.of("eclipse-scout-chart"),
        Path.of("eclipse-scout-cli"),
        Path.of("eclipse-scout-core"),
        Path.of("eclipse-scout-migrate"),
        Path.of("eclipse-scout-releng"),
        Path.of("eclipse-scout-tsconfig"),
        Path.of("eslint-config"),
        Path.of("karma-jasmine-scout"),
        Path.of("scout-hellojs-app"),
        Path.of("scout-helloworld-app"),
        Path.of("scout-jaxws-module"));
  }
}
