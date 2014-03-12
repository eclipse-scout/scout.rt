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

import static org.junit.Assert.assertFalse;
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
  private LinkableTypeExtractor<Object> m_assumeTypesWithClassIdDocumentedExtractor = new LinkableTypeExtractor<Object>(ITypeWithClassId.class, true);

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
    List<ServiceRegistration> service = TestingUtility.registerServices(Platform.getBundle("org.eclipse.scout.rt.spec.client"), 1000, new SpecTestDocsTextProviderService());
    TextsThreadLocal.set(new ScoutTexts(SERVICES.getServices(ITextProviderService.class)));
    try {
      testGetTextInternal(m_extractor, new TestClassWithoutClassId(), TestClassWithoutClassId.class.getSimpleName(), false);
      testGetTextInternal(m_extractor, new TestClassWithClassIdAndDoc(), "Name for " + ConfigurationUtility.getAnnotatedClassIdWithFallback(TestClassWithClassIdAndDoc.class), true);
      testGetTextInternal(m_extractor, new TestSubClassWithFallbackClassId(), "Name for " + ConfigurationUtility.getAnnotatedClassIdWithFallback(TestClassWithClassIdAndDoc.class), true);
      testGetTextInternal(m_extractor, new TestSubClassWithOwnClassIdNoDoc(), "Name for " + ConfigurationUtility.getAnnotatedClassIdWithFallback(TestClassWithClassIdAndDoc.class), true);
      testGetTextInternal(m_extractor, new TestSubClassWithOwnClassIdAndOwnDoc(), "Name for " + ConfigurationUtility.getAnnotatedClassIdWithFallback(TestSubClassWithOwnClassIdAndOwnDoc.class), true);

      testGetTextInternal(m_assumeTypesWithClassIdDocumentedExtractor, new TestClassWithoutClassId(), TestClassWithoutClassId.class.getSimpleName(), false);
      testGetTextInternal(m_assumeTypesWithClassIdDocumentedExtractor, new TestClassWithClassIdAndDoc(), "Name for " + ConfigurationUtility.getAnnotatedClassIdWithFallback(TestClassWithClassIdAndDoc.class), true);
      testGetTextInternal(m_assumeTypesWithClassIdDocumentedExtractor, new TestSubClassWithFallbackClassId(), TestSubClassWithFallbackClassId.class.getSimpleName(), true);
      testGetTextInternal(m_assumeTypesWithClassIdDocumentedExtractor, new TestSubClassWithOwnClassIdNoDoc(), TestSubClassWithOwnClassIdNoDoc.class.getSimpleName(), true);
      testGetTextInternal(m_assumeTypesWithClassIdDocumentedExtractor, new TestSubClassWithOwnClassIdAndOwnDoc(), "Name for " + ConfigurationUtility.getAnnotatedClassIdWithFallback(TestSubClassWithOwnClassIdAndOwnDoc.class), true);
    }
    finally {
      TestingUtility.unregisterServices(service);
    }
  }

  @Test
  public void testGetTextMissingDocs() {
    testGetTextInternal(m_extractor, new TestClassWithoutClassId(), TestClassWithoutClassId.class.getSimpleName(), false);
    testGetTextInternal(m_extractor, new TestClassWithClassIdAndDoc(), TestClassWithClassIdAndDoc.class.getSimpleName(), false);
    testGetTextInternal(m_extractor, new TestSubClassWithFallbackClassId(), TestSubClassWithFallbackClassId.class.getSimpleName(), false);
    testGetTextInternal(m_extractor, new TestSubClassWithOwnClassIdNoDoc(), TestSubClassWithOwnClassIdNoDoc.class.getSimpleName(), false);
    testGetTextInternal(m_extractor, new TestSubClassWithOwnClassIdAndOwnDoc(), TestSubClassWithOwnClassIdAndOwnDoc.class.getSimpleName(), false);

    testGetTextInternal(m_assumeTypesWithClassIdDocumentedExtractor, new TestClassWithoutClassId(), TestClassWithoutClassId.class.getSimpleName(), false);
    testGetTextInternal(m_assumeTypesWithClassIdDocumentedExtractor, new TestClassWithClassIdAndDoc(), TestClassWithClassIdAndDoc.class.getSimpleName(), true);
    testGetTextInternal(m_assumeTypesWithClassIdDocumentedExtractor, new TestSubClassWithFallbackClassId(), TestSubClassWithFallbackClassId.class.getSimpleName(), true);
    testGetTextInternal(m_assumeTypesWithClassIdDocumentedExtractor, new TestSubClassWithOwnClassIdNoDoc(), TestSubClassWithOwnClassIdNoDoc.class.getSimpleName(), true);
    testGetTextInternal(m_assumeTypesWithClassIdDocumentedExtractor, new TestSubClassWithOwnClassIdAndOwnDoc(), TestSubClassWithOwnClassIdAndOwnDoc.class.getSimpleName(), true);
  }

  private void testGetTextInternal(LinkableTypeExtractor<Object> extractor, Object object, String expectedSubstring, boolean expectLink) {
    String text = extractor.getText(object);
    assertTrue("Extracted text [" + text + "] does not contain expected substring [" + expectedSubstring + "].", text.contains(expectedSubstring));
    if (expectLink) {
      assertTrue("Extracted text [" + text + "] does not contain link syntax [\"[[...\"].", text.contains("[["));
    }
    else {
      assertFalse("Extracted text [" + text + "] should not contain link syntax [\"[[...\"].", text.contains("[["));
    }

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
