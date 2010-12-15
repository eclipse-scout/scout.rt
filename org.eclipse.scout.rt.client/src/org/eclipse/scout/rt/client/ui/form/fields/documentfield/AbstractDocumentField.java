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
import org.eclipse.scout.rt.client.ui.form.fields.documentfield.eventdata.DocumentFile;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.rt.shared.services.common.file.RemoteFile;
import org.eclipse.scout.service.SERVICES;

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
    m_uiFacade = createUIFacade();
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

  public boolean isComReady() {
    return propertySupport.getPropertyBool(PROP_COM_READY);
  }

  protected void execComReadyStatusChanged(boolean ready) throws ProcessingException {
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
    File file = new File(dir, simpleName + "." + formatType);
    fireDocumentFieldEventInternal(new DocumentFieldEvent(this, DocumentFieldEvent.TYPE_SAVE_AS, new DocumentFile(file, formatType)));
    try {
      long steps = timeout / 100;
      for (long i = 0; i < steps && !file.exists(); i++) {
        Thread.sleep(100L);
      }
      if (!file.exists()) {
        throw new ProcessingException("Timeout waiting for document creation");
      }
      if (dir.listFiles().length == 1) {
        RemoteFile r = new RemoteFile(file.getName(), file.lastModified());
        r.readData(file);
        return r;
      }
      RemoteFile r = new RemoteFile(simpleName + ".zip", file.lastModified());
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

  public void autoResizeDocument() {
    fireDocumentFieldEventInternal(new DocumentFieldEvent(this, DocumentFieldEvent.TYPE_AUTORESIZE_DOCUMENT));
  }

  protected IDocumentFieldUIFacade createUIFacade() {
    return new P_UIFacade();
  }

  protected class P_UIFacade implements IDocumentFieldUIFacade {

    public void fireComReady(boolean comReady) {
      try {
        if (propertySupport.setPropertyBool(PROP_COM_READY, comReady)) {
          execComReadyStatusChanged(comReady);
        }
      }
      catch (ProcessingException e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(e);
      }
      catch (Throwable t) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("Unexpected", t));
      }
    }
  }
}
