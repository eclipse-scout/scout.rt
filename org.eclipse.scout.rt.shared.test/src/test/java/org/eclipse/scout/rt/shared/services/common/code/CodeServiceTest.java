/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.services.common.code;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.platform.cache.ICacheBuilder;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.internal.BeanInstanceUtil;
import org.eclipse.scout.rt.platform.text.TEXTS;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.services.common.code.fixture.TestCodeType3;
import org.eclipse.scout.rt.shared.services.common.code.fixture.TestCodeType4;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.hamcrest.MatcherAssert;
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
   * Test method for {@link CodeService#getAllCodeTypeClasses()} .
   */
  @Test
  public void testGetAllCodeTypeClasses() {
    ICodeService service = newCodeServiceInstance();
    Collection<Class<? extends ICodeType<?, ?>>> codeTypeClasses1 = service.getAllCodeTypeClasses();
    assertEquals("codeTypeClasses1 size", 2, codeTypeClasses1.size());
    assertTrue("codeTypeClasses1 contains AbcCodeType", codeTypeClasses1.contains(AbcCodeType.class));
    assertTrue("codeTypeClasses1 contains ZyxCodeType", codeTypeClasses1.contains(ZyxCodeType.class));

    Collection<Class<? extends ICodeType<?, ?>>> codeTypeClasses2 = service.getAllCodeTypeClasses();
    assertEquals("codeTypeClasses2 size", 1, codeTypeClasses2.size());
    assertTrue("codeTypeClasses2 contains AbcCodeType", codeTypeClasses2.contains(AbcCodeType.class));
    assertFalse("codeTypeClasses2 contains ZyxCodeType", codeTypeClasses2.contains(ZyxCodeType.class));
  }

  /**
   * Test method for {@link CodeService#getAllCodeTypes()} .
   */
  @Test
  public void testGetAllCodeTypesString() {
    ICodeService service = newCodeServiceInstance();
    Collection<ICodeType<?, ?>> codeTypes1 = service.getAllCodeTypes();
    assertEquals("size", 2, codeTypes1.size());
    Set<Class<?>> codeTypeClasses = new HashSet<>(2);
    for (ICodeType<?, ?> ct : codeTypes1) {
      codeTypeClasses.add(ct.getClass());
    }
    assertTrue(codeTypeClasses.contains(AbcCodeType.class));
    assertTrue(codeTypeClasses.contains(ZyxCodeType.class));

    Collection<ICodeType<?, ?>> codeTypes2 = service.getAllCodeTypes();
    assertEquals("size", 1, codeTypes2.size());
    assertEquals("codeType 2 (0)", AbcCodeType.class, CollectionUtility.firstElement(codeTypes2).getClass());
  }

  /**
   * Test method for {@link org.eclipse.scout.rt.shared.services.common.code.CodeService#getCodeType(java.lang.Class)}.
   */
  @Test
  public void testGetCodeTypeClass() {
    ICodeService service = newCodeServiceInstance();
    service.getAllCodeTypes();

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
    service.getAllCodeTypes();

    List<Class<? extends ICodeType<?, ?>>> types1 = new ArrayList<>();
    types1.add(AbcCodeType.class);
    types1.add(ZyxCodeType.class);
    List<ICodeType<?, ?>> codeTypes1 = service.getCodeTypes(types1);
    assertEquals("codeTypes1 size", 2, codeTypes1.size());

    List<Class<? extends ICodeType<?, ?>>> types2 = new ArrayList<>();
    types2.add(AbcCodeType.class);
    List<ICodeType<?, ?>> codeTypes2 = service.getCodeTypes(types2);
    assertEquals("codeTypes2 size", 1, codeTypes2.size());

    List<Class<? extends ICodeType<?, ?>>> types3 = new ArrayList<>();
    types3.add(ZyxCodeType.class);
    List<ICodeType<?, ?>> codeTypes3 = service.getCodeTypes(types3);
    assertEquals("codeTypes2 size", 1, codeTypes3.size());

    assertSame(codeTypes1.get(0), codeTypes2.get(0));
    assertSame(codeTypes1.get(1), codeTypes3.get(0));

    List<Class<? extends ICodeType<?, ?>>> types4 = new ArrayList<>();
    types4.add(ZyxCodeType.class);
    types4.add(AbcCodeType.class);
    List<ICodeType<?, ?>> codeTypes4 = service.getCodeTypes(types4);
    assertEquals("codeTypes4 size", 2, codeTypes4.size());

    assertSame(codeTypes1.get(0), codeTypes4.get(1));
    assertSame(codeTypes1.get(1), codeTypes4.get(0));
  }

  /**
   * Test method for {@link org.eclipse.scout.rt.shared.services.common.code.CodeService#reloadCodeType(Class)}.
   */
  @Test
  public void testReloadCodeType() {
    ICodeService service = newCodeServiceInstance();
    service.getAllCodeTypes();

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
   */
  @Test
  public void testReloadCodeTypes() {
    ICodeService service = newCodeServiceInstance();
    service.getAllCodeTypes();

    AbcCodeType abcCodeType1 = service.getCodeType(AbcCodeType.class);
    ZyxCodeType zyxCodeType1 = service.getCodeType(ZyxCodeType.class);

    List<Class<? extends ICodeType<?, ?>>> types1 = new ArrayList<>();
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
   */
  @Test
  public void testFindCodeTypeById() {
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

  @Test
  public void testReplaceLookupUsingCodeService() {
    ICodeService service = newCodeServiceInstance();

    MatcherAssert.assertThat(service.getCodeType(TestCodeType3.class), instanceOf(TestCodeType4.class));
    MatcherAssert.assertThat(service.getCodeType(TestCodeType4.class), instanceOf(TestCodeType4.class));
  }

  @Test
  public void testReplaceCodeServiceGetCodeTypes() {
    ICodeService service = newCodeServiceInstance();

    List<ICodeType<?, ?>> codeTypes = service.getCodeTypes(Arrays.asList(TestCodeType3.class, TestCodeType4.class));
    assertEquals(2, codeTypes.size());
    MatcherAssert.assertThat(codeTypes.get(0), instanceOf(TestCodeType4.class));
    MatcherAssert.assertThat(codeTypes.get(1), instanceOf(TestCodeType4.class));
  }

  @Test
  public void testReplaceCodeServiceGetCodeTypeMap() {
    ICodeService service = newCodeServiceInstance();

    Map<Class<? extends ICodeType<?, ?>>, ICodeType<?, ?>> codeTypes = service.getCodeTypeMap(Arrays.asList(TestCodeType3.class, TestCodeType4.class));
    assertEquals(2, codeTypes.size());
    MatcherAssert.assertThat(codeTypes.get(TestCodeType3.class), instanceOf(TestCodeType4.class));
    MatcherAssert.assertThat(codeTypes.get(TestCodeType4.class), instanceOf(TestCodeType4.class));
  }

  @Test
  public void testResolveCodeForDifferentLocales() {
    var cacheValueResolver = ((P_TestCodeService) newCodeServiceInstance()).createCacheValueResolver();

    RunContexts.copyCurrent()
        .withLocale(Locale.ENGLISH)
        .run(() -> {
          assertEquals(Map.of("day", "Day", "month", "Month"), getCodeIdTextMap(cacheValueResolver.resolve(new CodeTypeCacheKey(Locale.ENGLISH, ZyxCodeType.class))));
          assertEquals(Map.of("day", "Tag", "month", "Monat"), getCodeIdTextMap(cacheValueResolver.resolve(new CodeTypeCacheKey(Locale.GERMAN, ZyxCodeType.class))));
        });

    RunContexts.copyCurrent()
        .withLocale(Locale.GERMAN)
        .run(() -> {
          assertEquals(Map.of("day", "Day", "month", "Month"), getCodeIdTextMap(cacheValueResolver.resolve(new CodeTypeCacheKey(Locale.ENGLISH, ZyxCodeType.class))));
          assertEquals(Map.of("day", "Tag", "month", "Monat"), getCodeIdTextMap(cacheValueResolver.resolve(new CodeTypeCacheKey(Locale.GERMAN, ZyxCodeType.class))));
        });

    RunContexts.copyCurrent()
        .withLocale(Locale.FRENCH)
        .run(() -> {
          assertEquals(Map.of("day", "Day", "month", "Month"), getCodeIdTextMap(cacheValueResolver.resolve(new CodeTypeCacheKey(Locale.ENGLISH, ZyxCodeType.class))));
          assertEquals(Map.of("day", "Tag", "month", "Monat"), getCodeIdTextMap(cacheValueResolver.resolve(new CodeTypeCacheKey(Locale.GERMAN, ZyxCodeType.class))));
        });
  }

  protected <T> Map<T, String> getCodeIdTextMap(ICodeType<?, T> codeType) {
    return codeType.getCodes().stream()
        .collect(Collectors.toMap(ICode::getId, ICode::getText));
  }

  protected ICodeService newCodeServiceInstance() {
    ICodeService service = new P_TestCodeService();
    BeanInstanceUtil.initializeBeanInstance(service);
    return service;
  }

  static class P_TestCodeService extends CodeService {
    private boolean m_isFirst = true;

    @Override
    public Set<Class<? extends ICodeType<?, ?>>> getAllCodeTypeClasses() {
      Set<Class<? extends ICodeType<?, ?>>> result = new HashSet<>();
      result.add(AbcCodeType.class);
      if (m_isFirst) {
        result.add(ZyxCodeType.class);
        m_isFirst = false;
      }
      return result;
    }

    @Override
    protected ICacheBuilder<CodeTypeCacheKey, ICodeType<?, ?>> createCacheBuilder() {
      return super.createCacheBuilder()
          .withCacheId(CODE_SERVICE_CACHE_ID + ".for.test")
          .withReplaceIfExists(true);
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

    public static class DayCode extends AbstractCode<String> {
      private static final long serialVersionUID = 1L;
      public static final String ID = "day";

      @Override
      protected String getConfiguredText() {
        return TEXTS.get("Day");
      }

      @Override
      public String getId() {
        return ID;
      }
    }

    public static class MonthCode extends AbstractCode<String> {
      private static final long serialVersionUID = 1L;
      public static final String ID = "month";

      @Override
      protected String getConfiguredText() {
        return TEXTS.get("Month");
      }

      @Override
      public String getId() {
        return ID;
      }
    }
  }
}
