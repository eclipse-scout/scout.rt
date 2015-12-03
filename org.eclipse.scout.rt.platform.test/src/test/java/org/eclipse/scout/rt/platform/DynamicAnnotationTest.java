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
package org.eclipse.scout.rt.platform;

import org.junit.Assert;
import org.junit.Test;

public class DynamicAnnotationTest {

  @Test
  public void testAnnotationEquality() {
    Assert.assertEquals(Bean01.class.getAnnotation(CreateImmediately.class), AnnotationFactory.createCreateImmediately());
    Assert.assertEquals(Bean01.class.getAnnotation(ApplicationScoped.class), AnnotationFactory.createApplicationScoped());
    Assert.assertEquals(Bean01.class.getAnnotation(Order.class), AnnotationFactory.createOrder(-30));
    Assert.assertNotEquals(Bean01.class.getAnnotation(Order.class), AnnotationFactory.createOrder(-20));
  }

  private static interface IBean01 {

  }

  @CreateImmediately
  @ApplicationScoped
  @Order(-30)
  private static class Bean01 implements IBean01 {

  }
}
