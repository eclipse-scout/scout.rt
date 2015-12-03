/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
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
import java.lang.annotation.Annotation;

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
    s_classInventory = new JandexClassInventory(indexer.complete());
  }

  protected static void indexClass(Indexer indexer, Class<?> clazz) throws IOException {
    indexer.index(clazz.getResourceAsStream(clazz.getSimpleName() + ".class"));
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
}
