package org.eclipse.scout.rt.client.ui.form.fields.documentfield;

import java.io.File;

import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.shared.services.common.file.RemoteFile;

/**
 * The document field is an editor field that presents a document for editing.
 * <p>
 * Current known implementations inlcude the Microsoft office word document editor in swing. This will be released soon
 * as a scout swing fragment under epl.
 */
public abstract class AbstractDocumentField extends AbstractValueField<RemoteFile> implements IDocumentField {
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

  public RemoteFile saveAs(String name, String formatType, long timeout) throws ProcessingException {
    if (name == null) {
      if (getValue() != null) {
        name = getValue().getName();
      }
      else {
        name = "document.doc";
      }
    }
    String simpleName = name.substring(0, name.lastIndexOf('.'));
    File dir = IOUtility.createTempDirectory("doc");
    File f = new File(dir, simpleName + "." + formatType);
    fireDocumentFieldEventInternal(new DocumentFieldEvent(this, DocumentFieldEvent.TYPE_SAVE_AS, f, formatType));
    try {
      long steps = timeout / 100;
      for (long i = 0; i < steps && !f.exists(); i++) {
        Thread.sleep(100L);
      }
      if (!f.exists()) {
        throw new ProcessingException("Timeout waiting for document creation");
      }
      if (dir.listFiles().length == 1) {
        RemoteFile r = new RemoteFile(f.getName(), f.lastModified());
        r.readData(f);
        return r;
      }
      RemoteFile r = new RemoteFile(simpleName + ".zip", f.lastModified());
      r.readZipContentFromDirectory(dir);
      return r;
    }
    catch (ProcessingException pe) {
      throw pe;
    }
    catch (Throwable t) {
      throw new ProcessingException("Unexpected", t);
    }
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
