/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.resource;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SequencedMap;

import jakarta.ws.rs.POST;

import org.eclipse.scout.rt.dataobject.DoMapEntity;
import org.eclipse.scout.rt.dataobject.value.DateValueDo;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class AbstractRestResourceAnnotationTestTest {
  @Test
  public void testAcceptMethodForMapReturnValueCheck() {
    FixtureAbstractRestResourceAnnotationTest test = new FixtureAbstractRestResourceAnnotationTest();
    List<String> methodNames = Arrays.stream(FixtureResource.class.getMethods())
        .filter(test::acceptMethodForMapReturnValueCheck)
        .map(Method::getName)
        .sorted()
        .toList();
    assertEquals(List.of("restFunction", "voidFunction"), methodNames);
  }

  @Test
  public void testTestMethodForMapReturnValue() throws NoSuchMethodException {
    assertMethodReported(FixtureResource.class.getDeclaredMethod("restFunction"), true);
    assertMethodReported(FixtureResource.class.getDeclaredMethod("missingAnnotation"), true);
    assertMethodReported(FixtureResource.class.getDeclaredMethod("privateFunction"), true);
    assertMethodReported(FixtureResource.class.getDeclaredMethod("packagePrivateFunction"), true);
    assertMethodReported(FixtureResource.class.getDeclaredMethod("protectedFunction"), false);
    assertMethodReported(FixtureResource.class.getDeclaredMethod("staticFunction"), false);
    assertMethodReported(FixtureResource.class.getDeclaredMethod("voidFunction"), false);
  }

  protected void assertMethodReported(Method m, boolean expectReported) {
    FixtureAbstractRestResourceAnnotationTest test = new FixtureAbstractRestResourceAnnotationTest();
    test.testMethodForMapReturnValue(m);
    boolean hasError = false;
    try {
      test.m_errorCollector.apply(new EmptyStatement(), Description.EMPTY).evaluate();
    }
    catch (Throwable t) {
      hasError = true;
    }
    assertEquals(expectReported, hasError);
  }

  private static final class EmptyStatement extends Statement {
    @Override
    public void evaluate() {
    }
  }

  @SuppressWarnings({"RestResourceMethodInspection", "unused", "ProtectedMemberInFinalClass"})
  private static final class FixtureResource {
    public FixtureResource() {
      // nop
    }

    @POST
    public Map<String, DateValueDo> restFunction() {
      return null;
    }

    public HashMap<String, DateValueDo> missingAnnotation() {
      return null;
    }

    @POST
    private LinkedHashMap<String, DateValueDo> privateFunction() {
      return null;
    }

    @POST
    SequencedMap<String, DateValueDo> packagePrivateFunction() {
      return null;
    }

    @POST
    protected DoMapEntity<DateValueDo> protectedFunction() {
      return null;
    }

    @POST
    public static DoMapEntity<DateValueDo> staticFunction() {
      return null;
    }

    @POST
    public void voidFunction() {
    }
  }

  private static final class FixtureAbstractRestResourceAnnotationTest extends AbstractRestResourceAnnotationTest {
    @Override
    protected String getPackageNamePrefix() {
      return "org.eclipse.scout.rt.rest";
    }
  }
}
