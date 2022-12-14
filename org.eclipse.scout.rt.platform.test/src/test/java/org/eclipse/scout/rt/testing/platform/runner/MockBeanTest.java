/*
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.testing.platform.runner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.holders.Holder;
import org.eclipse.scout.rt.platform.holders.StringHolder;
import org.eclipse.scout.rt.testing.platform.mock.BeanMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class MockBeanTest {

  private static final Holder<ITestB> BEAN_B_HOLDER = new Holder<>(ITestB.class);

  private StringHolder m_beanAIdHolder = new StringHolder();
  private StringHolder m_beanBIdHolder = new StringHolder();

  @BeanMock
  private ITestA m_beanA;
  @BeanMock
  private ITestB m_beanB;

  @Before()
  public void before() {
    initMock(m_beanA, m_beanAIdHolder);
    initMock(m_beanB, m_beanBIdHolder);
  }

  protected void initMock(ITestBean bean, StringHolder idHolder) {
    when(bean.hello()).thenReturn("hello mock");
    doAnswer(i -> {
      idHolder.setValue(i.getArgument(0, String.class));
      return null;
    }).when(bean).setId(anyString());
    when(bean.getId()).thenAnswer(i -> idHolder.getValue());
  }

  @Test
  public void testInitializeBeanMock() {
    assertNotNull(m_beanA);
    assertNotNull(BEANS.opt(ITestA.class));

    assertNotNull(m_beanB);
    assertNotNull(BEANS.opt(ITestB.class));
  }

  @Test
  public void testOrdinaryBean() {
    ITestA a1 = BEANS.get(ITestA.class);
    ITestA a2 = BEANS.get(ITestA.class);

    // ITestA is not application-scoped. Hence, every BEANS.get returns a new Mockito mock instance
    assertNotSame(a1, a2);

    // and the "initial" instance (i.e. m_beanA) is different as well
    assertNotSame(m_beanA, a1);

    // because of m_beanA is not the same as a1, none of the recorded behavior is available on a1
    assertNull(a1.hello());
    assertNull(a1.getId());

    a1.setId("a");
    assertNull(a1.getId());
  }

  @Test
  public void testApplicationScopedBean() {
    ITestB b1 = BEANS.get(ITestB.class);
    ITestB b2 = BEANS.get(ITestB.class);

    // ITestB is application-scoped. Hence, every BEANS.get returns the very same Mockito mock instance
    assertSame(b1, b2);

    // and of course, the initial instance is the same as well
    assertSame(m_beanB, b1);

    // because of that, the recorded Mockito behavior is executed
    assertEquals("hello mock", b1.hello());

    assertNull(b1.getId());

    b1.setId("b1");
    assertEquals("b1", b1.getId());

    // id is "persistent" on the application-scoped bean
    assertEquals("b1", BEANS.get(ITestB.class).getId());

    // see testApplicationScopedBeanIsNotSharedBetweenTestMethods
    assertBeanIsNotSharedBetweenTests();
  }

  @Test
  public void testApplicationScopedBeanIsNotSharedBetweenTestMethods() {
    // @BeanMocks are not shared between different test methods.
    assertBeanIsNotSharedBetweenTests();
  }

  protected void assertBeanIsNotSharedBetweenTests() {
    ITestB current = BEANS.get(ITestB.class);
    assertNotNull(current);
    if (BEAN_B_HOLDER.getHolderType() == null) {
      BEAN_B_HOLDER.setValue(current);
    }
    else {
      assertNotSame(BEAN_B_HOLDER.getValue(), current);
    }
  }

  public static interface ITestBean {
    String hello();

    void setId(String id);

    String getId();
  }

  public static interface ITestA extends ITestBean {
  }

  @ApplicationScoped
  public static interface ITestB extends ITestBean {
  }
}
