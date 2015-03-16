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
package org.eclipse.scout.rt.platform.cdi;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class BeansWithCircularDependenciesTest {

  private static Set<IBean<?>> s_beans;

  @BeforeClass
  public static void registerBeans() {
    s_beans = new HashSet<IBean<?>>();
    s_beans.add(OBJ.registerClass(ApplicationScopedBeanA.class));
    s_beans.add(OBJ.registerClass(ApplicationScopedBeanB.class));
    s_beans.add(OBJ.registerClass(ApplicationScopedBeanC.class));
    s_beans.add(OBJ.registerClass(ApplicationScopedBeanD.class));
    s_beans.add(OBJ.registerClass(ApplicationScopedBeanE.class));
    s_beans.add(OBJ.registerClass(ApplicationScopedBeanF.class));
  }

  @AfterClass
  public static void unregisterBeans() {
    for (IBean<?> bean : s_beans) {
      OBJ.unregisterBean(bean);
    }
  }

  @Test(timeout = 500, expected = BeanCreationException.class)
  public void testApplicationScopedWithDirectCircularDependency() {
    OBJ.one(ApplicationScopedBeanA.class);
  }

  @Test(timeout = 500, expected = BeanCreationException.class)
  public void testApplicationScopedWithIndirectCircularDependencyWithFourElements() {
    OBJ.one(ApplicationScopedBeanC.class);
  }

  @Test(timeout = 500, expected = BeanCreationException.class)
  public void testDirectCircularDependency() {
    OBJ.one(BeanA.class);
  }

  @Test(timeout = 500, expected = BeanCreationException.class)
  public void testIndirectCircularDependencyWithFourElements() {
    OBJ.one(BeanC.class);
  }

  @ApplicationScoped
  public static class ApplicationScopedBeanA {
    public ApplicationScopedBeanA() {
      OBJ.one(ApplicationScopedBeanB.class);
    }
  }

  @ApplicationScoped
  public static class ApplicationScopedBeanB {
    public ApplicationScopedBeanB() {
      OBJ.one(ApplicationScopedBeanA.class);
    }
  }

  @ApplicationScoped
  public static class ApplicationScopedBeanC {
    public ApplicationScopedBeanC() {
      OBJ.one(ApplicationScopedBeanD.class);
    }
  }

  @ApplicationScoped
  public static class ApplicationScopedBeanD {
    public ApplicationScopedBeanD() {
      OBJ.one(ApplicationScopedBeanE.class);
    }
  }

  @ApplicationScoped
  public static class ApplicationScopedBeanE {
    public ApplicationScopedBeanE() {
      OBJ.one(ApplicationScopedBeanF.class);
    }
  }

  @ApplicationScoped
  public static class ApplicationScopedBeanF {
    public ApplicationScopedBeanF() {
      OBJ.one(ApplicationScopedBeanC.class);
    }
  }

  @Bean
  public static class BeanA {
    public BeanA() {
      OBJ.one(BeanB.class);
    }
  }

  @Bean
  public static class BeanB {
    public BeanB() {
      OBJ.one(BeanA.class);
    }
  }

  @Bean
  public static class BeanC {
    public BeanC() {
      OBJ.one(BeanD.class);
    }
  }

  @Bean
  public static class BeanD {
    public BeanD() {
      OBJ.one(BeanE.class);
    }
  }

  @Bean
  public static class BeanE {
    public BeanE() {
      OBJ.one(BeanF.class);
    }
  }

  @Bean
  public static class BeanF {
    public BeanF() {
      OBJ.one(BeanC.class);
    }
  }
}
