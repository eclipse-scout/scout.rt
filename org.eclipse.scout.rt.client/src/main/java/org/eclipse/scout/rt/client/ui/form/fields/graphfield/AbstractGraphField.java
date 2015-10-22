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
import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.graphfield.GraphFieldChains.GraphFieldAppLinkActionChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.graphfield.IGraphFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.shared.data.basic.graph.GraphModel;

public class AbstractGraphField extends AbstractValueField<GraphModel> implements IGraphField {

  private IGraphFieldUIFacade m_uiFacade;

  @Override
  protected void initConfig() {
    m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(new P_UIFacade(), ModelContext.copyCurrent());
    super.initConfig();
  }

  /**
   * @param url
   * @param path
   *          {@link URL#getPath()}
   * @param local
   *          true if the url is not a valid external url but a local model url (http://local/...)
   */
  @ConfigOperation
  @Order(230)
  protected void execAppLinkAction(String ref) {
  }

  @Override
  public IGraphFieldUIFacade getUIFacade() {
    return m_uiFacade;
  }

  @Override
  public void doAppLinkAction(String ref) {
    interceptAppLinkAction(ref);
  }

  private class P_UIFacade implements IGraphFieldUIFacade {

    @Override
    public void fireAppLinkActionFromUI(String ref) {
      try {
        doAppLinkAction(ref);
      }
      catch (ProcessingException e) {
        BEANS.get(ExceptionHandler.class).handle(e);
      }
    }
  }

  protected final void interceptAppLinkAction(String ref) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    GraphFieldAppLinkActionChain chain = new GraphFieldAppLinkActionChain(extensions);
    chain.execAppLinkAction(ref);
  }

  protected static class LocalGraphFieldExtension<OWNER extends AbstractGraphField> extends LocalValueFieldExtension<GraphModel, OWNER> implements IGraphFieldExtension<OWNER> {

    public LocalGraphFieldExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public void execAppLinkAction(GraphFieldAppLinkActionChain chain, String ref) {
      getOwner().execAppLinkAction(ref);
    }
  }

  @Override
  protected IGraphFieldExtension<? extends AbstractGraphField> createLocalExtension() {
    return new LocalGraphFieldExtension<AbstractGraphField>(this);
  }
}
