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

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.annotations.ClassId;
import org.eclipse.scout.rt.shared.Activator;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCodeType;
import org.eclipse.scout.rt.shared.services.lookup.DefaultCodeLookupCallFactoryService;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.eclipse.scout.rt.testing.shared.services.common.code.TestingCodeService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.ServiceRegistration;

/**
 * Test form {@link CodeTypeNameExtractor}
 */
public class CodeTypeNameExtractorTest {
  private static List<ServiceRegistration> s_services;
  private static final String TEST_CODE_TYPE = "Test-CodeType";

  @BeforeClass
  public static void beforeClass() throws Exception {
    TestingCodeService codeService = new TestingCodeService(CollectionUtility.arrayList(new TestCodeType(), new MissingTextCodeType()));
    DefaultCodeLookupCallFactoryService codeLookupCallFactoryService = new DefaultCodeLookupCallFactoryService();
    s_services = TestingUtility.registerServices(Activator.getDefault().getBundle(), 1000, codeService, codeLookupCallFactoryService);
  }

  @AfterClass
  public static void afterClass() throws Exception {
    TestingUtility.unregisterServices(s_services);
  }

  @Test
  public void testGetText() {
    CodeTypeNameExtractor ex = new CodeTypeNameExtractor();
    assertEquals("{{a:c_3cfe2a7a-ca97-4076-b772-942bbb93efc5}}" + TEST_CODE_TYPE, ex.getText(TestCodeType.class));
    assertEquals("{{a:c_3233993b-1ef0-4571-910c-1c939443580b}}MissingTextCodeType", ex.getText(MissingTextCodeType.class));
  }

  @ClassId("3cfe2a7a-ca97-4076-b772-942bbb93efc5")
  private static class TestCodeType extends AbstractCodeType<Long, String> {

    private static final long serialVersionUID = 1L;

    public static final Long ID = Long.valueOf(42);

    @Override
    protected String getConfiguredText() {
      return TEST_CODE_TYPE;
    }

    @Override
    public Long getId() {
      return ID;
    }
  }

  @ClassId("3233993b-1ef0-4571-910c-1c939443580b")
  private static class MissingTextCodeType extends AbstractCodeType<Long, String> {

    private static final long serialVersionUID = 1L;

    public static final Long ID = Long.valueOf(42);

    @Override
    public Long getId() {
      return ID;
    }
  }
}
