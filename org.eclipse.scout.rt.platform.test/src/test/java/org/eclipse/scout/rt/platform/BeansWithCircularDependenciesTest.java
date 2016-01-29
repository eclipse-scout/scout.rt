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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.rt.platform.exception.BeanCreationException;
import org.eclipse.scout.rt.platform.internal.DefaultBeanInstanceProducer;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link DefaultBeanInstanceProducer} to make sure errors are thrown, if there are circular dependencies.
 */
@RunWith(PlatformTestRunner.class)
public class BeansWithCircularDependenciesTest {

  private static Set<IBean<?>> s_beans;

  @BeforeClass
  public static void registerBeans() {
    s_beans = new HashSet<IBean<?>>();
    s_beans.add(Platform.get().getBeanManager().registerClass(ApplicationScopedBeanA.class));
    s_beans.add(Platform.get().getBeanManager().registerClass(ApplicationScopedBeanB.class));
    s_beans.add(Platform.get().getBeanManager().registerClass(ApplicationScopedBeanC.class));
    s_beans.add(Platform.get().getBeanManager().registerClass(ApplicationScopedBeanD.class));
    s_beans.add(Platform.get().getBeanManager().registerClass(ApplicationScopedBeanE.class));
    s_beans.add(Platform.get().getBeanManager().registerClass(ApplicationScopedBeanF.class));
  }

  @AfterClass
  public static void unregisterBeans() {
    for (IBean<?> bean : s_beans) {
      Platform.get().getBeanManager().unregisterBean(bean);
    }
  }

  @Test(timeout = 500, expected = BeanCreationException.class)
  public void testApplicationScopedWithDirectCircularDependency() {
    BEANS.get(ApplicationScopedBeanA.class);
  }

  @Test(timeout = 500, expected = BeanCreationException.class)
  public void testApplicationScopedWithIndirectCircularDependencyWithFourElements() {
    BEANS.get(ApplicationScopedBeanC.class);
  }

  @Test(timeout = 500, expected = BeanCreationException.class)
  public void testDirectCircularDependency() {
    BEANS.get(BeanA.class);
  }

  @Test(timeout = 500, expected = BeanCreationException.class)
  public void testIndirectCircularDependencyWithFourElements() {
    BEANS.get(BeanC.class);
  }

  @ApplicationScoped
  public static class ApplicationScopedBeanA {
    public ApplicationScopedBeanA() {
      BEANS.get(ApplicationScopedBeanB.class);
    }
  }

  @ApplicationScoped
  public static class ApplicationScopedBeanB {
    public ApplicationScopedBeanB() {
      BEANS.get(ApplicationScopedBeanA.class);
    }
  }

  @ApplicationScoped
  public static class ApplicationScopedBeanC {
    public ApplicationScopedBeanC() {
      BEANS.get(ApplicationScopedBeanD.class);
    }
  }

  @ApplicationScoped
  public static class ApplicationScopedBeanD {
    public ApplicationScopedBeanD() {
      BEANS.get(ApplicationScopedBeanE.class);
    }
  }

  @ApplicationScoped
  public static class ApplicationScopedBeanE {
    public ApplicationScopedBeanE() {
      BEANS.get(ApplicationScopedBeanF.class);
    }
  }

  @ApplicationScoped
  public static class ApplicationScopedBeanF {
    public ApplicationScopedBeanF() {
      BEANS.get(ApplicationScopedBeanC.class);
    }
  }

  @Bean
  public static class BeanA {
    public BeanA() {
      BEANS.get(BeanB.class);
    }
  }

  @Bean
  public static class BeanB {
    public BeanB() {
      BEANS.get(BeanA.class);
    }
  }

  @Bean
  public static class BeanC {
    public BeanC() {
      BEANS.get(BeanD.class);
    }
  }

  @Bean
  public static class BeanD {
    public BeanD() {
      BEANS.get(BeanE.class);
    }
  }

  @Bean
  public static class BeanE {
    public BeanE() {
      BEANS.get(BeanF.class);
    }
  }

  @Bean
  public static class BeanF {
    public BeanF() {
      BEANS.get(BeanC.class);
    }
  }
}
