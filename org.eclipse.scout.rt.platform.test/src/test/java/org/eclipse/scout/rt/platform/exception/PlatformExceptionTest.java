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

import static org.eclipse.scout.rt.platform.util.CollectionUtility.arrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.Arrays;

import org.eclipse.scout.rt.platform.serialization.IObjectSerializer;
import org.eclipse.scout.rt.platform.serialization.SerializationUtility;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlatformExceptionTest {

  private static final Logger LOG = LoggerFactory.getLogger(PlatformExceptionTest.class);

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
    RuntimeException cause = new RuntimeException("expected JUnit test exception");

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
  }

  @Test
  public void testWithContextInfo() {
    PlatformException exception = new PlatformException("exception");
    exception
        .withContextInfo("key1", "value 1")
        .withContextInfo("key2", "value {}", 2)
        .withContextInfo("key3", "value 3");

    assertEquals(Arrays.asList("key1=value 1", "key2=value 2", "key3=value 3"), exception.getContextInfos());
  }

  @Test
  public void testDeserializeException() throws Exception {
    PlatformException exception = new PlatformException("exception").withContextInfo("name", "value");

    IObjectSerializer serializer = SerializationUtility.createObjectSerializer();
    PlatformException deserializedException = serializer.deserialize(serializer.serialize(exception), PlatformException.class);

    assertEquals("exception", deserializedException.getDisplayMessage());
    assertEquals(arrayList("name=value"), deserializedException.getContextInfos());

    deserializedException.withContextInfo("otherName", "otherValue");
    assertEquals(arrayList("name=value", "otherName=otherValue"), deserializedException.getContextInfos());
  }

  @Test
  @Ignore("This test is used only for verifying how exceptions are formatted using toString(), printStackTrace() and a logger instance.")
  public void testExceptionFormatting() {
    printException("NullPointer", new NullPointerException("npe message text"));

    // platform exception
    PlatformException platformException = new PlatformException("platform exception test");
    printException("simple PlatformException", platformException);

    // platform exception with context infos
    platformException
        .withContextInfo("a", "1234")
        .withContextInfo("replacement", "value-{}", "1234");
    printException("simple PlatformException with context infos", platformException);

    // processing exception
    ProcessingException processingException = new ProcessingException(new ProcessingStatus("pe message text", IStatus.OK));
    printException("simple ProcessingException", processingException);

    // processing exception with title
    ProcessingException wrappingProcessingException = new ProcessingException("wrapping Processing Exception message", processingException).withTitle("wrapping PE Title");
    printException("wrapped ProcessingException", wrappingProcessingException);

    // with context infos
    wrappingProcessingException
        .withContextInfo("a", "1234")
        .withContextInfo("b", "foo");
    processingException
        .withContextInfo("foo", "bar")
        .withContextInfo("status", "interrupted");
    printException("wrapped ProcessingException with status", wrappingProcessingException);
  }

  private void printException(String msg, Throwable t) {
    System.err.println("\n\n\n==============");
    System.err.println("formatting " + msg);
    System.err.println("==============");
    System.err.println("Sys.err.println(t)");
    System.err.println(t);
    System.err.println("-------");
    System.err.println("t.printStackTrace()");
    t.printStackTrace();
    System.err.println("-------");
    System.err.println("LOG.error(\"logger message\", t)");
    LOG.error("logger message", t);
  }
}
