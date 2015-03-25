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
package org.eclipse.scout.rt.platform;

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
    s_beans.add(Platform.get().getBeanContext().registerClass(ApplicationScopedBeanA.class));
    s_beans.add(Platform.get().getBeanContext().registerClass(ApplicationScopedBeanB.class));
    s_beans.add(Platform.get().getBeanContext().registerClass(ApplicationScopedBeanC.class));
    s_beans.add(Platform.get().getBeanContext().registerClass(ApplicationScopedBeanD.class));
    s_beans.add(Platform.get().getBeanContext().registerClass(ApplicationScopedBeanE.class));
    s_beans.add(Platform.get().getBeanContext().registerClass(ApplicationScopedBeanF.class));
  }

  @AfterClass
  public static void unregisterBeans() {
    for (IBean<?> bean : s_beans) {
      Platform.get().getBeanContext().unregisterBean(bean);
    }
  }

  @Test(timeout = 500, expected = BeanCreationException.class)
  public void testApplicationScopedWithDirectCircularDependency() {
    OBJ.get(ApplicationScopedBeanA.class);
  }

  @Test(timeout = 500, expected = BeanCreationException.class)
  public void testApplicationScopedWithIndirectCircularDependencyWithFourElements() {
    OBJ.get(ApplicationScopedBeanC.class);
  }

  @Test(timeout = 500, expected = BeanCreationException.class)
  public void testDirectCircularDependency() {
    OBJ.get(BeanA.class);
  }

  @Test(timeout = 500, expected = BeanCreationException.class)
  public void testIndirectCircularDependencyWithFourElements() {
    OBJ.get(BeanC.class);
  }

  @ApplicationScoped
  public static class ApplicationScopedBeanA {
    public ApplicationScopedBeanA() {
      OBJ.get(ApplicationScopedBeanB.class);
    }
  }

  @ApplicationScoped
  public static class ApplicationScopedBeanB {
    public ApplicationScopedBeanB() {
      OBJ.get(ApplicationScopedBeanA.class);
    }
  }

  @ApplicationScoped
  public static class ApplicationScopedBeanC {
    public ApplicationScopedBeanC() {
      OBJ.get(ApplicationScopedBeanD.class);
    }
  }

  @ApplicationScoped
  public static class ApplicationScopedBeanD {
    public ApplicationScopedBeanD() {
      OBJ.get(ApplicationScopedBeanE.class);
    }
  }

  @ApplicationScoped
  public static class ApplicationScopedBeanE {
    public ApplicationScopedBeanE() {
      OBJ.get(ApplicationScopedBeanF.class);
    }
  }

  @ApplicationScoped
  public static class ApplicationScopedBeanF {
    public ApplicationScopedBeanF() {
      OBJ.get(ApplicationScopedBeanC.class);
    }
  }

  @Bean
  public static class BeanA {
    public BeanA() {
      OBJ.get(BeanB.class);
    }
  }

  @Bean
  public static class BeanB {
    public BeanB() {
      OBJ.get(BeanA.class);
    }
  }

  @Bean
  public static class BeanC {
    public BeanC() {
      OBJ.get(BeanD.class);
    }
  }

  @Bean
  public static class BeanD {
    public BeanD() {
      OBJ.get(BeanE.class);
    }
  }

  @Bean
  public static class BeanE {
    public BeanE() {
      OBJ.get(BeanF.class);
    }
  }

  @Bean
  public static class BeanF {
    public BeanF() {
      OBJ.get(BeanC.class);
    }
  }
}
