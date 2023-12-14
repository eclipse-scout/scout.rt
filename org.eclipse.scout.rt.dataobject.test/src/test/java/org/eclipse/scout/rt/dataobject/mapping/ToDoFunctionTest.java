/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.mapping;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.mapping.ToDoFunctionTest.SubTestDo.ToSubTestDoFunction;
import org.eclipse.scout.rt.dataobject.mapping.ToDoFunctionTest.TestDo.ToTestDoFunction;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.junit.Assert;
import org.junit.Test;

public class ToDoFunctionTest {

  @Test
  public void testNoToDoFunction() {
    runWithBeans(() -> Assert.assertThrows(AssertionException.class, () -> BEANS.get(ToDoFunctionHelper.class).toDo(new TestObject(), IToTestDoFunction.class)),
        beanMetaData(TestDo.class),
        beanMetaData(SubTestDo.class));
  }

  @Test
  public void testToDoFunctionWithCorrectOrder() {
    runWithBeans(() -> {
      ToDoFunctionHelper toDoFunctionHelper = BEANS.get(ToDoFunctionHelper.class);

      TestObject test = new TestObject();
      test.setTest("test1");
      TestDo testDo = toDoFunctionHelper.toDo(test, IToTestDoFunction.class);
      assertTrue(testDo instanceof TestDo);
      assertEquals("test1", testDo.getTest());

      SubTestObject subTest = new SubTestObject();
      subTest.setTest("test2");
      subTest.setSubTest("subTest2");
      testDo = toDoFunctionHelper.toDo(subTest, IToTestDoFunction.class);
      assertTrue(testDo instanceof SubTestDo);
      SubTestDo subTestDo = (SubTestDo) testDo;
      assertEquals("test2", subTestDo.getTest());
      assertEquals("subTest2", subTestDo.getSubTest());
    },
        beanMetaData(TestDo.class),
        beanMetaData(ToTestDoFunction.class)
            .withOrder(2),
        beanMetaData(SubTestDo.class),
        beanMetaData(ToSubTestDoFunction.class)
            .withOrder(1));
  }

  @Test
  public void testToDoFunctionWithIncorrectOrder() {
    runWithBeans(() -> {
      ToDoFunctionHelper toDoFunctionHelper = BEANS.get(ToDoFunctionHelper.class);

      TestObject test = new TestObject();
      test.setTest("test1");
      TestDo testDo = toDoFunctionHelper.toDo(test, IToTestDoFunction.class);
      assertTrue(testDo instanceof TestDo);
      assertEquals("test1", testDo.getTest());

      SubTestObject subTest = new SubTestObject();
      subTest.setTest("test2");
      subTest.setSubTest("subTest2");
      testDo = toDoFunctionHelper.toDo(subTest, IToTestDoFunction.class);
      assertFalse(testDo instanceof SubTestDo);
      assertEquals("test2", testDo.getTest());
    },
        beanMetaData(TestDo.class),
        beanMetaData(ToTestDoFunction.class)
            .withOrder(1),
        beanMetaData(SubTestDo.class),
        beanMetaData(ToSubTestDoFunction.class)
            .withOrder(2));
  }

  protected BeanMetaData beanMetaData(Class<?> beanClass) {
    if (beanClass == null) {
      return null;
    }
    return new BeanMetaData(beanClass);
  }

  protected void runWithBeans(Runnable runnable, BeanMetaData... beanMetaDatas) {
    if (runnable == null) {
      return;
    }
    BeanTestingHelper helper = BeanTestingHelper.get();
    List<IBean<?>> registeredBeans = new ArrayList<>();
    try {
      for (BeanMetaData beanMetaData : beanMetaDatas) {
        if (beanMetaData != null) {
          registeredBeans.add(helper.registerBean(beanMetaData));
        }
      }

      runnable.run();
    }
    finally {
      helper.unregisterBeans(registeredBeans);
    }
  }

  protected static class TestObject {
    private String m_test;

    public String getTest() {
      return m_test;
    }

    public void setTest(String test) {
      m_test = test;
    }
  }

  @IgnoreBean
  protected static class TestDo extends DoEntity {

    public DoValue<String> test() {
      return doValue("test");
    }

    /* **************************************************************************
     * CUSTOM CONVENIENCE TO DO FUNCTION
     * *************************************************************************/

    @IgnoreBean
    protected static class ToTestDoFunction extends AbstractToTestDoFunction<TestObject, TestDo> {
      @Override
      public void apply(TestObject test, TestDo testDo) {
        testDo.withTest(test.getTest());
      }
    }

    /* **************************************************************************
     * GENERATED CONVENIENCE METHODS
     * *************************************************************************/

    @Generated("DoConvenienceMethodsGenerator")
    public TestDo withTest(String test) {
      test().set(test);
      return this;
    }

    @Generated("DoConvenienceMethodsGenerator")
    public String getTest() {
      return test().get();
    }
  }

  protected static class SubTestObject extends TestObject {
    private String m_subTest;

    public String getSubTest() {
      return m_subTest;
    }

    public void setSubTest(String subTest) {
      m_subTest = subTest;
    }
  }

  @IgnoreBean
  protected static class SubTestDo extends TestDo {

    public DoValue<String> subTest() {
      return doValue("subTest");
    }

    /* **************************************************************************
     * CUSTOM CONVENIENCE TO DO FUNCTION
     * *************************************************************************/

    @IgnoreBean
    protected static class ToSubTestDoFunction extends AbstractToTestDoFunction<SubTestObject, SubTestDo> {
      @Override
      public void apply(SubTestObject subTest, SubTestDo subTestDo) {
        BEANS.get(ToTestDoFunction.class).apply(subTest, subTestDo);
        subTestDo.withSubTest(subTest.getSubTest());
      }
    }

    /* **************************************************************************
     * GENERATED CONVENIENCE METHODS
     * *************************************************************************/

    @Generated("DoConvenienceMethodsGenerator")
    public SubTestDo withSubTest(String subTest) {
      subTest().set(subTest);
      return this;
    }

    @Generated("DoConvenienceMethodsGenerator")
    public String getSubTest() {
      return subTest().get();
    }

    @Override
    @Generated("DoConvenienceMethodsGenerator")
    public SubTestDo withTest(String test) {
      test().set(test);
      return this;
    }
  }

  protected interface IToTestDoFunction extends IToDoFunction<TestObject, TestDo> {
  }

  protected abstract static class AbstractToTestDoFunction<EXPLICIT_SOURCE extends TestObject, EXPLICIT_TARGET extends TestDo> extends AbstractToDoFunction<EXPLICIT_SOURCE, EXPLICIT_TARGET, TestObject, TestDo> implements IToTestDoFunction {
  }
}
