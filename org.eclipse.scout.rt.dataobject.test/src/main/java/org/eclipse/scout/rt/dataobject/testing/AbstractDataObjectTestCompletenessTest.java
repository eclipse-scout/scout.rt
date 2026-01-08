/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.testing;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import org.eclipse.scout.rt.platform.BEANS;
import org.junit.Test;

public abstract class AbstractDataObjectTestCompletenessTest {

  @Test
  public void testDataObjectSignatureTestCompleteness() throws IOException {
    DataObjectSignatureTestSupport support = createSignatureTestSupport();
    getPathExclusions().forEach(support::addPathExclusion);
    support.doTest();
    support.failOnError();
  }

  @Test
  public void testDoStructureMigrationHandlerCompletenessTestCompleteness() throws IOException {
    DataObjectMigrationHandlerCompletenessTestSupport support = createMigrationHandlerCompletenessTestSupport();
    getPathExclusions().forEach(support::addPathExclusion);
    support.doTest();
    support.failOnError();
  }

  @Test
  public void testIdStructureTestCompleteness() throws IOException {
    IdStructureTestSupport support = createIdStructureTestSupport();
    getPathExclusions().forEach(support::addPathExclusion);
    support.doTest();
    support.failOnError();
  }

  protected DataObjectSignatureTestSupport createSignatureTestSupport() {
    return BEANS.get(DataObjectSignatureTestSupport.class);
  }

  protected DataObjectMigrationHandlerCompletenessTestSupport createMigrationHandlerCompletenessTestSupport() {
    return BEANS.get(DataObjectMigrationHandlerCompletenessTestSupport.class);
  }

  protected IdStructureTestSupport createIdStructureTestSupport() {
    return BEANS.get(IdStructureTestSupport.class);
  }

  protected List<Path> getPathExclusions() {
    return Collections.emptyList();
  }
}
