package org.eclipse.scout.rt.client.ui.tile;

import org.eclipse.scout.rt.client.ui.form.FormUtility;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.shared.TEXTS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 7.1
 */
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
    initTileWidgetConfig();
  }

  @Override
  protected void postInitTileWidgetConfig() {
    getTileWidget().postInitConfig();
  }

  @Override
  protected void handleInitException(Exception exception) {
    LOG.error("Error while initializing tile {}: {}", getTileWidget(), exception.getMessage(), exception);
    getTileWidget().addErrorStatus(TEXTS.get("ErrorWhileLoadingData"));
  }

  @Override
  protected void handleLoadDataException(Exception e) {
    if (e instanceof VetoException) {
      LOG.info("VetoException on {}: {}", this.getClass().getName(), e.getMessage());
      getTileWidget().addErrorStatus(((ProcessingException) e).getStatus());
    }
    else {
      LOG.error("Unexpected error on {}", this.getClass().getName(), e);
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

    // Adjust style
    widget.setLabelPosition(IFormField.LABEL_POSITION_TOP);
    widget.setMandatory(false);
    widget.setStatusVisible(false);
    // Pull up status into label, let field fill entire tile
    widget.setStatusPosition(IFormField.STATUS_POSITION_TOP);
  }

  @Override
  protected void initTileWidget() {
    super.initTileWidget();

    T widget = getTileWidget();
    if (widget instanceof ICompositeField) {
      FormUtility.initFormFields((ICompositeField) widget);
    }
    else {
      widget.initField();
    }
  }

  @Override
  protected void disposeTileWidget() {
    T widget = getTileWidget();
    if (widget instanceof ICompositeField) {
      FormUtility.initFormFields((ICompositeField) widget);
    }
    else {
      widget.disposeField();
    }
    super.disposeTileWidget();
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
