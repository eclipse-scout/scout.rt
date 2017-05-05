/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.jackson.databind;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

/**
 * Test for {@link JandexJacksonAnnotationIntrospector} used to deserialize JSON strings into polymorphic types with and
 * without providing objectType as part of JSON string.
 */
public class JandexJacksonAnnotationIntrospectorTest {

  private static final Logger LOG = LoggerFactory.getLogger(JandexJacksonAnnotationIntrospectorTest.class);

  private static final List<IBean<?>> s_beans = new ArrayList<>();
  private static final String TEST_ID = "testId";
  private static final String JSON_TYPE_PROPERTY = "type";

  /**
   * Base interface for all fixture classes (Scout beans and POJOs)
   */
  protected static interface ITestObject {
    String getId();
  }

  // ---------- Fixture classes registered within bean manager ----------

  @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = JSON_TYPE_PROPERTY)
  @JsonTypeIdResolver(JandexTypeNameIdResolver.class)
  protected static abstract class AbstractBeanBaseClass implements ITestObject, Serializable {
    private static final long serialVersionUID = 1L;
    private String m_id;

    @Override
    public String getId() {
      return m_id;
    }

    public void setId(String id) {
      m_id = id;
    }
  }

  protected static class BeanBaseClass extends AbstractBeanBaseClass {
    private static final long serialVersionUID = 1L;
  }

  protected static class BeanChildClassA extends BeanBaseClass {
    private static final long serialVersionUID = 1L;
  }

  protected static class BeanChildClassB extends BeanBaseClass {
    private static final long serialVersionUID = 1L;
  }

  protected static class BeanChildClassBSub extends BeanChildClassB {
    private static final long serialVersionUID = 1L;
  }

  @JsonTypeName("ChildC")
  protected static class BeanChildClassC extends BeanBaseClass {
    private static final long serialVersionUID = 1L;
  }

  @JsonTypeName("ChildCSub1")
  protected static class BeanChildClassCSub1 extends BeanChildClassC {
    private static final long serialVersionUID = 1L;
  }

  @JsonTypeName("ChildCSub2")
  protected static class BeanChildClassCSub2 extends BeanChildClassC {
    private static final long serialVersionUID = 1L;
  }

  protected static class BeanChildClassD extends BeanBaseClass {
    private static final long serialVersionUID = 1L;
  }

  // ---------- Fixture classes NOT registered within bean manager ----------

  @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = JSON_TYPE_PROPERTY)
  @JsonTypeIdResolver(JandexTypeNameIdResolver.class)
  protected static class ClassWithTypeInfoWithoutTypeName implements ITestObject {
    private String m_id;

    @Override
    public String getId() {
      return m_id;
    }

    public void setId(String id) {
      m_id = id;
    }
  }

  protected static class ClassWithoutTypeInfo implements ITestObject {
    private String m_id;

    @Override
    public String getId() {
      return m_id;
    }

    public void setId(String id) {
      m_id = id;
    }
  }

  @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = JSON_TYPE_PROPERTY, defaultImpl = ClassWithAbstractTypeInfoAndDefaultImpl.class)
  @JsonTypeIdResolver(JandexTypeNameIdResolver.class)
  protected abstract static class AbstractClassWithTypeInfoAndDefaultImpl {
  }

  protected static class ClassWithAbstractTypeInfoAndDefaultImpl extends AbstractClassWithTypeInfoAndDefaultImpl implements ITestObject {
    private String m_id;

    @Override
    public String getId() {
      return m_id;
    }

    public void setId(String id) {
      m_id = id;
    }
  }

  @BeforeClass
  public static void beforeClass() {
    // Note AbstractBeanBaseClass is not registered in bean manager, since abstract classes are also excluded from bean manager registration in real Scout platform implementation (see BeanFilter class)
    TestingUtility.registerBean(new BeanMetaData(BeanBaseClass.class));
    TestingUtility.registerBean(new BeanMetaData(BeanChildClassA.class));
    TestingUtility.registerBean(new BeanMetaData(BeanChildClassB.class));
    TestingUtility.registerBean(new BeanMetaData(BeanChildClassBSub.class).withReplace(true));
    TestingUtility.registerBean(new BeanMetaData(BeanChildClassC.class));
    TestingUtility.registerBean(new BeanMetaData(BeanChildClassCSub1.class));
    TestingUtility.registerBean(new BeanMetaData(BeanChildClassCSub2.class).withReplace(true));
  }

  @AfterClass
  public static void afterClass() {
    TestingUtility.unregisterBeans(s_beans);
  }

  @Test(expected = JsonMappingException.class)
  public void testDeserializeAbstractBaseClassJsonWithoutObjectType() throws Exception {
    String json = "{\"id\": \"" + TEST_ID + "\" }";
    ObjectMapper om = newObjectMapper();
    om.readValue(json, AbstractBeanBaseClass.class);
  }

  @Test
  public void testDeserializeBaseClassJsonWithoutObjectType() throws Exception {
    runTestDeserialize(BeanBaseClass.class);
  }

  @Test
  public void testDeserializeChildClassA() throws Exception {
    runTestDeserialize(BeanChildClassA.class);
  }

  @Test
  public void testDeserializeChildClassB() throws Exception {
    runTestDeserialize(BeanChildClassB.class, BeanChildClassBSub.class);
    runTestDeserialize(BeanChildClassBSub.class, BeanChildClassBSub.class);
  }

  @Test
  public void testDeserializeClassWithTypeInfoJsonWithoutObjectType() throws Exception {
    runTestDeserialize(ClassWithTypeInfoWithoutTypeName.class);
  }

  @Test
  public void testDeserializeClassWithoutTypeInfo() throws Exception {
    runTestDeserialize(ClassWithoutTypeInfo.class);
  }

  @Test
  public void testDeserializeAbstractClassWithoutTypeInfoAndDefaultImpl() throws Exception {
    // abstract class AbstractClassWithTypeInfoAndDefaultImpl is annotated with default implementation
    runTestDeserialize(AbstractClassWithTypeInfoAndDefaultImpl.class, ClassWithAbstractTypeInfoAndDefaultImpl.class);
  }

  @Test
  public void testDeserializeClassWithoutTypeInfoAndDefaultImpl() throws Exception {
    // abstract base class AbstractClassWithTypeInfoAndDefaultImpl is annotated with default implementation, but concrete class ClassWithTypeInfoAndDefaultImpl is not and @JsonTypeInfo annotation is not inherited
    runTestDeserialize(ClassWithAbstractTypeInfoAndDefaultImpl.class);
  }

  @Test
  public void testDeserializeReplacedBaseClassWithTypeInfo() throws Exception {
    IBean<?> registeredReplacedBean = TestingUtility.registerBean(new BeanMetaData(BeanChildClassD.class).withReplace(true));
    try {
      runTestDeserialize(BeanBaseClass.class, BeanChildClassD.class, null);
      runTestDeserialize(BeanBaseClass.class, BeanChildClassD.class, "Foo");
    }
    finally {
      TestingUtility.unregisterBean(registeredReplacedBean);
    }
  }

  @Test
  public void testDeserializeBaseClassWithTypeInfo() throws Exception {
    runTestDeserialize(BeanBaseClass.class, BeanBaseClass.class, null);
    runTestDeserialize(BeanBaseClass.class, BeanChildClassCSub2.class, "ChildC");
    runTestDeserialize(BeanBaseClass.class, BeanChildClassCSub1.class, "ChildCSub1");
    runTestDeserialize(BeanBaseClass.class, BeanChildClassCSub2.class, "ChildCSub2");

    runTestDeserialize(BeanChildClassC.class, BeanChildClassCSub2.class, null);
    runTestDeserialize(BeanChildClassC.class, BeanChildClassCSub2.class, "ChildC");
    runTestDeserialize(BeanChildClassC.class, BeanChildClassCSub2.class, "foo"); // correct class cannot be found by id -> default implementation ChildClassCSub2 is chosen
    runTestDeserialize(BeanChildClassC.class, BeanChildClassCSub1.class, "ChildCSub1");
    runTestDeserialize(BeanChildClassC.class, BeanChildClassCSub2.class, "ChildCSub2");

    runTestDeserialize(BeanChildClassCSub1.class, BeanChildClassCSub1.class, null);
    runTestDeserialize(BeanChildClassCSub1.class, BeanChildClassCSub1.class, "ChildC"); // correct class cannot be found by id -> default implementation ChildClassCSub1 is chosen
    runTestDeserialize(BeanChildClassCSub1.class, BeanChildClassCSub1.class, "ChildCSub1");
    runTestDeserialize(BeanChildClassCSub1.class, BeanChildClassCSub1.class, "ChildCSub2"); // correct class cannot be found by id -> default implementation ChildClassCSub1 is chosen

    runTestDeserialize(BeanChildClassCSub2.class, BeanChildClassCSub2.class, null);
    runTestDeserialize(BeanChildClassCSub2.class, BeanChildClassCSub2.class, "ChildC"); // correct class cannot be found by id -> default implementation ChildClassCSub1 is chosen
    runTestDeserialize(BeanChildClassCSub2.class, BeanChildClassCSub2.class, "ChildCSub1");
    runTestDeserialize(BeanChildClassCSub2.class, BeanChildClassCSub2.class, "ChildCSub2"); // correct class cannot be found by id -> default implementation ChildClassCSub1 is chosen
  }

  /**
   * Run deserialization test without adding a JSON type name to JSON string and expecting resulting class to be of type
   * {@code clazz}.
   */
  protected void runTestDeserialize(Class<? extends ITestObject> clazz) throws Exception {
    runTestDeserialize(clazz, clazz, null);
  }

  /**
   * Run deserialization test without adding a JSON type name to JSON string and expecting resulting class to be of type
   * {@code expectedClazz}.
   */
  protected void runTestDeserialize(Class<?> clazz, Class<? extends ITestObject> expectedClazz) throws Exception {
    runTestDeserialize(clazz, expectedClazz, null);
  }

  /**
   * Run deserialization test adding a JSON type name as specified {@code typePropertyValue} to JSON string and
   * expecting resulting class to be of type {@code expectedClazz}.
   */
  protected void runTestDeserialize(Class<?> clazz, Class<? extends ITestObject> expectedClazz, String typePropertyValue) throws Exception {
    String jsonTypeProperty = typePropertyValue != null ? ", \"" + JSON_TYPE_PROPERTY + "\":\"" + typePropertyValue + "\"" : "";
    String json = "{\"id\": \"" + TEST_ID + "\"" + jsonTypeProperty + "}";
    ObjectMapper om = newObjectMapper();
    Object instance = om.readValue(json, clazz);
    LOG.info("Deserialized json\n{}\n to instance {} of class {} ", json, instance, clazz);
    assertEquals(expectedClazz, instance.getClass());
    assertEquals(TEST_ID, ITestObject.class.cast(instance).getId());
  }

  /**
   * Creates new {@link ObjectMapper} instance pre-configured to use the custom
   * {@link JandexJacksonAnnotationIntrospector}.
   */
  protected ObjectMapper newObjectMapper() {
    return BEANS.get(ObjectMapperFactory.class).create();
  }
}
