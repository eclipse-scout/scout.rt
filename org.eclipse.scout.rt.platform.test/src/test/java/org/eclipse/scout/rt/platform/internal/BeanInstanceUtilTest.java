/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.internal;

import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.annotation.PostConstruct;

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
    EmptyCtorBean o = BeanInstanceUtil.createBean(EmptyCtorBean.class);
    assertNotNull(o);
  }

  @Test(expected = BeanCreationException.class)
  public void testCreateAndInitializeBeanConstructorThrowingRuntimeException() {
    BeanInstanceUtil.createBean(BeanConstructorThrowingRuntimeException.class);
  }

  @Test(expected = BeanCreationException.class)
  public void testCreateAndInitializeBeanConstructorThrowingException() {
    BeanInstanceUtil.createBean(BeanConstructorThrowingException.class);
  }

  @Test(expected = BeanCreationException.class)
  public void testCreateAndInitializeBeanWithoutDefaultConstructor() {
    BeanInstanceUtil.createBean(BeanWithoutDefaultConstructor.class);
  }

  @Test(expected = BeanCreationException.class)
  public void testCreateAndInitializeBeanPostConstructThrowingRuntimeException() {
    PostConstructThrowingRuntimeException beanInstance = BeanInstanceUtil.createBean(PostConstructThrowingRuntimeException.class);
    BeanInstanceUtil.initializeBeanInstance(beanInstance);
  }

  @Test(expected = BeanCreationException.class)
  public void testCreateAndInitializeBeanPostConstructThrowingException() {
    PostConstructThrowingException beanInstance = BeanInstanceUtil.createBean(PostConstructThrowingException.class);
    BeanInstanceUtil.initializeBeanInstance(beanInstance);
  }

  @Test
  public void testCollectPostConstructMethodsBase() throws Exception {
    Collection<Method> postConstructMethods = BeanInstanceUtil.collectPostConstructMethods(PostConstructBase.class);
    assertEquals(4, postConstructMethods.size());

    Set<Method> expectedMethods = new HashSet<>();
    expectedMethods.add(PostConstructBase.class.getDeclaredMethod(METHOD_NAME_PUBLIC_POST_CONSTRUCT));
    expectedMethods.add(PostConstructBase.class.getDeclaredMethod(METHOD_NAME_PROTECTED_POST_CONSTRUCT));
    expectedMethods.add(PostConstructBase.class.getDeclaredMethod(METHOD_NAME_PACKAGE_PRIVATE_POST_CONSTRUCT));
    expectedMethods.add(PostConstructBase.class.getDeclaredMethod(METHOD_NAME_PRIVATE_POST_CONSTRUCT));
    assertEquals(expectedMethods, new HashSet<>(postConstructMethods));
  }

  @Test
  public void testCollectPostConstructMethodsEx1() throws Exception {
    Collection<Method> postConstructMethods = BeanInstanceUtil.collectPostConstructMethods(PostConstructEx1.class);
    assertEquals(5, postConstructMethods.size());

    Set<Method> expectedMethods = new HashSet<>();
    expectedMethods.add(PostConstructBase.class.getDeclaredMethod(METHOD_NAME_PRIVATE_POST_CONSTRUCT));
    expectedMethods.add(PostConstructEx1.class.getDeclaredMethod(METHOD_NAME_PUBLIC_POST_CONSTRUCT));
    expectedMethods.add(PostConstructEx1.class.getDeclaredMethod(METHOD_NAME_PROTECTED_POST_CONSTRUCT));
    expectedMethods.add(PostConstructEx1.class.getDeclaredMethod(METHOD_NAME_PACKAGE_PRIVATE_POST_CONSTRUCT));
    expectedMethods.add(PostConstructEx1.class.getDeclaredMethod(METHOD_NAME_PRIVATE_POST_CONSTRUCT));
    assertEquals(expectedMethods, new HashSet<>(postConstructMethods));
  }

  @Test
  public void testCollectPostConstructMethodsEx2() throws Exception {
    Collection<Method> postConstructMethods = BeanInstanceUtil.collectPostConstructMethods(PostConstructEx2.class);
    assertEquals(5, postConstructMethods.size());

    Set<Method> expectedMethods = new HashSet<>();
    expectedMethods.add(PostConstructBase.class.getDeclaredMethod(METHOD_NAME_PUBLIC_POST_CONSTRUCT));
    expectedMethods.add(PostConstructBase.class.getDeclaredMethod(METHOD_NAME_PROTECTED_POST_CONSTRUCT));
    expectedMethods.add(PostConstructBase.class.getDeclaredMethod(METHOD_NAME_PACKAGE_PRIVATE_POST_CONSTRUCT));
    expectedMethods.add(PostConstructBase.class.getDeclaredMethod(METHOD_NAME_PRIVATE_POST_CONSTRUCT));
    expectedMethods.add(PostConstructEx2.class.getDeclaredMethod(METHOD_NAME_PRIVATE_POST_CONSTRUCT));
    assertEquals(expectedMethods, new HashSet<>(postConstructMethods));
  }

  @Test(expected = BeanCreationException.class)
  public void testCollectPostConstructMethodsWithParameters() {
    BeanInstanceUtil.collectPostConstructMethods(PostConstructWithParameters.class);
  }

  @Test
  public void testInitializeInstanceBase() {
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
  public void testInitializeInstanceBaseEx1() {
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
  public void testInitializeInstanceBaseEx2() {
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
  public void testInitializeInstanceThrowingRuntimeException() {
    BeanInstanceUtil.initializeBeanInstance(new PostConstructThrowingRuntimeException());
  }

  @Test(expected = BeanCreationException.class)
  public void testInitializeInstanceThrowingException() {
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
