/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.inventory.internal;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.URL;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.internal.fixture.BeanAnnotatedAnnotation;
import org.eclipse.scout.rt.platform.internal.fixture.DirectlyAnnotatedBean;
import org.eclipse.scout.rt.platform.internal.fixture.IBeanAnnotatedInterface;
import org.eclipse.scout.rt.platform.internal.fixture.IIgnoreBeanAnnotatedInterface;
import org.eclipse.scout.rt.platform.internal.fixture.IgnoreBeanAnnotatedAnnotation;
import org.eclipse.scout.rt.platform.internal.fixture.IgnoreBeanBean;
import org.eclipse.scout.rt.platform.internal.fixture.IgnoreBeanOnAnnotationBean;
import org.eclipse.scout.rt.platform.internal.fixture.IgnoreBeanOnInterfaceBean;
import org.eclipse.scout.rt.platform.internal.fixture.IgnoreBeanOnSuperclassBean;
import org.eclipse.scout.rt.platform.internal.fixture.IndirectlyAnnotatedByBeanAnnotatedAnnotationBean;
import org.eclipse.scout.rt.platform.internal.fixture.IndirectlyAnnotatedByInterfaceBean;
import org.eclipse.scout.rt.platform.internal.fixture.IndirectlyAnnotatedBySuperclassBean;
import org.jboss.jandex.Indexer;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @since 5.1
 */
public class JandexClassInfoTest {

  private static JandexClassInventory s_classInventory;

  @BeforeClass
  public static void beforeClass() throws Exception {
    Indexer indexer = new Indexer();
    indexClass(indexer, BeanAnnotatedAnnotation.class);
    indexClass(indexer, DirectlyAnnotatedBean.class);
    indexClass(indexer, IgnoreBeanAnnotatedAnnotation.class);
    indexClass(indexer, IgnoreBeanBean.class);
    indexClass(indexer, IgnoreBeanOnAnnotationBean.class);
    indexClass(indexer, IgnoreBeanOnInterfaceBean.class);
    indexClass(indexer, IgnoreBeanOnSuperclassBean.class);
    indexClass(indexer, IBeanAnnotatedInterface.class);
    indexClass(indexer, IIgnoreBeanAnnotatedInterface.class);
    indexClass(indexer, IndirectlyAnnotatedByBeanAnnotatedAnnotationBean.class);
    indexClass(indexer, IndirectlyAnnotatedByInterfaceBean.class);
    indexClass(indexer, IndirectlyAnnotatedBySuperclassBean.class);
    indexClass(indexer, FixtureNoCtor.class);
    indexClass(indexer, FixturePublicEmptyCtor.class);
    indexClass(indexer, FixtureProtectedEmptyCtor.class);
    indexClass(indexer, FixturePrivateEmptyCtor.class);
    indexClass(indexer, FixturePackageEmptyCtor.class);
    indexClass(indexer, FixturePublicOneArgCtor.class);
    indexClass(indexer, FixturePublicOneNoCopyArgCtor.class);
    indexClass(indexer, FixturePublicOneArgAndPublicEmptyCtor.class);
    indexClass(indexer, FixturePublicOneArgAndPrivateEmptyCtor.class);
    s_classInventory = new JandexClassInventory(indexer.complete());
  }

  protected static void indexClass(Indexer indexer, Class<?> clazz) throws IOException {
    URL url = clazz.getResource(clazz.getSimpleName() + ".class");
    if (url == null) {
      url = new URL(clazz.getProtectionDomain().getCodeSource().getLocation(), clazz.getName().replace(".", "/") + ".class");
    }
    try (InputStream in = url.openStream()) {
      indexer.index(in);
    }
  }

  @Test
  public void testHasAnnotation() {
    assertHasAnnotation(true, BeanAnnotatedAnnotation.class, Bean.class);
    assertHasAnnotation(false, BeanAnnotatedAnnotation.class, IgnoreBean.class);

    assertHasAnnotation(true, DirectlyAnnotatedBean.class, Bean.class);
    assertHasAnnotation(false, DirectlyAnnotatedBean.class, IgnoreBean.class);

    assertHasAnnotation(false, IgnoreBeanAnnotatedAnnotation.class, Bean.class);
    assertHasAnnotation(true, IgnoreBeanAnnotatedAnnotation.class, IgnoreBean.class);

    assertHasAnnotation(true, IgnoreBeanBean.class, Bean.class);
    assertHasAnnotation(true, IgnoreBeanBean.class, IgnoreBean.class);

    assertHasAnnotation(false, IgnoreBeanOnAnnotationBean.class, Bean.class);
    assertHasAnnotation(false, IgnoreBeanOnAnnotationBean.class, IgnoreBean.class);

    assertHasAnnotation(false, IgnoreBeanOnInterfaceBean.class, Bean.class);
    assertHasAnnotation(false, IgnoreBeanOnInterfaceBean.class, IgnoreBean.class);

    assertHasAnnotation(false, IgnoreBeanOnSuperclassBean.class, Bean.class);
    assertHasAnnotation(false, IgnoreBeanOnSuperclassBean.class, IgnoreBean.class);

    assertHasAnnotation(true, IBeanAnnotatedInterface.class, Bean.class);
    assertHasAnnotation(false, IBeanAnnotatedInterface.class, IgnoreBean.class);

    assertHasAnnotation(false, IIgnoreBeanAnnotatedInterface.class, Bean.class);
    assertHasAnnotation(true, IIgnoreBeanAnnotatedInterface.class, IgnoreBean.class);

    assertHasAnnotation(false, IndirectlyAnnotatedByBeanAnnotatedAnnotationBean.class, Bean.class);
    assertHasAnnotation(false, IndirectlyAnnotatedByBeanAnnotatedAnnotationBean.class, IgnoreBean.class);

    assertHasAnnotation(true, IndirectlyAnnotatedByInterfaceBean.class, Bean.class);
    assertHasAnnotation(false, IndirectlyAnnotatedByInterfaceBean.class, IgnoreBean.class);

    assertHasAnnotation(false, IndirectlyAnnotatedBySuperclassBean.class, Bean.class);
    assertHasAnnotation(false, IndirectlyAnnotatedBySuperclassBean.class, IgnoreBean.class);
  }

  protected static void assertHasAnnotation(boolean expectedAvailable, Class<?> annotatedClass, Class<? extends Annotation> annotationClass) {
    assertEquals(expectedAvailable, s_classInventory.getClassInfo(annotatedClass).hasAnnotation(annotationClass));
  }

  @Test
  public void testHasNoArgsConstructor() {
    assertHasNoArgsConstructor(true, FixtureNoCtor.class);
    assertHasNoArgsConstructor(true, FixturePublicEmptyCtor.class);
    assertHasNoArgsConstructor(true, FixtureProtectedEmptyCtor.class);
    assertHasNoArgsConstructor(true, FixturePrivateEmptyCtor.class);
    assertHasNoArgsConstructor(true, FixturePackageEmptyCtor.class);
    assertHasNoArgsConstructor(false, FixturePublicOneArgCtor.class);
    assertHasNoArgsConstructor(false, FixturePublicOneNoCopyArgCtor.class);
    assertHasNoArgsConstructor(true, FixturePublicOneArgAndPublicEmptyCtor.class);
    assertHasNoArgsConstructor(true, FixturePublicOneArgAndPrivateEmptyCtor.class);
  }

  protected static void assertHasNoArgsConstructor(boolean expected, Class<?> c) {
    assertEquals(expected, s_classInventory.getClassInfo(c).hasNoArgsConstructor());
  }

  public static final class FixtureNoCtor {
  }

  public static final class FixturePublicEmptyCtor {
    public FixturePublicEmptyCtor() {
    }
  }

  public static final class FixtureProtectedEmptyCtor {
    protected FixtureProtectedEmptyCtor() {
    }
  }

  public static final class FixturePrivateEmptyCtor {
    private FixturePrivateEmptyCtor() {
    }
  }

  public static final class FixturePackageEmptyCtor {
    FixturePackageEmptyCtor() {
    }
  }

  public static final class FixturePublicOneArgCtor {
    public FixturePublicOneArgCtor(String s) {
    }
  }

  public static final class FixturePublicOneNoCopyArgCtor {
    public FixturePublicOneNoCopyArgCtor(int i) {
    }
  }

  public static final class FixturePublicOneArgAndPublicEmptyCtor {
    public FixturePublicOneArgAndPublicEmptyCtor(String s) {
    }

    public FixturePublicOneArgAndPublicEmptyCtor() {
    }
  }

  public static final class FixturePublicOneArgAndPrivateEmptyCtor {
    public FixturePublicOneArgAndPrivateEmptyCtor(String s) {
    }

    @SuppressWarnings("unused")
    private FixturePublicOneArgAndPrivateEmptyCtor() {
    }
  }
}
