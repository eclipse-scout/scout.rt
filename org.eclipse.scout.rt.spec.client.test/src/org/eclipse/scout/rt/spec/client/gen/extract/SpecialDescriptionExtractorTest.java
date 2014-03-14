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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.TEXTS;
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
 * Test for {@link SpecialDescriptionExtractor}
 */
public class SpecialDescriptionExtractorTest {

  private List<ServiceRegistration> m_testTextService;

  @Before
  public void before() throws InterruptedException {
    m_testTextService = TestingUtility.registerServices(Platform.getBundle("org.eclipse.scout.rt.spec.client"), 1000, new SpecTestDocsTextProviderService());
    TextsThreadLocal.set(new ScoutTexts(SERVICES.getServices(ITextProviderService.class)));
  }

  @After
  public void after() {
    TestingUtility.unregisterServices(m_testTextService);
    TextsThreadLocal.set(null);
  }

  @Test
  public void testGetText() {
    SpecialDescriptionExtractor nameExtractor = new SpecialDescriptionExtractor(TEXTS.get("org.eclipse.scout.rt.spec.type"), "_name");
    SpecialDescriptionExtractor fallbackNameExtractor = createFallbackNameExtractor(false);
    SpecialDescriptionExtractor descriptionExtractor = new SpecialDescriptionExtractor(TEXTS.get("org.eclipse.scout.rt.spec.doc"), "_description");
    assertEquals("Name for AbstractStringField could not be extracted!", "string field name", nameExtractor.getText(AbstractStringField.class));
    assertEquals("Name for AbstractStringField could not be extracted!", "string field name", fallbackNameExtractor.getText(AbstractStringField.class));
    assertEquals("Description for AbstractStringField could not be extracted!", "string field description", descriptionExtractor.getText(AbstractStringField.class));
    assertNull("Expected getText(...) to return null.", nameExtractor.getText(TestClass.class));
    assertEquals("Expected getText(...) to return fallback.", "fallback", fallbackNameExtractor.getText(TestClass.class));
    assertNull("Expected getText to return null.", descriptionExtractor.getText(TestClass.class));
  }

  private SpecialDescriptionExtractor createFallbackNameExtractor(boolean createAnchor) {
    SpecialDescriptionExtractor fallBackNameExtractor = new SpecialDescriptionExtractor(TEXTS.get("org.eclipse.scout.rt.spec.type"), "_name", createAnchor, new IDocTextExtractor<Class>() {

      @Override
      public String getText(Class object) {
        return "fallback";
      }

      @Override
      public String getHeader() {
        return null;
      }
    });
    return fallBackNameExtractor;
  }

  @Test
  public void testGetTextAnchor() {
    SpecialDescriptionExtractor anchorNameExtractor = new SpecialDescriptionExtractor(TEXTS.get("org.eclipse.scout.rt.spec.type"), "_name", true);
    SpecialDescriptionExtractor nameExtractor = new SpecialDescriptionExtractor(TEXTS.get("org.eclipse.scout.rt.spec.type"), "_name");
    SpecialDescriptionExtractor fallbackAnchorNameExtractor = createFallbackNameExtractor(true);
    String anchor = "{{a:c_" + ConfigurationUtility.getAnnotatedClassIdWithFallback(AbstractStringField.class) + "}}";
    assertFalse("Extracted text should not start with anchor.", nameExtractor.getText(AbstractStringField.class).startsWith("{{"));
    assertTrue("Extracted text should start with anchor.", anchorNameExtractor.getText(AbstractStringField.class).startsWith(anchor));
    assertTrue("Extracted text should start with anchor.", fallbackAnchorNameExtractor.getText(AbstractStringField.class).startsWith(anchor));
    assertNull("Expected getText to return null.", nameExtractor.getText(TestClass.class));
    assertNull("Expected getText to return null.", anchorNameExtractor.getText(TestClass.class));
    assertTrue("Extracted text should start with anchor.", fallbackAnchorNameExtractor.getText(TestClass.class).startsWith("{{"));
  }

  public static class TestClass {
  }
}
