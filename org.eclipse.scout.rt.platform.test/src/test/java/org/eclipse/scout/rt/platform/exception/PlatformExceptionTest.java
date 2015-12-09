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
package org.eclipse.scout.rt.platform.exception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.Arrays;

import org.junit.Test;

public class PlatformExceptionTest {

  @Test
  public void testMessageWithArgs() {
    PlatformException e = new PlatformException("hello");
    assertEquals("hello", e.getMessage());
    assertNull(e.getCause());

    e = new PlatformException("hello {}", "world");
    assertEquals("hello world", e.getMessage());
    assertNull(e.getCause());

    e = new PlatformException("hello {}", "world1", "world2");
    assertEquals("hello world1", e.getMessage());
    assertNull(e.getCause());

    e = new PlatformException("hello {} {}", "world1", "world2");
    assertEquals("hello world1 world2", e.getMessage());
    assertNull(e.getCause());

    e = new PlatformException("hello {{}}", "world");
    assertEquals("hello {world}", e.getMessage());
    assertNull(e.getCause());

    e = new PlatformException("{hello {}}", "world");
    assertEquals("{hello world}", e.getMessage());
    assertNull(e.getCause());

    e = new PlatformException("hello {} {}", "world");
    assertEquals("hello world {}", e.getMessage());
    assertNull(e.getCause());
  }

  @Test
  public void testWithArgsAndException() {
    RuntimeException cause = new RuntimeException();

    PlatformException e = new PlatformException("hello", cause);
    assertEquals("hello", e.getMessage());
    assertSame(cause, e.getCause());

    e = new PlatformException("hello {}", "world", cause);
    assertEquals("hello world", e.getMessage());
    assertSame(cause, e.getCause());

    e = new PlatformException("hello {}", "world1", "world2", cause);
    assertEquals("hello world1", e.getMessage());
    assertSame(cause, e.getCause());

    e = new PlatformException("hello {} {}", "world1", "world2", cause);
    assertEquals("hello world1 world2", e.getMessage());
    assertSame(cause, e.getCause());

    e = new PlatformException("hello {{}}", "world", cause);
    assertEquals("hello {world}", e.getMessage());
    assertSame(cause, e.getCause());

    e = new PlatformException("{hello {}}", "world", cause);
    assertEquals("{hello world}", e.getMessage());
    assertSame(cause, e.getCause());

    e = new PlatformException("hello {} {}", "world", cause);
    assertEquals("hello world " + cause.toString(), e.getMessage());
    assertNull(e.getCause());
  }

  @Test
  public void testWithContextInfo() {
    PlatformException exception = new PlatformException("exception");
    exception
        .withContextInfo("key1", "value 1")
        .withContextInfo("key2", "value {}", 2)
        .withContextInfo("key3", "value 3");

    assertEquals(Arrays.asList("key3=value 3", "key2=value 2", "key1=value 1"), exception.getContextInfos());
  }
}
