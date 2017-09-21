package org.eclipse.scout.rt.client.ui;

import org.eclipse.scout.rt.platform.reflect.AbstractPropertyObserver;

/**
 * @since 7.1
 */
public abstract class AbstractWidget extends AbstractPropertyObserver implements IWidget {

  public AbstractWidget() {
    this(true);
  }

  public AbstractWidget(boolean callInitializer) {
    if (callInitializer) {
      callInitializer();
    }
  }

  protected void callInitializer() {
    initConfig();
  }

  protected void initConfig() {
    // nop
  }
}
