/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.job;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.eclipse.scout.commons.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.context.ServerRunContext;
import org.junit.Test;

public class ServerJobInputValidatorTest {

  @Test
  public void test1() {
    new ServerJobInputValidator().validate(new JobInput().runContext(new ServerRunContext().session(null)));
    new ServerJobInputValidator().validate(new JobInput().runContext(new ServerRunContext().session(mock(IServerSession.class))));
    assertTrue(true);
  }

  @Test(expected = AssertionException.class)
  public void testNullServerRunContext() {
    new ServerJobInputValidator().validate(new JobInput());
  }

  @Test(expected = AssertionException.class)
  public void testWrongRunContext() {
    new ServerJobInputValidator().validate(new JobInput().runContext(new RunContext()));
  }
}
