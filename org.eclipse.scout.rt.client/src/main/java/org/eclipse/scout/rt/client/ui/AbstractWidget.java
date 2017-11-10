package org.eclipse.scout.rt.client.ui;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
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
    setCssClass(getConfiguredCssClass());
  }

  /**
   * Configures the css class(es) of this widget.
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   *
   * @return a string containing one or more classes separated by space, or null if no class should be set.
   */
  @ConfigProperty(ConfigProperty.STRING)
  @Order(55)
  protected String getConfiguredCssClass() {
    return null;
  }

  @Override
  public String getCssClass() {
    return propertySupport.getPropertyString(PROP_CSS_CLASS);
  }

  @Override
  public void setCssClass(String cssClass) {
    propertySupport.setPropertyString(PROP_CSS_CLASS, cssClass);
  }
}
