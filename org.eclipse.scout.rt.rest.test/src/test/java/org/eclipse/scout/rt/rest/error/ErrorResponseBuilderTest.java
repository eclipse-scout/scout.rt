/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.error;

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.CorrelationId;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class ErrorResponseBuilderTest {

  @Test
  public void testBuild() {
    ErrorDo error = BEANS.get(ErrorResponseBuilder.class).withErrorCode(42).withMessage("message").withTitle("title").withHttpStatus(10).buildError();

    assertEquals("message", error.getMessage());
    assertEquals("title", error.getTitle());
    assertEquals("42", error.getErrorCode());
    assertEquals(Integer.valueOf(10), error.getHttpStatus());
    assertEquals(CorrelationId.CURRENT.get(), error.getCorrelationId());
  }
}
