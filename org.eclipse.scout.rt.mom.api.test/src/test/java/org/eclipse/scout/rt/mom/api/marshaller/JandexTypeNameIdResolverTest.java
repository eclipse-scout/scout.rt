/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.mom.api.marshaller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.eclipse.scout.rt.platform.BEANS;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

/**
 * Various tests for JSON marshalling and unmarshalling of polymorphic types using {@link JandexTypeNameIdResolver}.
 */
public class JandexTypeNameIdResolverTest {

  @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
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

  static class BaseClassWrapper {
    public BaseClass baseClass;
  }

  static class MyNotAnnotatedType {
    public long l;
  }

  @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
  @JsonTypeIdResolver(JandexTypeNameIdResolver.class)
  static abstract class AbstractBaseClass {
    public boolean b;
  }

  @JsonTypeName("Impl3")
  static class Impl3 extends AbstractBaseClass {
    public float f;
  }

  static class ComplexType {
    public Impl1 i1;
    public MyNotAnnotatedType mnat;
    public BaseClass b;
    public AbstractBaseClass abc;
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
    Impl3 i3 = new Impl3();
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
    assertEquals(99, ((Impl3) ctMarshalled.abc).f, 0);
  }

  @SuppressWarnings("unchecked")
  protected static <T> T marshallUnmarshall(T object) {
    HashMap<String, String> emptyContext = new HashMap<>();
    JsonMarshaller marshaller = BEANS.get(JsonMarshaller.class);
    Object json = marshaller.marshall(object, emptyContext);
    return (T) marshaller.unmarshall(json, emptyContext);
  }
}
