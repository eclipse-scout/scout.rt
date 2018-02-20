package org.eclipse.scout.rt.client.ui.tile;

import java.util.Collections;
import java.util.List;

import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

/**
 * @since 7.1
 */
@ClassId("b9299ac7-2401-4ff5-9806-3dafbfce5d22")
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

    setTileWidget(createTileWidgetInternal());
    if (getTileWidget() == null) {
      throw new IllegalStateException("TileWidget must not be null]");
    }
  }

  /**
   * @return the class of the widget. Default is the first inner public class that extends {@link IWidget}.
   */
  protected Class<T> getConfiguredTileWidget() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    @SuppressWarnings("unchecked")
    Class<T> result = (Class<T>) ConfigurationUtility.filterClass(dca, IWidget.class);
    return result;
  }

  @SuppressWarnings("unchecked")
  protected T createTileWidgetInternal() {
    T field = null;
    Class<? extends IWidget> fieldClass = getConfiguredTileWidget();
    if (fieldClass != null) {
      field = (T) ConfigurationUtility.newInnerInstance(this, fieldClass);
    }
    return field;
  }

  @SuppressWarnings("unchecked")
  @Override
  public T getTileWidget() {
    return (T) propertySupport.getProperty(PROP_TILE_WIDGET);
  }

  public void setTileWidget(T widget) {
    propertySupport.setProperty(PROP_TILE_WIDGET, widget);
  }

  @Override
  public List<? extends IWidget> getChildren() {
    return CollectionUtility.flatten(super.getChildren(), Collections.singletonList(getTileWidget()));
  }
}
