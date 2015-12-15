package org.eclipse.scout.rt.platform.exception;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

public class ExceptionHandlerTest {

  @Test
  public void testRootCause() {
    assertNull(ExceptionHandler.getRootCause(null));

    Exception e = new Exception();
    assertSame(e, ExceptionHandler.getRootCause(e));
    assertSame(e, ExceptionHandler.getRootCause(new Exception(e)));
    assertSame(e, ExceptionHandler.getRootCause(new Throwable(new Exception(e))));
  }
}
