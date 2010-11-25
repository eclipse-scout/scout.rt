package org.eclipse.scout.rt.client.ui.form.fields.documentfield;

import java.io.File;

import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;

public class AbstractDocumentField extends AbstractValueField<Object[][]> implements IDocumentField {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractDocumentField.class);

  private IDocumentFieldUIFacade m_uiFacade;

  @Override
  protected void initConfig() {
    m_uiFacade = new P_UIFacade();
    super.initConfig();
    setFile(getConfiguredFile());
    setDisplayRulers(getConfiguredDisplayRulers());
    setDisplayStatusBar(getConfiguredDisplayStatusBar());
    setAutoResizeDocument(getConfiguredAutoResizeDocument());

  }

  @ConfigProperty(ConfigProperty.OBJECT)
  @ConfigPropertyValue("null")
  protected File getConfiguredFile() {
    return null;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @ConfigPropertyValue("true")
  protected boolean getConfiguredAutoResizeDocument() {
    return true;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @ConfigPropertyValue("false")
  protected boolean getConfiguredDisplayRulers() {
    return false;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @ConfigPropertyValue("false")
  protected boolean getConfiguredDisplayStatusBar() {
    return false;
  }

  public final void insertText(String text) {
    if (isFieldChanging()) {
      Exception caller = new Exception();
      LOG.warn("Loop detection in " + getClass().getName() + " with value " + text, caller);
      return;
    }
    try {
      setFieldChanging(true);
      propertySupport.setPropertyAlwaysFire(PROP_INSERT_TEXT, text);
    }
    finally {
      setFieldChanging(false);
    }
  }

  public final void toggleRibbon() {
    propertySupport.setPropertyAlwaysFire(PROP_TOGGLE_RIBBONS, null);
  }

  public void setFile(File file) {
    propertySupport.setPropertyAlwaysFire(PROP_FILE, file);
  }

  public File getFile() {
    return (File) propertySupport.getProperty(PROP_FILE);
  }

  public void setAutoResizeDocument(boolean autoResizeDocument) {
    propertySupport.setPropertyAlwaysFire(PROP_AUTORESIZE_DOCUMENT, autoResizeDocument);
  }

  public boolean isAutoResizeDocument() {
    return propertySupport.getPropertyBool(PROP_AUTORESIZE_DOCUMENT);
  }

  public void setDisplayRulers(boolean displayRulers) {
    propertySupport.setPropertyBool(PROP_DISPLAY_RULERS, displayRulers);
  }

  public boolean isDisplayRulers() {
    return propertySupport.getPropertyBool(PROP_DISPLAY_RULERS);
  }

  public void setDisplayStatusBar(boolean displayStatusBar) {
    propertySupport.setPropertyBool(PROP_DISPLAY_STATUS_BAR, displayStatusBar);
  }

  public boolean isDisplayStatusBar() {
    return propertySupport.getPropertyBool(PROP_DISPLAY_STATUS_BAR);
  }

  private class P_UIFacade implements IDocumentFieldUIFacade {
  }
}
