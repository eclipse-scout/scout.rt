/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.client.ui.form.fields.graphfield;

import java.net.URL;
import java.util.List;

import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.graphfield.GraphFieldChains.GraphFieldHyperlinkActionChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.graphfield.IGraphFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.shared.data.basic.graph.GraphModel;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.service.SERVICES;

public class AbstractGraphField extends AbstractValueField<GraphModel> implements IGraphField {

  private IGraphFieldUIFacade m_uiFacade;

  @Override
  protected void initConfig() {
    m_uiFacade = new P_UIFacade();
    super.initConfig();
  }

  /**
   * @param url
   * @param path
   *          {@link URL#getPath()}
   * @param local
   *          true if the url is not a valid external url but a local model url
   *          (http://local/...)
   */
  @ConfigOperation
  @Order(230)
  protected void execHyperlinkAction(URL url, String path, boolean local) throws ProcessingException {
  }

  public boolean isLoading() {
    return propertySupport.getPropertyBool(PROP_LOADING);
  }

  public void setLoading(boolean loading) {
    propertySupport.setPropertyBool(PROP_LOADING, loading);
  }

  @Override
  public IGraphFieldUIFacade getUIFacade() {
    return m_uiFacade;
  }

  @Override
  public void doHyperlinkAction(URL url) throws ProcessingException {
    if (url != null) {
      interceptHyperlinkAction(url, url.getPath(), "local".equals(url.getHost()));
    }
  }

  private class P_UIFacade implements IGraphFieldUIFacade {

    @Override
    public void fireHyperlinkActionFromUI(URL url) {
      try {
        doHyperlinkAction(url);
      }
      catch (ProcessingException e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(e);
      }
    }
  }

  protected final void interceptHyperlinkAction(URL url, String path, boolean local) throws ProcessingException {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    GraphFieldHyperlinkActionChain chain = new GraphFieldHyperlinkActionChain(extensions);
    chain.execHyperlinkAction(url, path, local);
  }

  protected static class LocalGraphFieldExtension<OWNER extends AbstractGraphField> extends LocalValueFieldExtension<GraphModel, OWNER> implements IGraphFieldExtension<OWNER> {

    public LocalGraphFieldExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public void execHyperlinkAction(GraphFieldHyperlinkActionChain chain, URL url, String path, boolean local) throws ProcessingException {
      getOwner().execHyperlinkAction(url, path, local);
    }
  }

  @Override
  protected IGraphFieldExtension<? extends AbstractGraphField> createLocalExtension() {
    return new LocalGraphFieldExtension<AbstractGraphField>(this);
  }
}
