package org.eclipse.scout.rt.client.ui.tile;

import org.eclipse.scout.rt.client.ui.AbstractWidget;
import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.platform.IOrdered;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;

/**
 * @since 7.1
 */
public abstract class AbstractTile extends AbstractWidget implements ITile {

  public AbstractTile() {
    this(true);
  }

  public AbstractTile(boolean callInitializer) {
    super();
    if (callInitializer) {
      callInitializer();
    }
  }

  @Override
  protected void callInitializer() {
    initConfig();
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setOrder(calculateViewOrder());
    setColorScheme(getConfiguredColorScheme());
    setGridDataHints(new GridData(getConfiguredGridX(), getConfiguredGridY(), getConfiguredGridW(), getConfiguredGridH(), -1, -1, false, false, -1, -1, true, true, 0, 0));
  }

  /**
   * Calculates the tiles's view order, e.g. if the @Order annotation is set to 30.0, the method will return 30.0. If no
   * {@link Order} annotation is set, the method checks its super classes for an @Order annotation.
   *
   * @since 3.10.0-M4
   */
  @SuppressWarnings("squid:S1244")
  protected double calculateViewOrder() {
    double viewOrder = getConfiguredViewOrder();
    Class<?> cls = getClass();
    if (viewOrder == IOrdered.DEFAULT_ORDER) {
      while (cls != null && ITile.class.isAssignableFrom(cls)) {
        if (cls.isAnnotationPresent(Order.class)) {
          Order order = (Order) cls.getAnnotation(Order.class);
          return order.value();
        }
        cls = cls.getSuperclass();
      }
    }
    return viewOrder;
  }

  /**
   * Configures the view order of this tile. The view order determines the order in which the tiles appear in the tile
   * box. The order of tiles with no view order configured ({@code < 0}) is initialized based on the {@link Order}
   * annotation of the tile class.
   * <p>
   * Subclasses can override this method. The default is {@link IOrdered#DEFAULT_ORDER}.
   *
   * @return View order of this tile.
   */
  @ConfigProperty(ConfigProperty.DOUBLE)
  @Order(80)
  protected double getConfiguredViewOrder() {
    return IOrdered.DEFAULT_ORDER;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(10)
  protected ITileColorScheme getConfiguredColorScheme() {
    return TileColorScheme.DEFAULT;
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(20)
  protected int getConfiguredGridW() {
    return 1;
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(30)
  protected int getConfiguredGridH() {
    return 1;
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(40)
  protected int getConfiguredGridX() {
    return -1;
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(50)
  protected int getConfiguredGridY() {
    return -1;
  }

  @Override
  public double getOrder() {
    return propertySupport.getPropertyDouble(PROP_ORDER);
  }

  @Override
  public void setOrder(double order) {
    propertySupport.setPropertyDouble(PROP_ORDER, order);
  }

  @Override
  public GridData getGridDataHints() {
    return new GridData((GridData) propertySupport.getProperty(PROP_GRID_DATA_HINTS));
  }

  @Override
  public void setGridDataHints(GridData hints) {
    propertySupport.setProperty(PROP_GRID_DATA_HINTS, new GridData(hints));
  }

  @Override
  public ITileColorScheme getColorScheme() {
    return (ITileColorScheme) propertySupport.getProperty(PROP_COLOR_SCHEME);
  }

  @Override
  public void setColorScheme(ITileColorScheme colorScheme) {
    propertySupport.setProperty(PROP_COLOR_SCHEME, colorScheme);
  }
}
