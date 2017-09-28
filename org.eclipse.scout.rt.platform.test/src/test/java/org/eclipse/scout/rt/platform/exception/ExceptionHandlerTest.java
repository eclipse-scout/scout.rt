/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.exception;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

public class ExceptionHandlerTest {

  @Test
  public void testRootCause() {
    assertNull(ExceptionHandler.getRootCause(null));

    Exception e = new Exception("expected JUnit test exception");
    assertSame(e, ExceptionHandler.getRootCause(e));
    assertSame(e, ExceptionHandler.getRootCause(new Exception("expected JUnit test exception", e)));
    assertSame(e, ExceptionHandler.getRootCause(new Throwable(new Exception("expected JUnit test exception", e))));
  }
}
