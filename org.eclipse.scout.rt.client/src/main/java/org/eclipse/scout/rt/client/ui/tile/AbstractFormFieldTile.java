package org.eclipse.scout.rt.client.ui.tile;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.text.TEXTS;
import org.eclipse.scout.rt.platform.util.concurrent.FutureCancelledError;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 8.0
 */
@ClassId("824f17e6-a83f-4e5b-8915-34737a786265")
public abstract class AbstractFormFieldTile<T extends IFormField> extends AbstractWidgetTile<T> implements IFormFieldTile<T> {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractFormFieldTile.class);

  public AbstractFormFieldTile() {
    this(true);
  }

  public AbstractFormFieldTile(boolean callInitializer) {
    super(false);
    if (callInitializer) {
      callInitializer();
    }
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setDisplayStyle(getConfiguredDisplayStyle());
    initTileWidgetConfig();
  }

  @Override
  protected void handleInitException(Exception exception) {
    LOG.error("Error while initializing tile {}: {}", getTileWidget(), exception.getMessage(), exception);
    getTileWidget().addErrorStatus(TEXTS.get("ErrorWhileLoadingData"));
  }

  @Override
  protected void handleLoadDataException(Throwable e) {
    super.handleLoadDataException(e);
    if (e instanceof VetoException) {
      getTileWidget().addErrorStatus(((ProcessingException) e).getStatus());
    }
    else if (e instanceof FutureCancelledError || e instanceof ThreadInterruptedError) {
      //NOP
    }
    else {
      getTileWidget().addErrorStatus(TEXTS.get("ErrorWhileLoadingData"));
    }
  }

  protected void initTileWidgetConfig() {
    // Apply tile configuration properties
    T widget = getTileWidget();
    if (getConfiguredLabel() != null) {
      widget.setLabel(getConfiguredLabel());
    }
    if (getConfiguredLabelVisible() != null) {
      widget.setLabelVisible(getConfiguredLabelVisible());
    }

    if (DISPLAY_STYLE_DASHBOARD.equals(getDisplayStyle())) {
      // Adjust style
      widget.setLabelPosition(IFormField.LABEL_POSITION_TOP);
      widget.setMandatory(false);
      widget.setStatusVisible(false);
      // Pull up status into label, let field fill entire tile
      widget.setStatusPosition(IFormField.STATUS_POSITION_TOP);
    }
  }

  /**
   * Configures the display style of the tile.
   * <p>
   * The available styles are:
   * <ul>
   * <li>{@link IFormFieldTile#DISPLAY_STYLE_PLAIN}</li>
   * <li>{@link IFormFieldTile#DISPLAY_STYLE_DASHBOARD}</li>
   * </ul>
   * <p>
   * Subclasses can override this method. The default is {@link IFormFieldTile#DISPLAY_STYLE_DASHBOARD}.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(140)
  protected String getConfiguredDisplayStyle() {
    return DISPLAY_STYLE_DASHBOARD;
  }

  // ----- Configuration delegated to tile field: -----

  /**
   * If set, this value is applied to the tile field's "label" property.
   */
  @ConfigProperty(ConfigProperty.STRING)
  @Order(70)
  protected String getConfiguredLabel() {
    return null;
  }

  /**
   * If set, this value is applied to the tile field's "labelVisible" property.
   */
  @ConfigProperty(ConfigProperty.STRING)
  @Order(70)
  @SuppressWarnings("findbugs:NP_BOOLEAN_RETURN_NULL")
  protected Boolean getConfiguredLabelVisible() {
    return null;
  }

  @Override
  public String getDisplayStyle() {
    return propertySupport.getPropertyString(PROP_DISPLAY_STYLE);
  }

  /**
   * Calling this method after initialization won't have any effect
   */
  protected void setDisplayStyle(String style) {
    propertySupport.setPropertyString(PROP_DISPLAY_STYLE, style);
  }

  @Override
  protected void beforeLoadData() {
    getTileWidget().clearErrorStatus();
  }

  @Override
  public void onLoadDataCancel() {
    setLoading(false);
    getTileWidget().addErrorStatus(TEXTS.get("ErrorWhileLoadingData"));
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + " [m_widget=" + getTileWidget() + ", m_container=" + getContainer() + "]";
  }
}
