/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.resource;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.testing.platform.util.AbstractCompletenessTestSupport;
import org.junit.Test;

/**
 * Completeness test which scans all folders of this project (string matching), see {@link AbstractCompletenessTestSupport#AbstractCompletenessTestSupport()} for details.
 */
public abstract class AbstractApiExposedAnnotationTestCompletenessTest {

  @Test
  public void testApiExposedAnnotationTestCompleteness() throws IOException {
    ApiExposedAnnotationTestSupport support = createApiExposedAnnotationTestSupport();
    getPathExclusions().forEach(support::addPathExclusion);
    support.doTest();
    support.failOnError();
  }

  protected ApiExposedAnnotationTestSupport createApiExposedAnnotationTestSupport() {
    return BEANS.get(ApiExposedAnnotationTestSupport.class);
  }

  protected List<Path> getPathExclusions() {
    return Collections.emptyList();
  }
}
