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
package org.eclipse.scout.commons.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.Callable;

import org.eclipse.scout.commons.Assertions.AssertionException;
import org.eclipse.scout.commons.job.internal.Executables;
import org.eclipse.scout.commons.job.internal.Executables.CallableWithJobInput;
import org.eclipse.scout.commons.job.internal.Executables.RunnableWithJobInput;
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

  @Test
  @SuppressWarnings("unchecked")
  public void testCallableWithJobInput1() throws Exception {
    IJobInput input = mock(IJobInput.class);
    Callable<String> origin = mock(Callable.class);
    when(origin.call()).thenReturn("ABC");

    CallableWithJobInput<String> callable = Executables.callableWithJobInput(origin, input);
    assertEquals("ABC", callable.call());

    verify(origin, times(1)).call();
    verifyNoMoreInteractions(origin);
    assertSame(input, callable.getInput());
  }

  @Test(expected = RuntimeException.class)
  @SuppressWarnings("unchecked")
  public void testCallableWithJobInput2() throws Exception {
    IJobInput input = mock(IJobInput.class);
    Callable<String> origin = mock(Callable.class);
    when(origin.call()).thenThrow(new RuntimeException());

    CallableWithJobInput<String> callable = Executables.callableWithJobInput(origin, input);
    callable.call();
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testRunnableWithJobInput1() throws Exception {
    IJobInput input = mock(IJobInput.class);
    Callable<Void> origin = mock(Callable.class);

    RunnableWithJobInput runnable = Executables.runnableWithJobInput(origin, input);
    runnable.run();

    verify(origin, times(1)).call();
    verifyNoMoreInteractions(origin);
    assertSame(input, runnable.getInput());
  }

  @Test()
  @SuppressWarnings("unchecked")
  public void testRunnableWithJobInput2() throws Exception {
    IJobInput input = mock(IJobInput.class);
    Callable<Void> origin = mock(Callable.class);
    when(origin.call()).thenThrow(new RuntimeException());

    RunnableWithJobInput runnable = Executables.runnableWithJobInput(origin, input);
    runnable.run();
    verify(origin, times(1)).call();
    verifyNoMoreInteractions(origin);
    assertSame(input, runnable.getInput());
  }
}
