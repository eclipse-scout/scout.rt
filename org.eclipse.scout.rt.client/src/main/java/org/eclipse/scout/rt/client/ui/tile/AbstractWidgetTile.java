package org.eclipse.scout.rt.client.ui.tile;

import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;

/**
 * @since 7.1
 */
public abstract class AbstractWidgetTile<T extends IWidget> extends AbstractTile implements IWidgetTile<T> {

  public AbstractWidgetTile() {
    this(true);
  }

  public AbstractWidgetTile(boolean callInitializer) {
    super(false);
    if (callInitializer) {
      callInitializer();
    }
  }

  @Override
  protected void initConfig() {
    super.initConfig();

    // Create instance of the widget (but don't initialize it here, because link to tile container box is not established yet)
    setRefWidget(createRefWidgetInternal());
  }

  /**
   * @return the class of the widget. Default is the first inner public class that extends {@link IWidget}.
   */
  protected Class<T> getConfiguredRefWidget() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    @SuppressWarnings("unchecked")
    Class<T> result = (Class<T>) ConfigurationUtility.filterClass(dca, IWidget.class);
    return result;
  }

  @SuppressWarnings("unchecked")
  protected T createRefWidgetInternal() {
    T field = null;
    Class<? extends IWidget> fieldClass = getConfiguredRefWidget();
    if (fieldClass != null) {
      field = (T) ConfigurationUtility.newInnerInstance(this, fieldClass);
    }
    return field;
  }

  @SuppressWarnings("unchecked")
  @Override
  public T getRefWidget() {
    return (T) propertySupport.getProperty(PROP_REF_WIDGET);
  }

  public void setRefWidget(T widget) {
    propertySupport.setProperty(PROP_REF_WIDGET, widget);
  }

  @Override
  public void initInternal() {
    initRefWidget();
  }

  protected void initRefWidget() {
    initRefWidgetInternal();
    execInitRefWidget();
  }

  protected void initRefWidgetInternal() {
    // nop
  }

  protected void execInitRefWidget() {
  }

  @Override
  protected void disposeInternal() {
    disposeRefWidget();
  }

  protected void disposeRefWidget() {
    disposeRefWidgetInternal();
    execDisposeRefWidget();
  }

  protected void disposeRefWidgetInternal() {
    // nop
  }

  protected void execDisposeRefWidget() {
  }

}
