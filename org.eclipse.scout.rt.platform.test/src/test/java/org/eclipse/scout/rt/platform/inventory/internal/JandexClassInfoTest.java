/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
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
import org.eclipse.scout.rt.platform.SuppressBean;
import org.eclipse.scout.rt.platform.internal.fixture.BeanAnnotatedAnnotation;
import org.eclipse.scout.rt.platform.internal.fixture.DirectlyAnnotatedBean;
import org.eclipse.scout.rt.platform.internal.fixture.IBeanAnnotatedInterface;
import org.eclipse.scout.rt.platform.internal.fixture.ISuppressBeanAnnotatedInterface;
import org.eclipse.scout.rt.platform.internal.fixture.IndirectlyAnnotatedByBeanAnnotatedAnnotationBean;
import org.eclipse.scout.rt.platform.internal.fixture.IndirectlyAnnotatedByInterfaceBean;
import org.eclipse.scout.rt.platform.internal.fixture.IndirectlyAnnotatedBySuperclassBean;
import org.eclipse.scout.rt.platform.internal.fixture.SuppressBeanAnnotatedAnnotation;
import org.eclipse.scout.rt.platform.internal.fixture.SuppressBeanBean;
import org.eclipse.scout.rt.platform.internal.fixture.SuppressBeanOnAnnotationBean;
import org.eclipse.scout.rt.platform.internal.fixture.SuppressBeanOnInterfaceBean;
import org.eclipse.scout.rt.platform.internal.fixture.SuppressBeanOnSuperclassBean;
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
    indexClass(indexer, SuppressBeanAnnotatedAnnotation.class);
    indexClass(indexer, SuppressBeanBean.class);
    indexClass(indexer, SuppressBeanOnAnnotationBean.class);
    indexClass(indexer, SuppressBeanOnInterfaceBean.class);
    indexClass(indexer, SuppressBeanOnSuperclassBean.class);
    indexClass(indexer, IBeanAnnotatedInterface.class);
    indexClass(indexer, ISuppressBeanAnnotatedInterface.class);
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
    assertHasAnnotation(false, BeanAnnotatedAnnotation.class, SuppressBean.class);

    assertHasAnnotation(true, DirectlyAnnotatedBean.class, Bean.class);
    assertHasAnnotation(false, DirectlyAnnotatedBean.class, SuppressBean.class);

    assertHasAnnotation(false, SuppressBeanAnnotatedAnnotation.class, Bean.class);
    assertHasAnnotation(true, SuppressBeanAnnotatedAnnotation.class, SuppressBean.class);

    assertHasAnnotation(true, SuppressBeanBean.class, Bean.class);
    assertHasAnnotation(true, SuppressBeanBean.class, SuppressBean.class);

    assertHasAnnotation(false, SuppressBeanOnAnnotationBean.class, Bean.class);
    assertHasAnnotation(false, SuppressBeanOnAnnotationBean.class, SuppressBean.class);

    assertHasAnnotation(false, SuppressBeanOnInterfaceBean.class, Bean.class);
    assertHasAnnotation(false, SuppressBeanOnInterfaceBean.class, SuppressBean.class);

    assertHasAnnotation(false, SuppressBeanOnSuperclassBean.class, Bean.class);
    assertHasAnnotation(false, SuppressBeanOnSuperclassBean.class, SuppressBean.class);

    assertHasAnnotation(true, IBeanAnnotatedInterface.class, Bean.class);
    assertHasAnnotation(false, IBeanAnnotatedInterface.class, SuppressBean.class);

    assertHasAnnotation(false, ISuppressBeanAnnotatedInterface.class, Bean.class);
    assertHasAnnotation(true, ISuppressBeanAnnotatedInterface.class, SuppressBean.class);

    assertHasAnnotation(false, IndirectlyAnnotatedByBeanAnnotatedAnnotationBean.class, Bean.class);
    assertHasAnnotation(false, IndirectlyAnnotatedByBeanAnnotatedAnnotationBean.class, SuppressBean.class);

    assertHasAnnotation(true, IndirectlyAnnotatedByInterfaceBean.class, Bean.class);
    assertHasAnnotation(false, IndirectlyAnnotatedByInterfaceBean.class, SuppressBean.class);

    assertHasAnnotation(false, IndirectlyAnnotatedBySuperclassBean.class, Bean.class);
    assertHasAnnotation(false, IndirectlyAnnotatedBySuperclassBean.class, SuppressBean.class);
  }

  protected static void assertHasAnnotation(boolean expectedAvailable, Class<?> annotatedClass, Class<? extends Annotation> annotationClass) {
    assertEquals(expectedAvailable, s_classInventory.getClassInfo(annotatedClass).hasAnnotation(annotationClass));
  }
}
