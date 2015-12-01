/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.services.common.code;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.platform.internal.BeanInstanceUtil;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * JUnit test for {@link CodeService}. See Bug 444213.
 *
 * @since 4.3.0 (Mars-M5)
 */
@RunWith(PlatformTestRunner.class)
public class CodeServiceTest {
  private static final Long ABC_ID = 150L;
  private static final Long ZYX_ID = 550L;

  /**
   * Test method for
   * {@link org.eclipse.scout.rt.shared.services.common.code.CodeService#getAllCodeTypeClasses(java.lang.String)} .
   */
  @Test
  public void testGetAllCodeTypeClasses() {
    ICodeService service = newCodeServiceInstance();
    Collection<Class<? extends ICodeType<?, ?>>> codeTypeClasses1 = service.getAllCodeTypeClasses("");
    assertEquals("codeTypeClasses1 size", 2, codeTypeClasses1.size());
    assertEquals("codeTypeClasses1 contains AbcCodeType", true, codeTypeClasses1.contains(AbcCodeType.class));
    assertEquals("codeTypeClasses1 contains ZyxCodeType", true, codeTypeClasses1.contains(ZyxCodeType.class));

    Collection<Class<? extends ICodeType<?, ?>>> codeTypeClasses2 = service.getAllCodeTypeClasses("");
    assertEquals("codeTypeClasses2 size", 1, codeTypeClasses2.size());
    assertEquals("codeTypeClasses2 contains AbcCodeType", true, codeTypeClasses2.contains(AbcCodeType.class));
    assertEquals("codeTypeClasses2 contains ZyxCodeType", false, codeTypeClasses2.contains(ZyxCodeType.class));
  }

  /**
   * Test method for
   * {@link org.eclipse.scout.rt.shared.services.common.code.CodeService#getAllCodeTypes(java.lang.String)} .
   */
  @Test
  public void testGetAllCodeTypesString() {
    ICodeService service = newCodeServiceInstance();
    Collection<ICodeType<?, ?>> codeTypes1 = service.getAllCodeTypes("");
    assertEquals("size", 2, codeTypes1.size());
    Set<Class<?>> codeTypeClasses = new HashSet<Class<?>>(2);
    for (ICodeType<?, ?> ct : codeTypes1) {
      codeTypeClasses.add(ct.getClass());
    }
    assertTrue(codeTypeClasses.contains(AbcCodeType.class));
    assertTrue(codeTypeClasses.contains(ZyxCodeType.class));

    Collection<ICodeType<?, ?>> codeTypes2 = service.getAllCodeTypes("");
    assertEquals("size", 1, codeTypes2.size());
    assertEquals("codeType 2 (0)", AbcCodeType.class, CollectionUtility.firstElement(codeTypes2).getClass());
  }

  /**
   * Test method for {@link org.eclipse.scout.rt.shared.services.common.code.CodeService#getCodeType(java.lang.Class)}.
   */
  @Test
  public void testGetCodeTypeClass() {
    ICodeService service = newCodeServiceInstance();
    service.getAllCodeTypes("");

    AbcCodeType abcCodeType1 = service.getCodeType(AbcCodeType.class);
    AbcCodeType abcCodeType2 = service.getCodeType(AbcCodeType.class);
    assertSame(abcCodeType1, abcCodeType2);

    ZyxCodeType zyxCodeType1 = service.getCodeType(ZyxCodeType.class);
    ZyxCodeType zyxCodeType2 = service.getCodeType(ZyxCodeType.class);
    assertSame(zyxCodeType1, zyxCodeType2);
  }

  /**
   * Test method for {@link org.eclipse.scout.rt.shared.services.common.code.CodeService#getCodeTypes(List)}.
   */
  @Test
  public void testGetCodeTypesList() {
    ICodeService service = newCodeServiceInstance();
    service.getAllCodeTypes("");

    List<Class<? extends ICodeType<?, ?>>> types1 = new ArrayList<Class<? extends ICodeType<?, ?>>>();
    types1.add(AbcCodeType.class);
    types1.add(ZyxCodeType.class);
    List<ICodeType<?, ?>> codeTypes1 = service.getCodeTypes(types1);
    assertEquals("codeTypes1 size", 2, codeTypes1.size());

    List<Class<? extends ICodeType<?, ?>>> types2 = new ArrayList<Class<? extends ICodeType<?, ?>>>();
    types2.add(AbcCodeType.class);
    List<ICodeType<?, ?>> codeTypes2 = service.getCodeTypes(types2);
    assertEquals("codeTypes2 size", 1, codeTypes2.size());

    List<Class<? extends ICodeType<?, ?>>> types3 = new ArrayList<Class<? extends ICodeType<?, ?>>>();
    types3.add(ZyxCodeType.class);
    List<ICodeType<?, ?>> codeTypes3 = service.getCodeTypes(types3);
    assertEquals("codeTypes2 size", 1, codeTypes3.size());

    assertSame(codeTypes1.get(0), codeTypes2.get(0));
    assertSame(codeTypes1.get(1), codeTypes3.get(0));

    List<Class<? extends ICodeType<?, ?>>> types4 = new ArrayList<Class<? extends ICodeType<?, ?>>>();
    types4.add(ZyxCodeType.class);
    types4.add(AbcCodeType.class);
    List<ICodeType<?, ?>> codeTypes4 = service.getCodeTypes(types4);
    assertEquals("codeTypes4 size", 2, codeTypes4.size());

    assertSame(codeTypes1.get(0), codeTypes4.get(1));
    assertSame(codeTypes1.get(1), codeTypes4.get(0));
  }

  /**
   * Test method for {@link org.eclipse.scout.rt.shared.services.common.code.CodeService#reloadCodeType(Class)}.
   *
   * @throws Exception
   */
  @Test
  public void testReloadCodeType() throws Exception {
    ICodeService service = newCodeServiceInstance();
    service.getAllCodeTypes("");

    AbcCodeType abcCodeType1 = service.getCodeType(AbcCodeType.class);

    AbcCodeType abcCodeType2 = service.reloadCodeType(AbcCodeType.class);
    assertNotSame(abcCodeType1, abcCodeType2);

    AbcCodeType abcCodeType3 = service.getCodeType(AbcCodeType.class);
    assertNotSame(abcCodeType1, abcCodeType3);
    assertSame(abcCodeType2, abcCodeType3);

    ICodeType<?, ?> r = service.reloadCodeType(null);
    assertNull(r);
  }

  /**
   * Test method for {@link org.eclipse.scout.rt.shared.services.common.code.CodeService#reloadCodeTypes(List)}.
   *
   * @throws Exception
   */
  @Test
  public void testReloadCodeTypes() throws Exception {
    ICodeService service = newCodeServiceInstance();
    service.getAllCodeTypes("");

    AbcCodeType abcCodeType1 = service.getCodeType(AbcCodeType.class);
    ZyxCodeType zyxCodeType1 = service.getCodeType(ZyxCodeType.class);

    List<Class<? extends ICodeType<?, ?>>> types1 = new ArrayList<Class<? extends ICodeType<?, ?>>>();
    types1.add(AbcCodeType.class);
    types1.add(ZyxCodeType.class);
    List<ICodeType<?, ?>> codeTypes1 = service.reloadCodeTypes(types1);

    assertTrue(abcCodeType1 instanceof AbcCodeType);
    assertTrue(codeTypes1.get(0) instanceof AbcCodeType);
    assertNotSame(abcCodeType1, codeTypes1.get(0));

    assertTrue(zyxCodeType1 instanceof ZyxCodeType);
    assertTrue(codeTypes1.get(1) instanceof ZyxCodeType);
    assertNotSame(zyxCodeType1, codeTypes1.get(1));

    List<ICodeType<?, ?>> r = service.reloadCodeTypes(null);
    assertTrue(r.isEmpty());
  }

  /**
   * Test method for {@link org.eclipse.scout.rt.shared.services.common.code.CodeService#findCodeTypeById(Object)} .
   *
   * @throws Exception
   */
  @Test
  public void testFindCodeTypeById() throws Exception {
    ICodeService service = newCodeServiceInstance();

    ICodeType<Long, ?> abcCodeType1 = service.findCodeTypeById(ABC_ID);
    ICodeType<Long, ?> abcCodeType2 = service.findCodeTypeById(ABC_ID);
    ICodeType<Long, ?> zyxCodeType1 = service.findCodeTypeById(ZYX_ID);
    ICodeType<Long, ?> zyxCodeType2 = service.findCodeTypeById(ZYX_ID);

    assertTrue(abcCodeType1 instanceof AbcCodeType);
    assertTrue(abcCodeType2 instanceof AbcCodeType);
    assertTrue(zyxCodeType1 instanceof ZyxCodeType);
    assertTrue(zyxCodeType2 instanceof ZyxCodeType);
    assertSame(abcCodeType1, abcCodeType2);
    assertSame(zyxCodeType1, zyxCodeType2);

    ICodeType<Object, ?> r = service.findCodeTypeById(null);
    assertNull(r);
  }

  protected ICodeService newCodeServiceInstance() {
    ICodeService service = new P_TestCodeService();
    BeanInstanceUtil.initializeInstance(service);
    return service;
  }

  static class P_TestCodeService extends CodeService {
    private boolean m_isFirst = true;

    @Override
    public Set<Class<? extends ICodeType<?, ?>>> getAllCodeTypeClasses(String classPrefix) {
      Set<Class<? extends ICodeType<?, ?>>> result = new HashSet<>();
      result.add(AbcCodeType.class);
      if (m_isFirst) {
        result.add(ZyxCodeType.class);
        m_isFirst = false;
      }
      return result;
    }
  }

  public static class AbcCodeType extends AbstractCodeType<Long, String> {

    private static final long serialVersionUID = 1L;

    @Override
    public Long getId() {
      return ABC_ID;
    }

  }

  public static class ZyxCodeType extends AbstractCodeType<Long, String> {

    private static final long serialVersionUID = 1L;

    @Override
    public Long getId() {
      return ZYX_ID;
    }

  }

}
