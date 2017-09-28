/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.extension;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Collections;
import java.util.NoSuchElementException;

import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.shared.extension.fixture.ITestingExtension;
import org.eclipse.scout.rt.shared.extension.fixture.LocalTestingExtension;
import org.eclipse.scout.rt.shared.extension.fixture.OtherTestingExtension;
import org.eclipse.scout.rt.shared.extension.fixture.TestingExtensibleObject;
import org.eclipse.scout.rt.shared.extension.fixture.TestingExtension;
import org.eclipse.scout.rt.shared.extension.fixture.TestingExtensionChain;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 4.2
 */
public class ExtensionChainTest {

  private TestingExtensibleObject m_owner;
  private LocalTestingExtension m_localExtension;
  private TestingExtension m_extensionA;
  private TestingExtension m_extensionB;
  private OtherTestingExtension m_otherExtensionA;
  private OtherTestingExtension m_otherExtensionB;

  @Before
  public void before() {
    m_owner = new TestingExtensibleObject();
    m_localExtension = new LocalTestingExtension(m_owner);
    m_extensionA = new TestingExtension(m_owner);
    m_extensionB = new TestingExtension(m_owner);
    m_otherExtensionA = new OtherTestingExtension(m_owner);
    m_otherExtensionB = new OtherTestingExtension(m_owner);
  }

  @Test
  public void testEmptyExtensionChain() {
    TestingExtensionChain<TestingExtension> chain = new TestingExtensionChain<TestingExtension>(Collections.<TestingExtension> emptyList(), TestingExtension.class);

    assertState(chain, false, false);
    assertNextFails(chain);
    assertPreviousFails(chain);
  }

  @Test
  public void testExtensionChainOneExtension() {
    TestingExtensionChain<TestingExtension> chain = new TestingExtensionChain<TestingExtension>(Collections.singletonList(m_extensionA), TestingExtension.class);

    assertState(chain, false, true);
    assertPreviousFails(chain);
    assertNext(chain, m_extensionA);

    assertState(chain, true, false);
    assertNextFails(chain);
    assertPrevious(chain, m_extensionA);

    assertState(chain, false, true);
  }

  @Test
  public void testExtensionChainTwoExtensions() {
    TestingExtensionChain<TestingExtension> chain = new TestingExtensionChain<TestingExtension>(Arrays.asList(m_extensionA, m_extensionB), TestingExtension.class);

    assertState(chain, false, true);
    assertPreviousFails(chain);
    assertNext(chain, m_extensionA);

    assertState(chain, true, true);
    assertPrevious(chain, m_extensionA);
    assertNext(chain, m_extensionA);
    assertNext(chain, m_extensionB);

    assertState(chain, true, false);
    assertNextFails(chain);
    assertPrevious(chain, m_extensionB);

    assertState(chain, true, true);
    assertPrevious(chain, m_extensionA);
    assertNext(chain, m_extensionA);
    assertPrevious(chain, m_extensionA);

    assertState(chain, false, true);
  }

  @Test
  public void testExtensionChainWithOneOtherExtension() {
    TestingExtensionChain<TestingExtension> chain = new TestingExtensionChain<TestingExtension>(Collections.singletonList(m_otherExtensionA), TestingExtension.class);

    assertState(chain, false, false);
    assertNextFails(chain);
    assertPreviousFails(chain);
  }

  @Test
  public void testExtensionChainWithTwoOtherExtensions() {
    TestingExtensionChain<TestingExtension> chain = new TestingExtensionChain<TestingExtension>(Arrays.asList(m_otherExtensionA, m_otherExtensionB), TestingExtension.class);

    assertState(chain, false, false);
    assertNextFails(chain);
    assertPreviousFails(chain);
  }

  @Test
  public void testExtensionChainWithExtensionAndOtherExtension() {
    TestingExtensionChain<TestingExtension> chain = new TestingExtensionChain<TestingExtension>(Arrays.<IExtension<?>> asList(m_extensionA, m_otherExtensionB), TestingExtension.class);

    assertState(chain, false, true);
    assertPreviousFails(chain);
    assertNext(chain, m_extensionA);

    assertState(chain, true, false);
    assertNextFails(chain);
    assertPrevious(chain, m_extensionA);

    assertState(chain, false, true);
  }

  @Test
  public void testExtensionChainWithOtherExtensionAndExtension() {
    TestingExtensionChain<TestingExtension> chain = new TestingExtensionChain<TestingExtension>(Arrays.<IExtension<?>> asList(m_otherExtensionB, m_extensionA), TestingExtension.class);

    assertState(chain, false, true);
    assertPreviousFails(chain);
    assertNext(chain, m_extensionA);

    assertState(chain, true, false);
    assertNextFails(chain);
    assertPrevious(chain, m_extensionA);

    assertState(chain, false, true);
  }

  @Test
  public void testExtensionChainWithExtensionAAndExtensionBAndOtherExtensionst() {
    TestingExtensionChain<TestingExtension> chain;

    chain = new TestingExtensionChain<TestingExtension>(Arrays.<IExtension<?>> asList(m_extensionA, m_extensionB, m_otherExtensionA), TestingExtension.class);
    doTestExtesionAAndExtensionB(chain);

    chain = new TestingExtensionChain<TestingExtension>(Arrays.<IExtension<?>> asList(m_extensionA, m_otherExtensionA, m_extensionB), TestingExtension.class);
    doTestExtesionAAndExtensionB(chain);

    chain = new TestingExtensionChain<TestingExtension>(Arrays.<IExtension<?>> asList(m_otherExtensionA, m_extensionA, m_extensionB), TestingExtension.class);
    doTestExtesionAAndExtensionB(chain);

    chain = new TestingExtensionChain<TestingExtension>(Arrays.<IExtension<?>> asList(m_extensionA, m_extensionB, m_otherExtensionA, m_otherExtensionB), TestingExtension.class);
    doTestExtesionAAndExtensionB(chain);

    chain = new TestingExtensionChain<TestingExtension>(Arrays.<IExtension<?>> asList(m_extensionA, m_otherExtensionA, m_extensionB, m_otherExtensionB), TestingExtension.class);
    doTestExtesionAAndExtensionB(chain);

    chain = new TestingExtensionChain<TestingExtension>(Arrays.<IExtension<?>> asList(m_otherExtensionA, m_extensionA, m_extensionB, m_otherExtensionB), TestingExtension.class);
    doTestExtesionAAndExtensionB(chain);

    chain = new TestingExtensionChain<TestingExtension>(Arrays.<IExtension<?>> asList(m_otherExtensionA, m_extensionA, m_otherExtensionB, m_extensionB), TestingExtension.class);
    doTestExtesionAAndExtensionB(chain);

    chain = new TestingExtensionChain<TestingExtension>(Arrays.<IExtension<?>> asList(m_extensionA, m_otherExtensionA, m_otherExtensionB, m_extensionB), TestingExtension.class);
    doTestExtesionAAndExtensionB(chain);

    chain = new TestingExtensionChain<TestingExtension>(Arrays.<IExtension<?>> asList(m_otherExtensionA, m_otherExtensionB, m_extensionA, m_extensionB), TestingExtension.class);
    doTestExtesionAAndExtensionB(chain);
  }

  @Test
  public void testExtensionChainOperationWithLocalExtension() {
    TestingExtensionChain<TestingExtension> chain = new TestingExtensionChain<TestingExtension>(Collections.singletonList(m_localExtension), ITestingExtension.class);
    chain.execOperation();
  }

  @Test
  public void testExtensionChainOperationWithLocalAndAdditionalExtension() {
    TestingExtensionChain<TestingExtension> chain = new TestingExtensionChain<TestingExtension>(Arrays.<IExtension<?>> asList(m_localExtension, m_extensionA), ITestingExtension.class);
    chain.execOperation();
  }

  @Test
  public void testExtensionChainOperationWithLocalExtensionThrowingException() {
    ITestingExtension extension = mock(ITestingExtension.class);
    doThrow(new ProcessingException()).when(extension).execOperation((TestingExtensionChain<?>) any(TestingExtensionChain.class));
    TestingExtensionChain<TestingExtension> chain = new TestingExtensionChain<TestingExtension>(Collections.singletonList(extension), ITestingExtension.class);
    try {
      chain.execOperation();
      fail("expecting exception");
    }
    catch (ProcessingException e) {
      // expected
    }
  }

  @Test
  public void testExtensionChainOperationWithTwoExtensionsThrowingException() {
    ITestingExtension extension = mock(ITestingExtension.class);
    doThrow(new ProcessingException()).when(extension).execOperation((TestingExtensionChain<?>) any(TestingExtensionChain.class));
    TestingExtensionChain<TestingExtension> chain = new TestingExtensionChain<TestingExtension>(Arrays.<IExtension<?>> asList(m_extensionA, extension), ITestingExtension.class);
    try {
      chain.execOperation();
      fail("expecting exception");
    }
    catch (ProcessingException e) {
      // expected
    }
  }

  protected void doTestExtesionAAndExtensionB(TestingExtensionChain<TestingExtension> chain) {
    assertState(chain, false, true);
    assertPreviousFails(chain);
    assertNext(chain, m_extensionA);

    assertState(chain, true, true);
    assertPrevious(chain, m_extensionA);
    assertNext(chain, m_extensionA);
    assertNext(chain, m_extensionB);

    assertState(chain, true, false);
    assertNextFails(chain);
    assertPrevious(chain, m_extensionB);

    assertState(chain, true, true);
    assertNext(chain, m_extensionB);
    assertPrevious(chain, m_extensionB);
    assertPrevious(chain, m_extensionA);

    assertState(chain, false, true);
  }

  public static void assertState(TestingExtensionChain<TestingExtension> chain, boolean expectedHasPrevious, boolean expectedHasNext) {
    assertEquals(expectedHasNext, chain.hasNext());
    assertEquals(expectedHasPrevious, chain.hasPrevious());
  }

  public static void assertNext(TestingExtensionChain<TestingExtension> chain, Object expectedNextElement) {
    assertSame(expectedNextElement, chain.next());
  }

  public static void assertPrevious(TestingExtensionChain<TestingExtension> chain, Object expectedPreviousElement) {
    assertSame(expectedPreviousElement, chain.previous());
  }

  public static void assertNextFails(TestingExtensionChain<TestingExtension> chain) {
    try {
      chain.next();
      fail("next() is not possible on an empty chain");
    }
    catch (NoSuchElementException e) {
      // ok
    }
  }

  public static void assertPreviousFails(TestingExtensionChain<TestingExtension> chain) {
    try {
      chain.previous();
      fail("previous() is not possible on an empty chain");
    }
    catch (NoSuchElementException e) {
      // ok
    }
  }
}
