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
package org.eclipse.scout.rt.jackson.databind;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.exception.DefaultRuntimeExceptionTranslator;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * Various tests for JSON marshalling and unmarshalling of polymorphic types using {@link JandexTypeNameIdResolver}.
 */
public class JandexTypeNameIdResolverTest {

  private static final String JSON_TYPE_PROPERTY = "type";

  @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = JSON_TYPE_PROPERTY)
  @JsonTypeIdResolver(JandexTypeNameIdResolver.class)
  @JsonTypeName("Base")
  static class BaseClass {
    public boolean b;
  }

  @JsonTypeName("Impl1")
  static class Impl1 extends BaseClass {
    public int i;
  }

  @JsonTypeName("Impl2")
  static class Impl2 extends BaseClass {
    public String s;
  }

  static class Impl3 extends BaseClass {
    public String s;
  }

  static class BaseClassWrapper {
    public BaseClass baseClass;
  }

  static class MyNotAnnotatedType {
    public long l;
  }

  @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = JSON_TYPE_PROPERTY)
  @JsonTypeIdResolver(JandexTypeNameIdResolver.class)
  static abstract class AbstractBaseClass {
    public boolean b;
  }

  @JsonTypeName("Impl4")
  static class Impl4 extends AbstractBaseClass {
    public float f;
  }

  static class ComplexType {
    public Impl1 i1;
    public MyNotAnnotatedType mnat;
    public BaseClass b;
    public AbstractBaseClass abc;
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
  +------------+--------------+
  |                           |
 +--------------------+   +--------------------+
 |  ProjectBean1      |   |    ProjectBean2    |
 +--------------------+   +--------------------+

*/

  @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = JSON_TYPE_PROPERTY)
  @JsonTypeIdResolver(JandexTypeNameIdResolver.class)
  @JsonTypeName("CoreBean")
  static class CoreBean {
    public boolean b;
  }

  static class ProjectTemplateBean extends CoreBean {
    public int i;
  }

  static class ProjectBean1 extends ProjectTemplateBean {
    public float f1;
  }

  static class ProjectBean2 extends ProjectTemplateBean {
    public float f2;
  }

  @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = JSON_TYPE_PROPERTY)
  @JsonTypeIdResolver(JandexTypeNameIdResolver.class)
  static class BaseClassWithoutJsonTypeName {
    public boolean b;
  }

  private static final List<IBean<?>> s_beans = new ArrayList<>();

  @BeforeClass
  public static void beforeClass() {
    s_beans.add(TestingUtility.registerBean(new BeanMetaData(CoreBean.class)));
    s_beans.add(TestingUtility.registerBean(new BeanMetaData(ProjectTemplateBean.class).withReplace(true)));
  }

  @AfterClass
  public static void afterClass() {
    TestingUtility.unregisterBeans(s_beans);
  }

  @Test
  public void testGetMechanism() {
    assertEquals(Id.NAME, new JandexTypeNameIdResolver().getMechanism());
  }

  @Test
  public void testIdFromBaseType() {
    JandexTypeNameIdResolver resolver = new JandexTypeNameIdResolver();
    resolver.init(TypeFactory.defaultInstance().constructType(CoreBean.class));
    assertEquals("CoreBean", resolver.idFromBaseType());
  }

  @Test
  public void idFromValueAndType() {
    JandexTypeNameIdResolver resolver = new JandexTypeNameIdResolver();
    resolver.init(TypeFactory.defaultInstance().constructType(CoreBean.class));

    assertEquals("CoreBean", resolver.idFromValueAndType(null, CoreBean.class));
    assertEquals("CoreBean", resolver.idFromValueAndType(new CoreBean(), CoreBean.class));
    assertEquals("CoreBean", resolver.idFromValueAndType(new ProjectTemplateBean(), CoreBean.class));

    IBean<?> registeredProjectBean = TestingUtility.registerBean(new BeanMetaData(ProjectBean1.class).withReplace(true));
    try {
      assertEquals("CoreBean", resolver.idFromValueAndType(new ProjectBean1(), CoreBean.class));
      assertEquals("CoreBean", resolver.idFromValueAndType(new ProjectBean1(), ProjectTemplateBean.class));

      resolver.init(TypeFactory.defaultInstance().constructType(ProjectTemplateBean.class));
      assertEquals("CoreBean", resolver.idFromValueAndType(new ProjectBean1(), CoreBean.class));
      assertEquals("CoreBean", resolver.idFromValueAndType(new ProjectBean1(), ProjectTemplateBean.class));
    }
    finally {
      TestingUtility.unregisterBean(registeredProjectBean);
    }
  }

  @Test
  public void testMarshallUnmarshallBaseClassWithoutJsonTypeName() {
    BaseClassWithoutJsonTypeName b = new BaseClassWithoutJsonTypeName();
    b.b = true;
    marshallUnmarshall(b, true, false);
  }

  @Test
  public void testMarshallUnmarshallBaseClass() {
    BaseClass b = new BaseClass();
    b.b = true;

    BaseClass bMarshalled = marshallUnmarshall(b);
    assertEquals(true, bMarshalled.b);
  }

  @Test
  public void testMarshallUnmarshallSubClasses() {
    Impl1 i1 = new Impl1();
    i1.b = true;
    i1.i = 100;
    Impl1 i1Marshalled = marshallUnmarshall(i1);
    assertEquals(true, i1Marshalled.b);
    assertEquals(100, i1Marshalled.i);

    Impl2 i2 = new Impl2();
    i2.b = true;
    i2.s = "foo";
    Impl2 i2Marshalled = marshallUnmarshall(i2);
    assertEquals(true, i2Marshalled.b);
    assertEquals("foo", i2Marshalled.s);
  }

  /**
   * Deserialize instance of subclass Impl3 which does not have a JSON type name. This case normally is covered by using
   * {@link JandexJacksonAnnotationIntrospector} which adds default implementation behavior to serialization.
   */
  @Test(expected = InvalidTypeIdException.class)
  public void testMarshallUnmarshallSubClasses_invalidType() throws Exception {
    Impl3 i3 = new Impl3();
    i3.b = true;
    i3.s = "foo";
    ObjectMapper om = new ObjectMapper();
    String json = om.writeValueAsString(i3);
    json = json.replace("}", ",\"" + JSON_TYPE_PROPERTY + "\":\"foo\"}");
    om.readValue(json, Impl3.class);
  }

  @Test
  public void testMarshallUnmarshallSubClassAsBaseClass() {
    Impl1 i1 = new Impl1();
    i1.b = true;
    i1.i = 100;
    BaseClass b = i1;

    BaseClass bMarshalled = marshallUnmarshall(b);
    assertEquals(true, bMarshalled.b);
    assertTrue(bMarshalled instanceof Impl1);
    assertEquals(100, ((Impl1) bMarshalled).i);
  }

  @Test
  public void testMarshallUnmarshallWrappedBaseClass() {
    Impl1 i1 = new Impl1();
    i1.b = true;
    i1.i = 100;

    BaseClassWrapper wrapper = new BaseClassWrapper();
    wrapper.baseClass = i1;

    BaseClassWrapper wrapperMarshalled = marshallUnmarshall(wrapper);
    assertEquals(true, wrapperMarshalled.baseClass.b);
    assertTrue(wrapperMarshalled.baseClass instanceof Impl1);
    assertEquals(100, ((Impl1) wrapperMarshalled.baseClass).i);

    Impl2 i2 = new Impl2();
    i2.b = true;
    i2.s = "foo";
    wrapper.baseClass = i2;

    BaseClassWrapper wrapperMarshalled2 = marshallUnmarshall(wrapper);
    assertEquals(true, wrapperMarshalled2.baseClass.b);
    assertTrue(wrapperMarshalled2.baseClass instanceof Impl2);
    assertEquals("foo", ((Impl2) wrapperMarshalled2.baseClass).s);
  }

  @Test
  public void testMarshallUnmarshallMixedType() {
    ComplexType ct = new ComplexType();
    ct.i1 = new Impl1();
    ct.i1.b = true;
    ct.i1.i = 100;
    ct.mnat = new MyNotAnnotatedType();
    ct.mnat.l = 1000l;
    Impl2 i2 = new Impl2();
    i2.b = true;
    i2.s = "foo";
    ct.b = i2;
    Impl4 i3 = new Impl4();
    i3.b = true;
    i3.f = 99;
    ct.abc = i3;
    ComplexType ctMarshalled = marshallUnmarshall(ct);
    assertEquals(true, ctMarshalled.i1.b);
    assertEquals(100, ctMarshalled.i1.i);
    assertEquals(1000l, ctMarshalled.mnat.l);
    assertTrue(ctMarshalled.b instanceof Impl2);
    assertEquals(true, ctMarshalled.b.b);
    assertEquals("foo", ((Impl2) ctMarshalled.b).s);
    assertEquals(true, ctMarshalled.abc.b);
    assertEquals(99, ((Impl4) ctMarshalled.abc).f, 0);
  }

  @Test
  public void testMarshallUnmarshallCoreBean() {
    CoreBean bean = new CoreBean();
    bean.b = true;
    CoreBean beanMarshalled = marshallUnmarshall(bean);
    assertTrue(beanMarshalled.b);
    assertEquals(ProjectTemplateBean.class, beanMarshalled.getClass());
  }

  @Test
  public void testMarshallUnmarshallProjectTemplateBean() {
    ProjectTemplateBean bean = new ProjectTemplateBean();
    bean.b = true;
    bean.i = 100;
    ProjectTemplateBean beanMarshalled = marshallUnmarshall(bean);
    assertEquals(true, beanMarshalled.b);
    assertEquals(100, beanMarshalled.i);
    assertEquals(ProjectTemplateBean.class, beanMarshalled.getClass());
  }

  @Test
  public void testMarshallUnmarshallProjectBean() {
    IBean<?> registeredProjectBean = TestingUtility.registerBean(new BeanMetaData(ProjectBean1.class).withReplace(true));
    try {
      ProjectBean1 bean = new ProjectBean1();
      bean.b = true;
      bean.i = 100;
      bean.f1 = 3.14f;
      ProjectBean1 beanMarshalled = marshallUnmarshall(bean);
      assertEquals(true, beanMarshalled.b);
      assertEquals(100, beanMarshalled.i);
      assertEquals(3.14f, beanMarshalled.f1, 0);
      assertEquals(ProjectBean1.class, beanMarshalled.getClass());
    }
    finally {
      TestingUtility.unregisterBean(registeredProjectBean);
    }
  }

  @Test(expected = AssertionException.class)
  public void testMarshallUnmarshallProjectBeanNotUnique() {
    IBean<?> registeredProjectBean1 = TestingUtility.registerBean(new BeanMetaData(ProjectBean1.class).withReplace(true));
    IBean<?> registeredProjectBean2 = TestingUtility.registerBean(new BeanMetaData(ProjectBean2.class).withReplace(true));
    try {
      CoreBean bean = new CoreBean();
      marshallUnmarshall(bean);
    }
    finally {
      TestingUtility.unregisterBean(registeredProjectBean1);
      TestingUtility.unregisterBean(registeredProjectBean2);
    }
  }

  /* ------ Test case with an ICustomer interface extending IDataObject, multiple implementations, which are then replaced by project classes
  
               +--------------------+
               |    IDataObject     |<---------------+
               +---------^----------+                |
                         |                           |
               +---------+----------+      +-----------------+
               | AbstractDataObject |      |    ICustomer    |
               +---------^----------+      +---------^-------+
                         |                           |
            +------------+--------------+            |
            |                           |            |
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

  @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = JSON_TYPE_PROPERTY)
  @JsonTypeIdResolver(JandexTypeNameIdResolver.class)
  static interface IDataObject extends Serializable {
    String getId();

    void setId(String id);
  }

  static interface ICustomer extends IDataObject {
  }

  static abstract class AbstractDataObject implements IDataObject {
    private static final long serialVersionUID = 1L;
    private String m_id;

    @Override
    public String getId() {
      return m_id;
    }

    @Override
    public void setId(String id) {
      m_id = id;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((m_id == null) ? 0 : m_id.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      AbstractDataObject other = (AbstractDataObject) obj;
      if (m_id == null) {
        if (other.m_id != null) {
          return false;
        }
      }
      else if (!m_id.equals(other.m_id)) {
        return false;
      }
      return true;
    }
  }

  @JsonTypeName("Person")
  static class Person extends AbstractDataObject {
    private static final long serialVersionUID = 1L;
  }

  static class ProjectCompany extends Company implements ICustomer {
    private static final long serialVersionUID = 1L;
  }

  @JsonTypeName("Company")
  static class Company extends AbstractDataObject {
    private static final long serialVersionUID = 1L;
  }

  static class ProjectPerson extends Person implements ICustomer {
    private static final long serialVersionUID = 1L;
  }

  @JsonTypeIdResolver(JandexTypeNameIdResolver.class)
  static class PersonResponse {
    List<Person> persons;

    public List<Person> getPersons() {
      return persons;
    }

    public void setPersons(List<Person> persons) {
      this.persons = persons;
    }
  }

  @JsonTypeIdResolver(JandexTypeNameIdResolver.class)
  static class CustomerResponse {
    List<ICustomer> customers;

    public List<ICustomer> getCustomers() {
      return customers;
    }

    public void setCustomers(List<ICustomer> customers) {
      this.customers = customers;
    }
  }

  @Test(expected = PlatformException.class)
  public void testPoJoClass() {
    try {
      ProjectPerson person = new ProjectPerson();
      marshallUnmarshall(person, false, false);
    }
    catch (PlatformException e) {
      assertTrue(e.getCause() instanceof JsonMappingException);
      throw e;
    }
  }

  @Test
  public void testPoJoListInterfaceWithMultipleImplementation() {
    List<ICustomer> customers = new ArrayList<>();
    ProjectPerson person = new ProjectPerson();
    person.setId("personId");
    customers.add(person);
    ProjectCompany company = new ProjectCompany();
    company.setId("companyId");
    customers.add(company);
    List<ICustomer> marshalled = marshallUnmarshall(customers, false, false); // note since List<> is not annotated with @JandexTypeNameIdResolver, no typeId is generated
    assertEquals(2, marshalled.size());
    CollectionUtility.contains(marshalled, person);
    CollectionUtility.contains(marshalled, company);
  }

  @Test
  public void testBeanInterfaceWithMultipleImplementation() {
    List<IBean<?>> registeredBeans = new ArrayList<>();
    try {
      registeredBeans.add(TestingUtility.registerBean(new BeanMetaData(Person.class)));
      registeredBeans.add(TestingUtility.registerBean(new BeanMetaData(ProjectPerson.class).withReplace(true)));

      runTestBeanInterfaceWithMultipleImplementation();

      registeredBeans.add(TestingUtility.registerBean(new BeanMetaData(Company.class)));
      registeredBeans.add(TestingUtility.registerBean(new BeanMetaData(ProjectCompany.class).withReplace(true)));

      runTestBeanInterfaceWithMultipleImplementation();

      CustomerResponse customerResponse = new CustomerResponse();
      customerResponse.customers = new ArrayList<>();
      customerResponse.customers.add(BEANS.get(ProjectPerson.class));
      customerResponse.customers.add(BEANS.get(ProjectCompany.class));
      marshallUnmarshall(customerResponse);
    }
    finally {
      TestingUtility.unregisterBeans(registeredBeans);
    }
  }

  protected void runTestBeanInterfaceWithMultipleImplementation() {
    ProjectPerson person = BEANS.get(ProjectPerson.class);
    ProjectPerson personMarshalled = marshallUnmarshall(person);
    assertEquals(ProjectPerson.class, personMarshalled.getClass());

    PersonResponse personResponse = new PersonResponse();
    personResponse.persons = new ArrayList<>();
    personResponse.persons.add(person);
    PersonResponse personResponseMarshalled = marshallUnmarshall(personResponse);
    assertEquals(ProjectPerson.class, personResponseMarshalled.persons.get(0).getClass());

    CustomerResponse customerResponse = new CustomerResponse();
    ICustomer customer = BEANS.get(ProjectPerson.class);
    ICustomer customerMarshalled = marshallUnmarshall(customer);
    assertEquals(ProjectPerson.class, customerMarshalled.getClass());
    customerResponse.customers = new ArrayList<>();
    customerResponse.customers.add(customer);

    CustomerResponse customerResponseMarshalled = marshallUnmarshall(customerResponse);
    assertEquals(ProjectPerson.class, customerResponseMarshalled.customers.get(0).getClass());
  }

  /* ------ Test case with an ICustomer interface NOT extending IDataObject, multiple implementations, which are then replaced by project classes
  
               +--------------------+
               |    IDataObject     |
               +---------^----------+
                         |
               +---------+----------+      +-----------------+
               | AbstractDataObject |      |    ICustomer2   |
               +---------^----------+      +---------^-------+
                         |                           |
            +------------+--------------+            |
            |                           |            |
    +--------------------+   +--------------------+  |
    |      Person        |   |       Company      |  |
    +--------^-----------+   +----------^---------+  |
             |                          |            |
             +-----------+--------------^------------+
             |                          |
    +--------+-----------+   +----------+---------+
    |   ProjectPerson2   |   |   ProjectCompany2  |
    +--------------------+   +--------------------+
  
  */

  @JsonTypeIdResolver(JandexTypeNameIdResolver.class)
  @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = JSON_TYPE_PROPERTY)
  static interface ICustomer2 {
  }

  static class ProjectCompany2 extends Company implements ICustomer2 {
    private static final long serialVersionUID = 1L;
  }

  static class ProjectPerson2 extends Person implements ICustomer2 {
    private static final long serialVersionUID = 1L;
  }

  @JsonTypeIdResolver(JandexTypeNameIdResolver.class)
  static class CustomerResponse2 {
    List<ICustomer2> customers;

    public List<ICustomer2> getCustomers() {
      return customers;
    }

    public void setCustomers(List<ICustomer2> customers) {
      this.customers = customers;
    }
  }

  @Test
  public void testPoJoInterfaceWithMultipleImplementation() {
    List<IBean<?>> registeredBeans = new ArrayList<>();
    try {
      registeredBeans.add(TestingUtility.registerBean(new BeanMetaData(Person.class)));
      registeredBeans.add(TestingUtility.registerBean(new BeanMetaData(ProjectPerson2.class).withReplace(true)));
      registeredBeans.add(TestingUtility.registerBean(new BeanMetaData(Company.class)));
      registeredBeans.add(TestingUtility.registerBean(new BeanMetaData(ProjectCompany2.class).withReplace(true)));

      CustomerResponse2 customerResponse = new CustomerResponse2();
      customerResponse.customers = new ArrayList<>();
      customerResponse.customers.add(BEANS.get(ProjectPerson2.class));
      customerResponse.customers.add(BEANS.get(ProjectCompany2.class));
      marshallUnmarshall(customerResponse);
    }
    finally {
      TestingUtility.unregisterBeans(registeredBeans);
    }
  }

  /**
   * Test-case with a base class and two subclasses which do not specify an own JSON type identifier.
   * <p>
   * This example is a completeness test for JandexTypeNameIdResolver, in a normal case this scenario is not
   * reproducible due to a check for duplicated replaced classes within bean manager. This test case should fail, since
   * JandexTypeNameIdResolver cannot find the correct matching class if more than one matching subclass is available.
   */
  @Test(expected = InvalidTypeIdException.class)
  public void testPoJoClassWithMultipleImplementation() throws Exception {
    IBean<?> personBean = TestingUtility.registerBean(new BeanMetaData(ProjectPerson.class));
    IBean<?> person2Bean = TestingUtility.registerBean(new BeanMetaData(ProjectPerson2.class));
    try {
      String json = "{\"persons\":[{\"type\":\"Person\"},{\"type\":\"Person\"}]}";
      ObjectMapper mapper = new ObjectMapper();
      mapper.setAnnotationIntrospector(BEANS.get(JandexJacksonAnnotationIntrospector.class));
      mapper.readValue(json, PersonResponse.class);
    }
    finally {
      TestingUtility.unregisterBean(personBean);
      TestingUtility.unregisterBean(person2Bean);
    }
  }

  protected static <T> T marshallUnmarshall(T object) {
    return marshallUnmarshall(object, false, true);
  }

  @SuppressWarnings("unchecked")
  protected static <T> T marshallUnmarshall(T object, boolean useAnnotationIntrospector, boolean expectJsonTypeProperty) {
    ObjectMapper mapper = new ObjectMapper();
    if (useAnnotationIntrospector) {
      mapper.setAnnotationIntrospector(BEANS.get(JandexJacksonAnnotationIntrospector.class));
    }
    try {
      String json = mapper.writeValueAsString(object);
      assertEquals("Expected \"" + JSON_TYPE_PROPERTY + "\" property in JSON string " + json, expectJsonTypeProperty, String.class.cast(json).contains(JSON_TYPE_PROPERTY));
      return (T) mapper.readValue(json, (Class<T>) object.getClass());
    }
    catch (IOException e) {
      throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(e);
    }
  }
}
