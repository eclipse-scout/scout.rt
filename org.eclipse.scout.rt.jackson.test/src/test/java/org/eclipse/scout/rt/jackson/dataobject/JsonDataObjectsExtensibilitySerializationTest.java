/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.jackson.dataobject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.jackson.testing.DataObjectSerializationTestHelper;
import org.eclipse.scout.rt.jackson.testing.TestingJacksonDataObjectMapper;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.dataobject.DataObjectHelper;
import org.eclipse.scout.rt.platform.dataobject.DataObjectInventory;
import org.eclipse.scout.rt.platform.dataobject.DoEntity;
import org.eclipse.scout.rt.platform.dataobject.DoList;
import org.eclipse.scout.rt.platform.dataobject.DoValue;
import org.eclipse.scout.rt.platform.dataobject.IDataObject;
import org.eclipse.scout.rt.platform.dataobject.IDataObjectMapper;
import org.eclipse.scout.rt.platform.dataobject.TypeName;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Various test cases serializing and deserializing Scout data objects from/to JSON with replaced classes within class
 * hierarchy.
 */
public class JsonDataObjectsExtensibilitySerializationTest {

  protected static DataObjectSerializationTestHelper s_testHelper;
  protected static DataObjectHelper s_dataObjectHelper;

  protected static IDataObjectMapper s_dataObjectMapper;

  @BeforeClass
  public static void beforeClass() {
    s_testHelper = BEANS.get(DataObjectSerializationTestHelper.class);
    s_dataObjectHelper = BEANS.get(DataObjectHelper.class);
    s_dataObjectMapper = BEANS.get(TestingJacksonDataObjectMapper.class);
  }

  /* ------ Test case CoreBean as base, a project template bean and two concrete project bean implementations
  
  +---------------------+
  |      CoreBean       |
  +----------^----------+
             |
  +----------+----------+
  | ProjectTemplateBean |
  +----------^----------+
             |
             +---------------------------+
             |                           |
   +--------------------+   +--------------------+
   |  ProjectBean1      |   |    ProjectBean2    |
   +--------------------+   +--------------------+
  
  */

  @IgnoreBean
  @TypeName("TestCore")
  static class TestCoreDo extends DoEntity {
    public DoValue<Boolean> b() {
      return doValue("b");
    }
  }

  @IgnoreBean
  static class TestProjectTemplateDo extends TestCoreDo {
    public DoValue<Integer> i() {
      return doValue("i");
    }
  }

  @IgnoreBean
  static class TestProject1Do extends TestProjectTemplateDo {
    public DoValue<Float> f1() {
      return doValue("f1");
    }
  }

  @IgnoreBean
  static class TestProject2Do extends TestProjectTemplateDo {
    public DoValue<Float> f2() {
      return doValue("f2");
    }
  }

  @IgnoreBean
  static class BaseClassWithoutJsonTypeName {
    public boolean b;
  }

  @Test
  public void testToTypeName() {
    assertEquals("TestCore", BEANS.get(DataObjectInventory.class).toTypeName(TestCoreDo.class));
    assertEquals("TestCore", BEANS.get(DataObjectInventory.class).toTypeName(TestProjectTemplateDo.class));
    assertEquals("TestCore", BEANS.get(DataObjectInventory.class).toTypeName(TestProject1Do.class));
    assertEquals("TestCore", BEANS.get(DataObjectInventory.class).toTypeName(TestProject2Do.class));
  }

  @Test
  public void fromTypeName() {
    assertEquals(TestCoreDo.class, BEANS.get(DataObjectInventory.class).fromTypeName("TestCore"));

    List<IBean<?>> registered = new ArrayList<>();
    registered.add(TestingUtility.registerBean(new BeanMetaData(TestProjectTemplateDo.class).withReplace(true)));
    try {
      assertEquals(TestProjectTemplateDo.class, BEANS.get(DataObjectInventory.class).fromTypeName("TestCore"));

      registered.add(TestingUtility.registerBean(new BeanMetaData(TestProject1Do.class).withReplace(true)));
      assertEquals(TestProject1Do.class, BEANS.get(DataObjectInventory.class).fromTypeName("TestCore"));
    }
    finally {
      TestingUtility.unregisterBeans(registered);
    }
  }

  @Test
  public void testMarshallUnmarshallCoreBean() {
    IBean<?> registered = TestingUtility.registerBean(new BeanMetaData(TestCoreDo.class));
    try {
      TestCoreDo core = new TestCoreDo();
      core.b().set(true);

      TestCoreDo beanMarshalled = marshallUnmarshall(core, "TestCoreDo.json");
      assertTrue(beanMarshalled.b().get());
      assertEquals(TestCoreDo.class, beanMarshalled.getClass());
    }
    finally {
      TestingUtility.unregisterBean(registered);
    }
  }

  @Test
  public void testMarshallUnmarshallProjectTemplateBean() {
    List<IBean<?>> registered = new ArrayList<>();
    registered.add(TestingUtility.registerBean(new BeanMetaData(TestCoreDo.class)));
    registered.add(TestingUtility.registerBean(new BeanMetaData(TestProjectTemplateDo.class).withReplace(true)));
    try {
      TestProjectTemplateDo bean = new TestProjectTemplateDo();
      bean.b().set(true);
      bean.i().set(100);

      TestProjectTemplateDo beanMarshalled = marshallUnmarshall(bean, "TestProjectTemplateDo.json");
      assertEquals(true, beanMarshalled.b().get());
      assertEquals(Integer.valueOf(100), beanMarshalled.i().get());
      assertEquals(TestProjectTemplateDo.class, beanMarshalled.getClass());
    }
    finally {
      TestingUtility.unregisterBeans(registered);
    }
  }

  @Test
  public void testMarshallUnmarshallProjectBean() {
    List<IBean<?>> registered = new ArrayList<>();
    registered.add(TestingUtility.registerBean(new BeanMetaData(TestCoreDo.class)));
    registered.add(TestingUtility.registerBean(new BeanMetaData(TestProjectTemplateDo.class).withReplace(true)));
    registered.add(TestingUtility.registerBean(new BeanMetaData(TestProject1Do.class).withReplace(true)));
    try {
      TestProject1Do bean = new TestProject1Do();
      bean.b().set(true);
      bean.i().set(100);
      bean.f1().set(3.14f);
      TestProject1Do beanMarshalled = marshallUnmarshall(bean, "TestProject1Do.json");
      assertEquals(true, beanMarshalled.b().get());
      assertEquals(Integer.valueOf(100), beanMarshalled.i().get());
      assertEquals(3.14f, beanMarshalled.f1().get(), 0);
      assertEquals(TestProject1Do.class, beanMarshalled.getClass());
    }
    finally {
      TestingUtility.unregisterBeans(registered);
    }
  }

  @Test(expected = AssertionException.class)
  public void testMarshallUnmarshallProjectBeanNotUnique() {
    List<IBean<?>> registered = new ArrayList<>();
    registered.add(TestingUtility.registerBean(new BeanMetaData(TestCoreDo.class)));
    registered.add(TestingUtility.registerBean(new BeanMetaData(TestProjectTemplateDo.class).withReplace(true)));
    registered.add(TestingUtility.registerBean(new BeanMetaData(TestProject1Do.class).withReplace(true)));
    registered.add(TestingUtility.registerBean(new BeanMetaData(TestProject2Do.class).withReplace(true)));
    try {
      TestCoreDo core = new TestCoreDo();
      core.b().set(true);
      marshallUnmarshall(core, "TestCoreDo.json");
    }
    finally {
      TestingUtility.unregisterBeans(registered);
    }
  }

  /* ------ Test case with an ICustomer interface extending IDataObject, multiple implementations, which are then replaced by project classes
  
               +--------------------+
               |    IDataObject     |<-------------+
               +---------^----------+              |
                         |                         |
               +---------+----------+    +-----------------+
               | AbstractDataObject |    |    ICustomer    |
               +---------^----------+    +---------^-------+
                         |                         |
            +------------+--------------+          |
            |                           |          |
  +--------------------+   +--------------------+  |
  |      Person        |   |       Company      |  |
  +--------^-----------+   +----------^---------+  |
           |                          |            |
           +-----------+--------------^------------+
           |                          |
  +--------+-----------+   +----------+---------+
  |   ProjectPerson    |   |   ProjectCompany   |
  +--------------------+   +--------------------+
  
  */

  static interface ITestCustomerDo extends IDataObject {
  }

  static abstract class AbstractTestDataObject extends DoEntity {
    public DoValue<String> id() {
      return doValue("id");
    }
  }

  @TypeName("TestCustomer")
  static class TestCustomerDo extends AbstractTestDataObject {
  }

  static class TestProjectCompanyDo extends TestCompanyDo implements ITestCustomerDo {
  }

  @TypeName("TestCompany")
  static class TestCompanyDo extends AbstractTestDataObject {
  }

  static class TestProjectCustomerDo extends TestCustomerDo implements ITestCustomerDo {
  }

  @TypeName("TestCustomerResponse")
  static class TestCustomerResponse extends DoEntity {
    public DoList<TestCustomerDo> customers() {
      return doList("customers");
    }
  }

  @TypeName("TestICustomerResponse")
  static class TestICustomerResponse extends DoEntity {
    public DoList<ITestCustomerDo> customers() {
      return doList("customers");
    }
  }

  @Test
  public void testBeanInterfaceWithMultipleImplementation() {
    List<IBean<?>> registeredBeans = new ArrayList<>();
    try {
      registeredBeans.add(TestingUtility.registerBean(new BeanMetaData(TestCustomerDo.class)));
      registeredBeans.add(TestingUtility.registerBean(new BeanMetaData(TestProjectCustomerDo.class).withReplace(true)));
      registeredBeans.add(TestingUtility.registerBean(new BeanMetaData(TestCustomerResponse.class)));
      registeredBeans.add(TestingUtility.registerBean(new BeanMetaData(TestICustomerResponse.class)));

      runTestBeanInterfaceWithMultipleImplementation();

      registeredBeans.add(TestingUtility.registerBean(new BeanMetaData(TestCompanyDo.class)));
      registeredBeans.add(TestingUtility.registerBean(new BeanMetaData(TestProjectCompanyDo.class).withReplace(true)));

      runTestBeanInterfaceWithMultipleImplementation();

      TestICustomerResponse customerResponse = new TestICustomerResponse();

      customerResponse.customers().add(BEANS.get(TestProjectCustomerDo.class));
      customerResponse.customers().add(BEANS.get(TestProjectCompanyDo.class));
      marshallUnmarshall(customerResponse, "TestICustomerResponse2.json");
    }
    finally {
      TestingUtility.unregisterBeans(registeredBeans);
    }
  }

  protected void runTestBeanInterfaceWithMultipleImplementation() {
    TestProjectCustomerDo customer = BEANS.get(TestProjectCustomerDo.class);
    TestProjectCustomerDo customerMarshalled = marshallUnmarshall(customer, "TestCustomerDo.json");
    assertEquals(TestProjectCustomerDo.class, customerMarshalled.getClass());

    TestCustomerResponse customerResponse = new TestCustomerResponse();
    customerResponse.customers().add(customer);
    TestCustomerResponse personResponseMarshalled = marshallUnmarshall(customerResponse, "TestCustomerResponse.json");
    assertEquals(TestProjectCustomerDo.class, personResponseMarshalled.customers().get(0).getClass());

    ITestCustomerDo iCustomer = BEANS.get(TestProjectCustomerDo.class);
    ITestCustomerDo iCustomerMarshalled = marshallUnmarshall(iCustomer, "TestCustomerDo.json");
    assertEquals(TestProjectCustomerDo.class, iCustomerMarshalled.getClass());

    TestICustomerResponse iCustomerResponse = new TestICustomerResponse();
    iCustomerResponse.customers().add(iCustomer);
    TestICustomerResponse customerResponseMarshalled = marshallUnmarshall(iCustomerResponse, "TestICustomerResponse.json");
    assertEquals(TestProjectCustomerDo.class, customerResponseMarshalled.customers().get(0).getClass());
  }

  @SuppressWarnings("unchecked")
  protected <T extends IDataObject> T marshallUnmarshall(T object, String expectedResourceName) {
    String json = s_dataObjectMapper.writeValue(object);
    assertJsonEquals(expectedResourceName, json);
    return s_dataObjectMapper.readValue(json, (Class<T>) object.getClass());
  }

  protected void assertJsonEquals(String expectedResourceName, String actual) {
    s_testHelper.assertJsonEquals(getResource(expectedResourceName), actual);
  }

  protected String readResourceAsString(String resourceName) throws IOException {
    return s_testHelper.readResourceAsString(getResource(resourceName));
  }

  protected URL getResource(String expectedResourceName) {
    return JsonDataObjectsExtensibilitySerializationTest.class.getResource(expectedResourceName);
  }
}
