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
    if (isInitConfigDone()) {
      return;
    }
    initConfigInternal();
    setInitConfigDone(true);
  }

  /**
   * Will be called by {@link #callInitializer()} but only if {@link #isInitConfigDone()} returns false.
   */
  protected void initConfigInternal() {
    initConfig();
  }

  protected void initConfig() {
    setCssClass(getConfiguredCssClass());
  }

  @Override
  public boolean isInitConfigDone() {
    return propertySupport.getPropertyBool(PROP_INIT_CONFIG_DONE);
  }

  protected void setInitConfigDone(boolean initConfigDone) {
    propertySupport.setPropertyBool(PROP_INIT_CONFIG_DONE, initConfigDone);
  }

  @Override
  public void postInitConfig() {
    if (isPostInitConfigDone()) {
      return;
    }
    postInitConfigInternal();
    setPostInitConfigDone(true);
  }

  /**
   * Will be called by {@link #postInitConfig()} but only if {@link #isPostInitConfigDone()} returns false.
   */
  protected void postInitConfigInternal() {
    // NOP
  }

  @Override
  public boolean isPostInitConfigDone() {
    return propertySupport.getPropertyBool(PROP_POST_INIT_CONFIG_DONE);
  }

  protected void setPostInitConfigDone(boolean postInitConfigDone) {
    propertySupport.setPropertyBool(PROP_POST_INIT_CONFIG_DONE, postInitConfigDone);
  }

  @Override
  public void init() {
    if (isInitDone()) {
      return;
    }
    initInternal();
    setInitDone(true);
  }

  /**
   * Will be called by {@link #init()} but only if {@link #isInitDone()} returns false.
   */
  protected void initInternal() {
    // NOP
  }

  @Override
  public boolean isInitDone() {
    return propertySupport.getPropertyBool(PROP_INIT_DONE);
  }

  protected void setInitDone(boolean initDone) {
    propertySupport.setPropertyBool(PROP_INIT_DONE, initDone);
  }

  @Override
  public void reinit() {
    setInitDone(false);
    init();
  }

  @Override
  public void dispose() {
    if (isDisposeDone()) {
      return;
    }
    disposeInternal();
    setDisposeDone(true);
  }

  /**
   * Will be called by {@link #dispose()} but only if {@link #isDisposeDone()} returns false.
   */
  protected void disposeInternal() {
    // NOP
  }

  @Override
  public boolean isDisposeDone() {
    return propertySupport.getPropertyBool(PROP_DISPOSE_DONE);
  }

  protected void setDisposeDone(boolean disposeDone) {
    propertySupport.setPropertyBool(PROP_DISPOSE_DONE, disposeDone);
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
