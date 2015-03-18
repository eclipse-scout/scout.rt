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
package org.eclipse.scout.rt.platform.job;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.concurrent.Callable;

import org.eclipse.scout.commons.Assertions.AssertionException;
import org.eclipse.scout.commons.ICallable;
import org.eclipse.scout.commons.IExecutable;
import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.rt.platform.job.internal.Executables;
import org.junit.Test;

public class ExecutablesTest {

  @Test
  public void testCallableFromIRunnable1() throws Exception {
    IRunnable origin = mock(IRunnable.class);

    Callable<Void> callable = Executables.callable(origin);
    assertNull(callable.call());

    verify(origin, times(1)).run();
    verifyNoMoreInteractions(origin);
  }

  @Test(expected = RuntimeException.class)
  public void testCallableFromIRunnable2() throws Exception {
    IRunnable origin = mock(IRunnable.class);
    doThrow(new RuntimeException()).when(origin).run();

    Callable<Void> callable = Executables.callable(origin);
    callable.call();
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testCallableFromICallable1() throws Exception {
    ICallable<String> origin = mock(ICallable.class);

    Callable<String> callable = Executables.callable(origin);
    assertSame(origin, callable);
  }

  @Test(expected = AssertionException.class)
  @SuppressWarnings("unchecked")
  public void testCallableFromIExecutable() throws Exception {
    Callable<String> callable = Executables.callable(mock(IExecutable.class));
    callable.call();
  }
}
