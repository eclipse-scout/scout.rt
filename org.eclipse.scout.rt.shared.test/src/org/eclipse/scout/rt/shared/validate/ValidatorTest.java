/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.validate;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.data.form.ValidationRule;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;
import org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData;
import org.eclipse.scout.rt.shared.validate.annotations.FieldReference;
import org.eclipse.scout.rt.shared.validate.annotations.Mandatory;
import org.eclipse.scout.rt.shared.validate.annotations.MaxLength;
import org.eclipse.scout.rt.shared.validate.annotations.MaxValue;
import org.eclipse.scout.rt.shared.validate.annotations.MinValue;
import org.eclipse.scout.rt.shared.validate.annotations.RegexMatch;
import org.eclipse.scout.rt.shared.validate.annotations.Treat0AsNull;
import org.eclipse.scout.service.AbstractService;
import org.eclipse.scout.service.IService;
import org.junit.Assert;
import org.junit.Test;

public class ValidatorTest {

  @Test
  public void testBean() {
    CarBean b = new CarBean();
    validateFail(b);
    b.setId(0L);
    validateFail(b);
    b.setId(-1L);
    validateFail(b);
    b.setId(2L);
    validateSuccess(b);
    b.setWeight(20.0);
    validateSuccess(b);
    b.setId(null);
    validateFail(b);
    b.setId(2L);
    b.setModel("12345678");
    validateSuccess(b);
    b.setModel("123456789");
    validateFail(b);
    b.setModel("12345678");
    validateSuccess(b);
    b.setWeight(123456.0);
    validateFail(b);
  }

  @Test
  public void testBeanEx() {
    CarBeanEx b = new CarBeanEx();
    validateSuccess(b);
    b.setId(-1L);
    validateFail(b);
    b.setId(0L);
    validateSuccess(b);
    b.setWeight(20.0);
    validateFail(b);
    b.setWeight(9.0);
    validateSuccess(b);
  }

  @Test
  public void testFormData() {
    //only address bean
    AddressFormData a = new AddressFormData();
    a.getStreetProperty().setValue("Bahnhofstrasse 0");
    a.getCityProp().setValue("Zurich");
    validateSuccess(a);
    //only person bean
    PersonFormData p = new PersonFormData();
    p.getNameField().setValue("Hans Müller");
    validateSuccess(p);
    //person links to address
    p.getAddressProperty().setValue(a);
    validateSuccess(p);
    //address links to person (mutual circle) must not loop indefinitely
    a.getPersonProperty().setValue(p);
    validateSuccess(p);
    validateSuccess(a);
  }

  @Test
  public void testHelloService() throws Exception {
    Object service;
    Method method;
    //IHelloService.call1(@MaxLength(5) int[] a, @RegexMatch("[0-9]-[A-Z]-[0-9]") String s, @MaxValue(9) @MinValue(1) Long n)
    service = new HelloService();
    method = service.getClass().getMethod("call1", int[].class, String.class, Long.class);
    invokeSuccess(service, method, new Object[]{null, "0-A-1", 1L});
    invokeSuccess(service, method, new Object[]{new int[]{1, 2, 3}, "5-M-5", 5L});
    invokeSuccess(service, method, new Object[]{new int[]{1, 2, 3, 4, 5}, "9-Z-9", 9L});
    invokeFail(service, method, new Object[]{new int[]{1, 2, 3, 4, 5, 6}, "9-Z-9", 9L});
    invokeFail(service, method, new Object[]{new int[]{1, 2, 3, 4}, "9-5-9", 9L});
    invokeSuccess(service, method, new Object[]{new int[]{1, 2, 3, 4}, "9-Z-9", null});
    invokeFail(service, method, new Object[]{new int[]{1, 2, 3, 4}, "9-Z-9", 0L});
    invokeFail(service, method, new Object[]{new int[]{1, 2, 3, 4}, "9-Z-9", 10L});
    //IHelloService.call2(@MaxValue(2) Float m)
    service = new HelloService();
    method = service.getClass().getMethod("call2", Float.class);
    invokeSuccess(service, method, new Object[]{null});
    invokeSuccess(service, method, new Object[]{1.3f});
    invokeSuccess(service, method, new Object[]{2.0f});
    invokeFail(service, method, new Object[]{3f});
    //IHelloService.call3(@MaxValue(3) Float m)
    service = new HelloService();
    method = service.getClass().getMethod("call3", Float.class);
    invokeSuccess(service, method, new Object[]{null});
    invokeSuccess(service, method, new Object[]{1.3f});
    invokeSuccess(service, method, new Object[]{3.0f});
    invokeFail(service, method, new Object[]{3.1f});
  }

  @Test
  public void testHelloServiceEx() throws Exception {
    Object service;
    Method method;
    //IHelloService.call1(@MaxLength(5) int[] a, @RegexMatch("[0-9]-[A-Z]-[0-9]") String s, @MaxValue(9) @MinValue(1) Long n)
    //HelloServiceEx.call1(@MaxLength(2) int[] a, @RegexMatch("[a-c]") String s, @MaxValue(12) Long n)
    service = new HelloServiceEx();
    method = service.getClass().getMethod("call1", int[].class, String.class, Long.class);
    invokeSuccess(service, method, new Object[]{null, "a", 1L});
    invokeSuccess(service, method, new Object[]{new int[]{1}, "b", 5L});
    invokeSuccess(service, method, new Object[]{new int[]{1, 2}, "c", 12L});
    invokeFail(service, method, new Object[]{new int[]{1, 2, 3}, "a", 9L});
    invokeFail(service, method, new Object[]{new int[]{1, 2}, "9", 9L});
    invokeFail(service, method, new Object[]{new int[]{1, 2}, "c", 0L});
    invokeFail(service, method, new Object[]{new int[]{1, 2}, "c", 13L});
    //IHelloService.call2(@MaxValue(2) Float m)
    //HelloServiceEx.call2(@MaxValue(5) Float m)
    service = new HelloServiceEx();
    method = service.getClass().getMethod("call2", Float.class);
    invokeSuccess(service, method, new Object[]{null});
    invokeSuccess(service, method, new Object[]{3.3f});
    invokeSuccess(service, method, new Object[]{5.0f});
    invokeFail(service, method, new Object[]{6f});
    //HelloServiceEx.callEx1(@MaxValue(5) Collection<Long> list)
    service = new HelloServiceEx();
    method = service.getClass().getMethod("callEx1", Collection.class);
    invokeSuccess(service, method, new Object[]{null});
    invokeSuccess(service, method, new Object[]{Arrays.asList(new Object[]{})});
    invokeSuccess(service, method, new Object[]{Arrays.asList(new Object[]{1L, 2L, 3L, 4L, 5L})});
    invokeSuccess(service, method, new Object[]{Arrays.asList(new Object[]{4L, 5L, 6L, 7L})});
    //HelloServiceEx.callEx2(@MaxValue(value = 5, subtree = true) Collection<Long> list)
    service = new HelloServiceEx();
    method = service.getClass().getMethod("callEx2", Collection.class);
    invokeSuccess(service, method, new Object[]{null});
    invokeSuccess(service, method, new Object[]{Arrays.asList(new Object[]{})});
    invokeSuccess(service, method, new Object[]{Arrays.asList(new Object[]{1L, 2L, 3L, 4L, 5L})});
    invokeFail(service, method, new Object[]{Arrays.asList(new Object[]{4L, 5L, 6L, 7L})});
  }

  private void invokeSuccess(Object service, Method m, Object[] args) throws Exception {
    invoke(service, m, args, true);
  }

  private void invokeFail(Object service, Method m, Object[] args) throws Exception {
    invoke(service, m, args, false);
  }

  private void invoke(Object service, Method m, Object[] args, boolean expectSuccess) throws Exception {
    try {
      new DefaultValidator(new IValidationStrategy.PROCESS()).validateMethodCall(m, args);
    }
    catch (Throwable e) {
      if (expectSuccess) {
        Assert.fail("expected success");
      }
      return;
    }
    if (!expectSuccess) {
      Assert.fail("expected failure");
    }
    m.invoke(service, args);
  }

  private void validateSuccess(Object o) {
    validate(o, true);
  }

  private void validateFail(Object o) {
    validate(o, false);
  }

  private void validate(Object o, boolean expectSuccess) {
    try {
      new DefaultValidator(new IValidationStrategy.PROCESS()).validateParameter(o, null);
    }
    catch (Throwable e) {
      if (expectSuccess) {
        Assert.fail("expected success");
      }
      return;
    }
    if (!expectSuccess) {
      Assert.fail("expected failure");
    }
  }

  public static interface IHelloService extends IService {
    void call1(@MaxLength(5) int[] a, @RegexMatch("[0-9]-[A-Z]-[0-9]") String s, @MaxValue(9) @MinValue(1) Long n);

    void call2(@MaxValue(2) Float m);

    void call3(@MaxValue(3) Float m);
  }

  public static class HelloService extends AbstractService implements IHelloService {
    @Override
    public void call1(int[] a, String s, Long n) {
    }

    @Override
    public void call2(Float m) {
    }

    @Override
    public void call3(Float m) {
    }
  }

  public static class HelloServiceEx extends HelloService {
    @Override
    public void call1(@MaxLength(2) int[] a, @RegexMatch("[a-c]") String s, @MaxValue(12) Long n) {

    }

    @Override
    public void call2(@MaxValue(5) Float m) {
      super.call2(m);
    }

    @Override
    public void call3(Float m) {
      super.call3(m);
    }

    public void callEx1(@MaxValue(5) Collection<Long> list) {
    }

    public void callEx2(@MaxValue(value = 5, subtree = true) Collection<Long> list) {
    }

  }

  public static class CarBean {
    @Mandatory
    @MinValue(1)
    @Treat0AsNull
    private Long m_id;
    @MaxLength(8)
    private String m_model;
    @MinValue(1)
    @MaxValue(9999)
    private Double m_weight;

    public Long getId() {
      return m_id;
    }

    public void setId(Long id) {
      m_id = id;
    }

    public String getModel() {
      return m_model;
    }

    public void setModel(String model) {
      m_model = model;
    }

    public double getWeight() {
      return m_weight;
    }

    public void setWeight(double weight) {
      m_weight = weight;
    }
  }

  public static class CarBeanEx extends CarBean {
    @FieldReference("m_id")
    @Mandatory(false)
    @Treat0AsNull(false)
    private static final int M_ID = 0;
    @FieldReference("m_weight")
    @MaxValue(9)
    private static final int M_WEIGHT = 0;
  }

  public static class PersonFormData extends AbstractFormData {

    private static final long serialVersionUID = 1L;

    public NameField getNameField() {
      return getFieldByClass(NameField.class);
    }

    public AddressProp getAddressProperty() {
      return getPropertyByClass(AddressProp.class);
    }

    public class NameField extends AbstractValueFieldData<String> {
      private static final long serialVersionUID = 1L;

      @Override
      protected void initValidationRules(Map<String, Object> ruleMap) {
        ruleMap.put(ValidationRule.MANDATORY, true);
        ruleMap.put(ValidationRule.MAX_LENGTH, 300);
      }
    }

    public class AddressProp extends AbstractPropertyData<AddressFormData> {
      private static final long serialVersionUID = 1L;
    }
  }

  public static class AddressFormData extends AbstractFormData {

    private static final long serialVersionUID = 1L;

    public PersonProp getPersonProperty() {
      return getPropertyByClass(PersonProp.class);
    }

    public StreetProp getStreetProperty() {
      return getPropertyByClass(StreetProp.class);
    }

    public CityProp getCityProp() {
      return getPropertyByClass(CityProp.class);
    }

    public class PersonProp extends AbstractPropertyData<PersonFormData> {
      private static final long serialVersionUID = 1L;
    }

    public class StreetProp extends AbstractPropertyData<String> {
      private static final long serialVersionUID = 1L;

      @FieldReference("m_value")
      @MaxLength(201)
      private static final int M_VALUE_REF = 0;
    }

    public class CityProp extends AbstractPropertyData<String> {
      private static final long serialVersionUID = 1L;

      @FieldReference("m_value")
      @MaxLength(202)
      private static final int M_VALUE_REF = 0;
    }
  }
}
