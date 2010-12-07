package org.eclipse.scout.rt.client.ui.form.fields.documentfield;

import java.io.File;

import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;

/**
 * The document field is an editor field that presents a document for editing.
 * <p>
 * Current known implementations inlcude the Microsoft office word document editor in swing. This will be released soon
 * as a scout swing fragment under epl.
 */
public abstract class AbstractDocumentField extends AbstractValueField<File> implements IDocumentField {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractDocumentField.class);

  private final EventListenerList m_listenerList = new EventListenerList();
  private IDocumentFieldUIFacade m_uiFacade;

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @ConfigPropertyValue("false")
  protected boolean getConfiguredRulersVisible() {
    return false;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @ConfigPropertyValue("false")
  protected boolean getConfiguredStatusBarVisible() {
    return false;
  }

  @Override
  protected void initConfig() {
    m_uiFacade = new P_UIFacade();
    super.initConfig();
    setRulersVisible(getConfiguredRulersVisible());
    setStatusBarVisible(getConfiguredStatusBarVisible());
  }

  public IDocumentFieldUIFacade getUIFacade() {
    return m_uiFacade;
  }

  public void setRulersVisible(boolean b) {
    propertySupport.setPropertyBool(PROP_RULERS_VISIBLE, b);
  }

  public boolean isRulersVisible() {
    return propertySupport.getPropertyBool(PROP_RULERS_VISIBLE);
  }

  public void setStatusBarVisible(boolean b) {
    propertySupport.setPropertyBool(PROP_STATUS_BAR_VISIBLE, b);
  }

  public boolean isStatusBarVisible() {
    return propertySupport.getPropertyBool(PROP_STATUS_BAR_VISIBLE);
  }

  public void addDocumentFieldListener(DocumentFieldListener listener) {
    m_listenerList.add(DocumentFieldListener.class, listener);
  }

  public void removeDocumentFieldListener(DocumentFieldListener listener) {
    m_listenerList.remove(DocumentFieldListener.class, listener);
  }

  // main handler
  protected void fireDocumentFieldEventInternal(DocumentFieldEvent e) {
    DocumentFieldListener[] listeners = m_listenerList.getListeners(DocumentFieldListener.class);
    if (listeners != null && listeners.length > 0) {
      for (int i = 0; i < listeners.length; i++) {
        try {
          listeners[i].documentFieldChanged(e);
        }
        catch (Throwable t) {
          LOG.error("fire " + e, t);
        }
      }
    }
  }

  public void saveAs(File file, String formatType) {
    fireDocumentFieldEventInternal(new DocumentFieldEvent(this, DocumentFieldEvent.TYPE_SAVE_AS, file, formatType));
  }

  public void insertText(String text) {
    fireDocumentFieldEventInternal(new DocumentFieldEvent(this, DocumentFieldEvent.TYPE_INSERT_TEXT, text));
  }

  public void toggleRibbons() {
    fireDocumentFieldEventInternal(new DocumentFieldEvent(this, DocumentFieldEvent.TYPE_TOGGLE_RIBBONS));
  }

  public void autoResizeDocument() {
    fireDocumentFieldEventInternal(new DocumentFieldEvent(this, DocumentFieldEvent.TYPE_AUTORESIZE_DOCUMENT));
  }

  private class P_UIFacade implements IDocumentFieldUIFacade {
  }
}
