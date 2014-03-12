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

import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.ITypeWithClassId;
import org.eclipse.scout.commons.annotations.ClassId;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
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
 * Test for {@link DescriptionExtractor}
 */
public class DescriptionExtractorTest {

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
  public void testGetText() throws InterruptedException {
    DescriptionExtractor<IFormField> descExtractor = new DescriptionExtractor<IFormField>();
    assertEquals("Description of TestTextField", descExtractor.getText(new TestTextFieldWithClassIdAndDoc()));
    assertEquals("docOne", descExtractor.getText(new TestTextField1WithComposedClassId()));
    assertEquals("docOne_docTwo", descExtractor.getText(new TestTextField2WithComposedClassId()));
    assertEquals("docOne_docTwo_docThree", descExtractor.getText(new TestTextField3WithComposedClassId()));
    assertEquals("", descExtractor.getText(new TestTextFieldWithClassIdWithoutDoc()));
    assertEquals("", descExtractor.getText(new TestTextFieldWithoutClassId()));
  }

  @ClassId("4306a8dc-7764-4356-af76-d88abafe8a01")
  private class TestTextFieldWithClassIdAndDoc extends AbstractStringField {
  }

  @ClassId("29bd91e6-3490-4e0c-ad52-eae77f4b8e1c")
  private class TestTextFieldWithClassIdWithoutDoc extends AbstractStringField {
  }

  private class TestTextField1WithComposedClassId extends AbstractStringField {
    @Override
    public String classId() {
      return "doc1" + ITypeWithClassId.ID_CONCAT_SYMBOL + "noDoc" + ITypeWithClassId.ID_CONCAT_SYMBOL + "noDoc";
    }
  }

  private class TestTextField2WithComposedClassId extends AbstractStringField {
    @Override
    public String classId() {
      return "doc1" + ITypeWithClassId.ID_CONCAT_SYMBOL + "doc2" + ITypeWithClassId.ID_CONCAT_SYMBOL + "noDoc";
    }
  }

  private class TestTextField3WithComposedClassId extends AbstractStringField {
    @Override
    public String classId() {
      return "doc1" + ITypeWithClassId.ID_CONCAT_SYMBOL + "doc2" + ITypeWithClassId.ID_CONCAT_SYMBOL + "doc3";
    }
  }

  private class TestTextFieldWithoutClassId extends AbstractStringField {
  }
}
