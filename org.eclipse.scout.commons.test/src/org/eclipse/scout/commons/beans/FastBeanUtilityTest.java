/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons.beans;

import java.lang.reflect.Method;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

/**
 * JUnit tests for {@link FastBeanUtility}
 * 
 * @since 3.9.0
 */
public class FastBeanUtilityTest {

  public static class MyBean {
    public Long getId() {
      return null;
    }

    private void myMethod() {
    }

    public void setId(Long id) {
    }
  }

  /**
   * Test for Bug 400240
   */
  @Test
  public void testDeclaredPublicMethods() {
    Method[] methods = FastBeanUtility.getDeclaredPublicMethods(MyBean.class);
    Assert.assertEquals("length", 2, methods.length);

    ArrayList<String> methodNames = new ArrayList<String>();
    for (Method method : methods) {
      methodNames.add(method.getName());
    }
    Assert.assertEquals("contains getId()", true, methodNames.contains("getId"));
    Assert.assertEquals("contains setId()", true, methodNames.contains("setId"));
  }
}
