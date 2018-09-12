/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
    ErrorDo error = BEANS.get(ErrorResponseBuilder.class).withCode(42).withMessage("message").withTitle("title").withStatus(10).buildError();

    assertEquals("message", error.getMessage());
    assertEquals("title", error.getTitle());
    assertEquals("42", error.getCode());
    assertEquals(Integer.valueOf(10), error.getStatus());
    assertEquals(CorrelationId.CURRENT.get(), error.getCorrelationId());
  }
}
