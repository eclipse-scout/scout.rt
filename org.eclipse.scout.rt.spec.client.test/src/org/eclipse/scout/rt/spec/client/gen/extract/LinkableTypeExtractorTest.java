/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.spec.client.gen.extract;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.ITypeWithClassId;
import org.eclipse.scout.commons.annotations.ClassId;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.TextsThreadLocal;
import org.eclipse.scout.rt.shared.services.common.text.ITextProviderService;
import org.eclipse.scout.rt.spec.client.text.SpecTestDocsTextProviderService;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.eclipse.scout.service.SERVICES;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.ServiceRegistration;

/**
 * Test form {@link LinkableTypeExtractor}
 */
public class LinkableTypeExtractorTest {

  private LinkableTypeExtractor<Object> m_extractor = new LinkableTypeExtractor<Object>();

  @Before
  public void before() {
    TextsThreadLocal.set(new ScoutTexts(SERVICES.getServices(ITextProviderService.class)));
  }

  @After
  public void after() {
    TextsThreadLocal.set(null);
  }

  @Test
  public void testGetTextDocsAvailable() {
    List<ServiceRegistration> registerServices = TestingUtility.registerServices(Platform.getBundle("org.eclipse.scout.rt.spec.client"), 5, new SpecTestDocsTextProviderService());
    TextsThreadLocal.set(new ScoutTexts(SERVICES.getServices(ITextProviderService.class)));
    try {
      testGetTextInternal(new TestClassWithoutClassId(), TestClassWithoutClassId.class.getSimpleName());
      testGetTextInternal(new TestClassWithClassIdAndDoc(), "Name for " + ConfigurationUtility.getAnnotatedClassIdWithFallback(TestClassWithClassIdAndDoc.class));
      testGetTextInternal(new TestSubClassWithFallbackClassId(), "Name for " + ConfigurationUtility.getAnnotatedClassIdWithFallback(TestClassWithClassIdAndDoc.class));
      testGetTextInternal(new TestSubClassWithOwnClassIdNoDoc(), "Name for " + ConfigurationUtility.getAnnotatedClassIdWithFallback(TestClassWithClassIdAndDoc.class));
      testGetTextInternal(new TestSubClassWithOwnClassIdAndOwnDoc(), "Name for " + ConfigurationUtility.getAnnotatedClassIdWithFallback(TestSubClassWithOwnClassIdAndOwnDoc.class));
    }
    finally {
      TestingUtility.unregisterServices(registerServices);
    }
  }

  @Test
  public void testGetTextMissingDocs() {
    testGetTextInternal(new TestClassWithoutClassId(), TestClassWithoutClassId.class.getSimpleName());
    testGetTextInternal(new TestClassWithClassIdAndDoc(), TestClassWithClassIdAndDoc.class.getSimpleName());
    testGetTextInternal(new TestSubClassWithFallbackClassId(), TestSubClassWithFallbackClassId.class.getSimpleName());
    testGetTextInternal(new TestSubClassWithOwnClassIdNoDoc(), TestSubClassWithOwnClassIdNoDoc.class.getSimpleName());
    testGetTextInternal(new TestSubClassWithOwnClassIdAndOwnDoc(), TestSubClassWithOwnClassIdAndOwnDoc.class.getSimpleName());
  }

  private void testGetTextInternal(Object object, String expectedSubstring) {
    String text = m_extractor.getText(object);
    assertTrue("Extracted text [" + text + "] does not contain expected substring [" + expectedSubstring + "].", text.contains(expectedSubstring));
  }

  private class TestClassWithoutClassId {
  }

  @ClassId("d8000f16-6eed-489e-a792-c01a399c5260")
  private class TestClassWithClassIdAndDoc implements ITypeWithClassId {
    @Override
    public String classId() {
      return ConfigurationUtility.getAnnotatedClassIdWithFallback(getClass());
    }
  }

  private class TestSubClassWithFallbackClassId extends TestClassWithClassIdAndDoc {
  }

  @ClassId("5767a332-a0c3-4aee-a8e1-20be98841203")
  private class TestSubClassWithOwnClassIdNoDoc extends TestClassWithClassIdAndDoc {
  }

  @ClassId("aaa81f58-d4c7-4dff-bc50-8af9fb6584a8")
  private class TestSubClassWithOwnClassIdAndOwnDoc extends TestClassWithClassIdAndDoc {
  }
}
