/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.eclipse.scout.rt.platform.exception.BeanCreationException;
import org.eclipse.scout.rt.platform.internal.fixture.EmptyCtorBean;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.junit.Test;

/**
 * Tests for {@link BeanInstanceUtil}
 */
public class BeanInstanceUtilTest {

  private static final String METHOD_NAME_PUBLIC_POST_CONSTRUCT = "publicPostConstruct";
  private static final String METHOD_NAME_PROTECTED_POST_CONSTRUCT = "protectedPostConstruct";
  private static final String METHOD_NAME_PACKAGE_PRIVATE_POST_CONSTRUCT = "packagePrivatePostConstruct";
  private static final String METHOD_NAME_PRIVATE_POST_CONSTRUCT = "privatePostConstruct";

  private static final String METHOD_NAME_PUBLIC = "public";
  private static final String METHOD_NAME_PROTECTED = "protected";
  private static final String METHOD_NAME_PACKAGE_PRIVATE = "packagePrivate";
  private static final String METHOD_NAME_PRIVATE = "private";

  @Test
  public void testCreateAndInitializeBean() {
    EmptyCtorBean o = BeanInstanceUtil.createAndInitializeBean(EmptyCtorBean.class);
    assertNotNull(o);
  }

  @Test(expected = BeanCreationException.class)
  public void testCreateAndInitializeBeanConstructorThrowingRuntimeException() {
    BeanInstanceUtil.createAndInitializeBean(BeanConstructorThrowingRuntimeException.class);
  }

  @Test(expected = BeanCreationException.class)
  public void testCreateAndInitializeBeanConstructorThrowingException() {
    BeanInstanceUtil.createAndInitializeBean(BeanConstructorThrowingException.class);
  }

  @Test(expected = BeanCreationException.class)
  public void testCreateAndInitializeBeanWithoutDefaultConstructor() {
    BeanInstanceUtil.createAndInitializeBean(BeanWithoutDefaultConstructor.class);
  }

  @Test(expected = BeanCreationException.class)
  public void testCreateAndInitializeBeanPostConstructThrowingRuntimeException() {
    BeanInstanceUtil.createAndInitializeBean(PostConstructThrowingRuntimeException.class);
  }

  @Test(expected = BeanCreationException.class)
  public void testCreateAndInitializeBeanPostConstructThrowingException() {
    BeanInstanceUtil.createAndInitializeBean(PostConstructThrowingException.class);
  }

  @Test
  public void testCollectPostConstructMethodsBase() throws Exception {
    Collection<Method> postConstructMethods = BeanInstanceUtil.collectPostConstructMethods(PostConstructBase.class);
    assertEquals(4, postConstructMethods.size());

    Set<Method> expectedMethods = new HashSet<Method>();
    expectedMethods.add(PostConstructBase.class.getDeclaredMethod(METHOD_NAME_PUBLIC_POST_CONSTRUCT));
    expectedMethods.add(PostConstructBase.class.getDeclaredMethod(METHOD_NAME_PROTECTED_POST_CONSTRUCT));
    expectedMethods.add(PostConstructBase.class.getDeclaredMethod(METHOD_NAME_PACKAGE_PRIVATE_POST_CONSTRUCT));
    expectedMethods.add(PostConstructBase.class.getDeclaredMethod(METHOD_NAME_PRIVATE_POST_CONSTRUCT));
    assertEquals(expectedMethods, new HashSet<Method>(postConstructMethods));
  }

  @Test
  public void testCollectPostConstructMethodsEx1() throws Exception {
    Collection<Method> postConstructMethods = BeanInstanceUtil.collectPostConstructMethods(PostConstructEx1.class);
    assertEquals(5, postConstructMethods.size());

    Set<Method> expectedMethods = new HashSet<Method>();
    expectedMethods.add(PostConstructBase.class.getDeclaredMethod(METHOD_NAME_PRIVATE_POST_CONSTRUCT));
    expectedMethods.add(PostConstructEx1.class.getDeclaredMethod(METHOD_NAME_PUBLIC_POST_CONSTRUCT));
    expectedMethods.add(PostConstructEx1.class.getDeclaredMethod(METHOD_NAME_PROTECTED_POST_CONSTRUCT));
    expectedMethods.add(PostConstructEx1.class.getDeclaredMethod(METHOD_NAME_PACKAGE_PRIVATE_POST_CONSTRUCT));
    expectedMethods.add(PostConstructEx1.class.getDeclaredMethod(METHOD_NAME_PRIVATE_POST_CONSTRUCT));
    assertEquals(expectedMethods, new HashSet<Method>(postConstructMethods));
  }

  @Test
  public void testCollectPostConstructMethodsEx2() throws Exception {
    Collection<Method> postConstructMethods = BeanInstanceUtil.collectPostConstructMethods(PostConstructEx2.class);
    assertEquals(5, postConstructMethods.size());

    Set<Method> expectedMethods = new HashSet<Method>();
    expectedMethods.add(PostConstructBase.class.getDeclaredMethod(METHOD_NAME_PUBLIC_POST_CONSTRUCT));
    expectedMethods.add(PostConstructBase.class.getDeclaredMethod(METHOD_NAME_PROTECTED_POST_CONSTRUCT));
    expectedMethods.add(PostConstructBase.class.getDeclaredMethod(METHOD_NAME_PACKAGE_PRIVATE_POST_CONSTRUCT));
    expectedMethods.add(PostConstructBase.class.getDeclaredMethod(METHOD_NAME_PRIVATE_POST_CONSTRUCT));
    expectedMethods.add(PostConstructEx2.class.getDeclaredMethod(METHOD_NAME_PRIVATE_POST_CONSTRUCT));
    assertEquals(expectedMethods, new HashSet<Method>(postConstructMethods));
  }

  @Test(expected = BeanCreationException.class)
  public void testCollectPostConstructMethodsWithParameters() throws Exception {
    BeanInstanceUtil.collectPostConstructMethods(PostConstructWithParameters.class);
  }

  @Test
  public void testInitializeInstanceBase() throws Exception {
    PostConstructBase base = new PostConstructBase();
    BeanInstanceUtil.initializeBeanInstance(base);

    List<String> expectedMethodInvocations = CollectionUtility.arrayList(
        formatLogEntry(PostConstructBase.class, METHOD_NAME_PUBLIC_POST_CONSTRUCT),
        formatLogEntry(PostConstructBase.class, METHOD_NAME_PROTECTED_POST_CONSTRUCT),
        formatLogEntry(PostConstructBase.class, METHOD_NAME_PACKAGE_PRIVATE_POST_CONSTRUCT),
        formatLogEntry(PostConstructBase.class, METHOD_NAME_PRIVATE_POST_CONSTRUCT));
    Collections.sort(expectedMethodInvocations);

    assertEquals(expectedMethodInvocations, base.getMethodInvocationLog());
  }

  @Test
  public void testInitializeInstanceBaseEx1() throws Exception {
    PostConstructEx1 baseEx1 = new PostConstructEx1();
    BeanInstanceUtil.initializeBeanInstance(baseEx1);

    List<String> expectedMethodInvocations = CollectionUtility.arrayList(
        formatLogEntry(PostConstructBase.class, METHOD_NAME_PRIVATE_POST_CONSTRUCT),
        formatLogEntry(PostConstructEx1.class, METHOD_NAME_PUBLIC_POST_CONSTRUCT),
        formatLogEntry(PostConstructEx1.class, METHOD_NAME_PROTECTED_POST_CONSTRUCT),
        formatLogEntry(PostConstructEx1.class, METHOD_NAME_PACKAGE_PRIVATE_POST_CONSTRUCT),
        formatLogEntry(PostConstructEx1.class, METHOD_NAME_PRIVATE_POST_CONSTRUCT));
    Collections.sort(expectedMethodInvocations);

    assertEquals(expectedMethodInvocations, baseEx1.getMethodInvocationLog());
  }

  @Test
  public void testInitializeInstanceBaseEx2() throws Exception {
    PostConstructEx2 baseEx2 = new PostConstructEx2();
    BeanInstanceUtil.initializeBeanInstance(baseEx2);

    List<String> expectedMethodInvocations = CollectionUtility.arrayList(
        formatLogEntry(PostConstructBase.class, METHOD_NAME_PRIVATE_POST_CONSTRUCT),
        formatLogEntry(PostConstructEx2.class, METHOD_NAME_PUBLIC_POST_CONSTRUCT),
        formatLogEntry(PostConstructEx2.class, METHOD_NAME_PROTECTED_POST_CONSTRUCT),
        formatLogEntry(PostConstructEx2.class, METHOD_NAME_PACKAGE_PRIVATE_POST_CONSTRUCT),
        formatLogEntry(PostConstructEx2.class, METHOD_NAME_PRIVATE_POST_CONSTRUCT));
    Collections.sort(expectedMethodInvocations);

    assertEquals(expectedMethodInvocations, baseEx2.getMethodInvocationLog());
  }

  @Test(expected = BeanCreationException.class)
  public void testInitializeInstanceThrowingRuntimeException() throws Exception {
    BeanInstanceUtil.initializeBeanInstance(new PostConstructThrowingRuntimeException());
  }

  @Test(expected = BeanCreationException.class)
  public void testInitializeInstanceThrowingException() throws Exception {
    BeanInstanceUtil.initializeBeanInstance(new PostConstructThrowingException());
  }

  public static String formatLogEntry(Class c, String methodName) {
    return String.format("%s.%s", c.getSimpleName(), methodName);
  }

  @SuppressWarnings("unused")
  private static class PostConstructBase {

    List<String> m_invokedMethods = new ArrayList<>();

    public List<String> getMethodInvocationLog() {
      Collections.sort(m_invokedMethods);
      return m_invokedMethods;
    }

    protected void logMethodInvocation(Class c, String methodName) {
      m_invokedMethods.add(formatLogEntry(c, methodName));
    }

    @PostConstruct
    public void publicPostConstruct() {
      logMethodInvocation(PostConstructBase.class, METHOD_NAME_PUBLIC_POST_CONSTRUCT);
    }

    @PostConstruct
    protected void protectedPostConstruct() {
      logMethodInvocation(PostConstructBase.class, METHOD_NAME_PROTECTED_POST_CONSTRUCT);
    }

    @PostConstruct
    void packagePrivatePostConstruct() {
      logMethodInvocation(PostConstructBase.class, METHOD_NAME_PACKAGE_PRIVATE_POST_CONSTRUCT);
    }

    @PostConstruct
    private void privatePostConstruct() {
      logMethodInvocation(PostConstructBase.class, METHOD_NAME_PRIVATE_POST_CONSTRUCT);
    }

    public void publicMethod() {
      logMethodInvocation(PostConstructBase.class, METHOD_NAME_PUBLIC);
    }

    protected void protectedMethod() {
      logMethodInvocation(PostConstructBase.class, METHOD_NAME_PROTECTED);
    }

    void packagePrivateMethod() {
      logMethodInvocation(PostConstructBase.class, METHOD_NAME_PACKAGE_PRIVATE);
    }

    private void privateMethod() {
      logMethodInvocation(PostConstructBase.class, METHOD_NAME_PRIVATE);
    }
  }

  private static class PostConstructEx1 extends PostConstructBase {

    @Override
    @PostConstruct
    public void publicPostConstruct() {
      logMethodInvocation(PostConstructEx1.class, METHOD_NAME_PUBLIC_POST_CONSTRUCT);
    }

    @Override
    @PostConstruct
    protected void protectedPostConstruct() {
      logMethodInvocation(PostConstructEx1.class, METHOD_NAME_PROTECTED_POST_CONSTRUCT);
    }

    @Override
    @PostConstruct
    void packagePrivatePostConstruct() {
      logMethodInvocation(PostConstructEx1.class, METHOD_NAME_PACKAGE_PRIVATE_POST_CONSTRUCT);
    }

    @PostConstruct
    private void privatePostConstruct() {
      logMethodInvocation(PostConstructEx1.class, METHOD_NAME_PRIVATE_POST_CONSTRUCT);
    }

    @Override
    public void publicMethod() {
      logMethodInvocation(PostConstructEx1.class, METHOD_NAME_PUBLIC);
    }

    @Override
    protected void protectedMethod() {
      logMethodInvocation(PostConstructEx1.class, METHOD_NAME_PROTECTED);
    }

    @Override
    void packagePrivateMethod() {
      logMethodInvocation(PostConstructEx1.class, METHOD_NAME_PACKAGE_PRIVATE);
    }

    @SuppressWarnings("unused")
    private void privateMethod() {
      logMethodInvocation(PostConstructEx1.class, METHOD_NAME_PRIVATE);
    }
  }

  private static class PostConstructEx2 extends PostConstructBase {

    @Override
    public void publicPostConstruct() {
      logMethodInvocation(PostConstructEx2.class, METHOD_NAME_PUBLIC_POST_CONSTRUCT);
    }

    @Override
    protected void protectedPostConstruct() {
      logMethodInvocation(PostConstructEx2.class, METHOD_NAME_PROTECTED_POST_CONSTRUCT);
    }

    @Override
    void packagePrivatePostConstruct() {
      logMethodInvocation(PostConstructEx2.class, METHOD_NAME_PACKAGE_PRIVATE_POST_CONSTRUCT);
    }

    @PostConstruct
    private void privatePostConstruct() {
      logMethodInvocation(PostConstructEx2.class, METHOD_NAME_PRIVATE_POST_CONSTRUCT);
    }

    @Override
    public void publicMethod() {
      logMethodInvocation(PostConstructEx2.class, METHOD_NAME_PUBLIC);
    }

    @Override
    protected void protectedMethod() {
      logMethodInvocation(PostConstructEx2.class, METHOD_NAME_PROTECTED);
    }

    @Override
    void packagePrivateMethod() {
      logMethodInvocation(PostConstructEx2.class, METHOD_NAME_PACKAGE_PRIVATE);
    }

    @SuppressWarnings("unused")
    private void privateMethod() {
      logMethodInvocation(PostConstructEx2.class, METHOD_NAME_PRIVATE);
    }
  }

  private static class PostConstructWithParameters {
    @PostConstruct
    public void badMethod(String s) {
    }
  }

  private static class PostConstructThrowingRuntimeException {
    @PostConstruct
    public void postConstruct() {
      throw new RuntimeException("exception by design");
    }
  }

  private static class PostConstructThrowingException {
    @PostConstruct
    public void postConstruct() throws Exception {
      throw new Exception("exception by design");
    }
  }

  private static class BeanConstructorThrowingRuntimeException {
    @SuppressWarnings("unused")
    public BeanConstructorThrowingRuntimeException() {
      throw new RuntimeException("exception by design");
    }
  }

  private static class BeanConstructorThrowingException {
    @SuppressWarnings("unused")
    public BeanConstructorThrowingException() throws Exception {
      throw new Exception("exception by design");
    }
  }

  private static class BeanWithoutDefaultConstructor {
    @SuppressWarnings("unused")
    public BeanWithoutDefaultConstructor(String s) {
    }
  }
}
