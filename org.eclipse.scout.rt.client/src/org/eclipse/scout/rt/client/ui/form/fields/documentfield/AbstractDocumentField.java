package org.eclipse.scout.rt.client.ui.form.fields.documentfield;

import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.TypeCastUtility;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.client.ui.form.fields.documentfield.eventdata.SaveAsData;
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

  public AbstractDocumentField() {
    this(true);
  }

  public AbstractDocumentField(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected double getConfiguredGridWeightX() {
    return 1;
  }

  @Override
  protected double getConfiguredGridWeightY() {
    return 1;
  }

  @Override
  protected boolean getConfiguredLabelVisible() {
    return false;
  }

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

  @Override
  public IDocumentFieldUIFacade getUIFacade() {
    return m_uiFacade;
  }

  @Override
  public void setRulersVisible(boolean b) {
    propertySupport.setPropertyBool(PROP_RULERS_VISIBLE, b);
  }

  @Override
  public boolean isRulersVisible() {
    return propertySupport.getPropertyBool(PROP_RULERS_VISIBLE);
  }

  @Override
  public void setStatusBarVisible(boolean b) {
    propertySupport.setPropertyBool(PROP_STATUS_BAR_VISIBLE, b);
  }

  @Override
  public boolean isStatusBarVisible() {
    return propertySupport.getPropertyBool(PROP_STATUS_BAR_VISIBLE);
  }

  @Override
  public void addDocumentFieldListener(DocumentFieldListener listener) {
    m_listenerList.add(DocumentFieldListener.class, listener);
  }

  @Override
  public void removeDocumentFieldListener(DocumentFieldListener listener) {
    m_listenerList.remove(DocumentFieldListener.class, listener);
  }

  @Override
  public boolean isComReady() {
    return propertySupport.getPropertyBool(PROP_COM_READY);
  }

  @ConfigOperation
  protected void execComReadyStatusChanged(boolean ready) throws ProcessingException {
  }

  // main handler
  protected Object fireDocumentFieldEventInternal(DocumentFieldEvent e) throws ProcessingException {
    Object returnValue = null;
    ProcessingException exception = null;
    DocumentFieldListener[] listeners = m_listenerList.getListeners(DocumentFieldListener.class);
    if (listeners != null && listeners.length > 0) {
      for (int i = 0; i < listeners.length; i++) {
        try {
          Object tmp = listeners[i].documentFieldChanged(e);
          if (returnValue == null) {
            returnValue = tmp;
          }
        }
        catch (ProcessingException t) {
          exception = t;
        }
      }
    }

    if (exception != null) {
      throw exception;
    }

    return returnValue;
  }

  @Override
  protected boolean execIsSaveNeeded() throws ProcessingException {
    if (!isInitialized() || getForm().isFormLoading()) {
      return false;
    }
    // mark field for saving. There is no event to listen on...
    Object ret = fireDocumentFieldEventInternal(new DocumentFieldEvent(AbstractDocumentField.this, DocumentFieldEvent.TYPE_SAVE_NEEDED));
    return TypeCastUtility.castValue(ret, boolean.class);
  }

  @Override
  public RemoteFile save() throws ProcessingException {
    return saveAs(null, null);
  }

  @Override
  public RemoteFile saveAs(String name) throws ProcessingException {
    return saveAs(name, null);
  }

  @Override
  public RemoteFile saveAs(String name, String format) throws ProcessingException {
    return (RemoteFile) fireDocumentFieldEventInternal(new DocumentFieldEvent(this, DocumentFieldEvent.TYPE_SAVE_AS, new SaveAsData(name, format)));
  }

  @Override
  public void autoResizeDocument() {
    try {
      fireDocumentFieldEventInternal(new DocumentFieldEvent(this, DocumentFieldEvent.TYPE_AUTORESIZE_DOCUMENT));
    }
    catch (ProcessingException e) {
      LOG.warn("Could not auto resize document", e);
    }
  }

  protected IDocumentFieldUIFacade createUIFacade() {
    return new P_UIFacade();
  }

  protected class P_UIFacade implements IDocumentFieldUIFacade {

    @Override
    public void setDocumentFromUI(RemoteFile remoteFile) {
      try {
        setFieldChanging(true);
        setValue(remoteFile);
      }
      finally {
        setFieldChanging(false);
      }
    }

    @Override
    public void fireComReadyFromUI(boolean comReady) {
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
