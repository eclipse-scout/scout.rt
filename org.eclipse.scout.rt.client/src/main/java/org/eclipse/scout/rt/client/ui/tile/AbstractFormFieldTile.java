package org.eclipse.scout.rt.client.ui.tile;

import org.eclipse.scout.rt.client.ui.form.FormUtility;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
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
  protected void handleInitException(Exception exception) {
    LOG.error("Error while initializing tile {}: {}", getRefWidget(), exception.getMessage(), exception);
    getRefWidget().addErrorStatus(TEXTS.get("ErrorWhileLoadingData"));
  }

  @Override
  protected void initRefWidgetInternal() {
    super.initRefWidgetInternal();

    T refWidget = getRefWidget();
    if (refWidget instanceof ICompositeField) {
      FormUtility.initFormFields((ICompositeField) refWidget);
    }
    else {
      refWidget.initField();
    }
    // FIXME CGU tiles postInit?

    // Apply tile configuration properties
    if (getConfiguredLabel() != null) {
      refWidget.setLabel(getConfiguredLabel());
    }
    if (getConfiguredLabelVisible() != null) {
      refWidget.setLabelVisible(getConfiguredLabelVisible());
    }

    // Adjust style
    refWidget.setLabelPosition(IFormField.LABEL_POSITION_TOP);
    refWidget.setMandatory(false);
    refWidget.setStatusVisible(false);
    // Pull up status into label, let field fill entire tile
    refWidget.setStatusPosition(IFormField.STATUS_POSITION_TOP);
  }

  @Override
  protected void disposeRefWidgetInternal() {
    T refWidget = getRefWidget();
    if (refWidget instanceof ICompositeField) {
      FormUtility.initFormFields((ICompositeField) refWidget);
    }
    else {
      refWidget.disposeField();
    }
    super.disposeRefWidgetInternal();
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
}
