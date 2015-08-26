package org.eclipse.scout.rt.platform.internal.fixture;

import org.eclipse.scout.rt.platform.Bean;

@Bean
public class EmptyCtorBean {
  public EmptyCtorBean() {
  }

  public boolean test() {
    return true;
  }
}
