/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.inventory.internal.fixture;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.IgnoreBean;

@Bean
public class TestingBean {
  @Bean
  public static class S1 {
  }

  @ApplicationScoped
  protected static class S2 {
  }

  @Bean
  private static class S3 {
  }

  @Bean
  static class S4 {
  }

  //must inherit @Bean
  public static class S1Sub1 extends S1 {
  }

  @Bean
  public class M1 {
  }

  @Bean
  public interface I1 {
  }

  @Bean
  interface I2 {
  }

  @Bean
  public enum E1 {
  }

  @Bean
  public @interface A1 {
  }

  @Bean
  protected class M2 {

  }

  @Bean
  public static final class S5 {
    private S5() {
    }
  }

  @Bean
  protected static class S6 {
    protected S6() {
    }
  }

  @IgnoreBean
  public static class S6Sub1 extends S6 {

  }

  public static class S6Sub2 extends S6 {
    public S6Sub2(String arg) {

    }
  }
}
