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

import javax.annotation.PostConstruct;

import org.eclipse.scout.rt.platform.exception.BeanCreationException;
import org.eclipse.scout.rt.platform.internal.DefaultBeanInstanceProducer;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link DefaultBeanInstanceProducer} to make sure errors are thrown, if there are circular dependencies
 * (regardless of the circular dependency is created by constructor or {@link PostConstruct} annotated method
 * invocations).
 */
@RunWith(PlatformTestRunner.class)
public class BeansWithCircularDependenciesTest {

  @Test(timeout = 500, expected = BeanCreationException.class)
  public void testApplicationScopedWithDirectCircularDependency() {
    BEANS.get(ApplicationScopedBeanA.class);
  }

  @Test(timeout = 500, expected = BeanCreationException.class)
  public void testApplicationScopedWithDirectCircularDependencyPostConstructToConstructor() {
    BEANS.get(ApplicationScopedBeanADash.class);
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
  public void testDirectCircularDependencyPostConstructToConstructor() {
    BEANS.get(BeanADash.class);
  }

  @Test(timeout = 500, expected = BeanCreationException.class)
  public void testIndirectCircularDependencyWithFourElements() {
    BEANS.get(BeanC.class);
  }

  @Test(timeout = 500, expected = BeanCreationException.class)
  public void testApplicationScopedPostConstructedWithDirectCircularDependency() {
    BEANS.get(ApplicationScopedBeanWithPostConstructA.class);
  }

  @Test(timeout = 500, expected = BeanCreationException.class)
  public void testApplicationScopedPostConstructedWithIndirectCircularDependencyWithFourElements() {
    BEANS.get(ApplicationScopedBeanWithPostConstructC.class);
  }

  @Test(timeout = 500, expected = BeanCreationException.class)
  public void testPostConstructedDirectCircularDependency() {
    BEANS.get(BeanWithPostConstructA.class);
  }

  @Test(timeout = 500, expected = BeanCreationException.class)
  public void testPostConstructedIndirectCircularDependencyWithFourElements() {
    BEANS.get(BeanWithPostConstructC.class);
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
  public static class ApplicationScopedBeanADash {
    @PostConstruct
    private void postConstruct() {
      BEANS.get(ApplicationScopedBeanBDash.class);
    }
  }

  @ApplicationScoped
  public static class ApplicationScopedBeanBDash {
    public ApplicationScopedBeanBDash() {
      BEANS.get(ApplicationScopedBeanADash.class);
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

  @ApplicationScoped
  public static class ApplicationScopedBeanWithPostConstructA {
    @PostConstruct
    private void postConstruct() {
      BEANS.get(ApplicationScopedBeanWithPostConstructB.class);
    }
  }

  @ApplicationScoped
  public static class ApplicationScopedBeanWithPostConstructB {
    @PostConstruct
    private void postConstruct() {
      BEANS.get(ApplicationScopedBeanWithPostConstructA.class);
    }
  }

  @ApplicationScoped
  public static class ApplicationScopedBeanWithPostConstructC {
    @PostConstruct
    private void postConstruct() {
      BEANS.get(ApplicationScopedBeanWithPostConstructD.class);
    }
  }

  @ApplicationScoped
  public static class ApplicationScopedBeanWithPostConstructD {
    @PostConstruct
    private void postConstruct() {
      BEANS.get(ApplicationScopedBeanWithPostConstructE.class);
    }
  }

  @ApplicationScoped
  public static class ApplicationScopedBeanWithPostConstructE {
    @PostConstruct
    private void postConstruct() {
      BEANS.get(ApplicationScopedBeanWithPostConstructF.class);
    }
  }

  @ApplicationScoped
  public static class ApplicationScopedBeanWithPostConstructF {
    @PostConstruct
    private void postConstruct() {
      BEANS.get(ApplicationScopedBeanWithPostConstructC.class);
    }
  }

  @Bean
  public static class BeanA {
    public BeanA() {
      BEANS.get(BeanB.class);
    }
  }

  @Bean
  public static class BeanADash {
    @PostConstruct
    private void postConstruct() {
      BEANS.get(BeanBDash.class);
    }
  }

  @Bean
  public static class BeanBDash {
    public BeanBDash() {
      BEANS.get(BeanADash.class);
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

  @Bean
  public static class BeanWithPostConstructA {
    @PostConstruct
    private void postConstruct() {
      BEANS.get(BeanWithPostConstructB.class);
    }
  }

  @Bean
  public static class BeanWithPostConstructB {
    @PostConstruct
    private void postConstruct() {
      BEANS.get(BeanWithPostConstructA.class);
    }
  }

  @Bean
  public static class BeanWithPostConstructC {
    @PostConstruct
    private void postConstruct() {
      BEANS.get(BeanWithPostConstructD.class);
    }
  }

  @Bean
  public static class BeanWithPostConstructD {
    @PostConstruct
    private void postConstruct() {
      BEANS.get(BeanWithPostConstructE.class);
    }
  }

  @Bean
  public static class BeanWithPostConstructE {
    @PostConstruct
    private void postConstruct() {
      BEANS.get(BeanWithPostConstructF.class);
    }
  }

  @Bean
  public static class BeanWithPostConstructF {
    @PostConstruct
    private void postConstruct() {
      BEANS.get(BeanWithPostConstructC.class);
    }
  }
}
