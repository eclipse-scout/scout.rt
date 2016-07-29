/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields.beanfield;

import java.util.List;

import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.beanfield.BeanFieldChains.BeanFieldAppLinkActionChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.beanfield.IBeanFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.classid.ClassId;

/**
 * This field may be used if the value is relevant for the gui, and not just the display text.
 */
@ClassId("9fcc88ba-acf8-4409-b310-7d7132a0b6f3")
public abstract class AbstractBeanField<VALUE> extends AbstractValueField<VALUE> implements IBeanField<VALUE> {
  private IBeanFieldUIFacade m_uiFacade;

  @Override
  protected void initConfig() {
    super.initConfig();
    m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(new P_UIFacade(), ModelContext.copyCurrent());
  }

  @Override
  public IBeanFieldUIFacade getUIFacade() {
    return m_uiFacade;
  }

  @Override
  protected String execFormatValue(VALUE value) {
    // Bean fields should not have a display text by default, because it cannot be generated automatically
    // from the value bean. To suppress the default Object.toString() result, we return "" here. However,
    // subclasses may define their own bean formatting by overriding this method (although it will
    // probably be of no use).
    return "";
  }

  /**
   * Called when an app link has been clicked.
   * <p>
   * Subclasses can override this method. The default does nothing.
   */
  @ConfigOperation
  @Order(10)
  protected void execAppLinkAction(String ref) {
  }

  protected final void interceptAppLinkAction(String ref) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    BeanFieldAppLinkActionChain chain = new BeanFieldAppLinkActionChain<VALUE>(extensions);
    chain.execAppLinkAction(ref);
  }

  protected static class LocalBeanFieldExtension<VALUE, OWNER extends AbstractBeanField<VALUE>> extends LocalValueFieldExtension<VALUE, OWNER> implements IBeanFieldExtension<VALUE, OWNER> {

    public LocalBeanFieldExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public void execAppLinkAction(BeanFieldAppLinkActionChain<VALUE> chain, String ref) {
      getOwner().execAppLinkAction(ref);
    }
  }

  @Override
  protected IBeanFieldExtension<VALUE, ? extends AbstractBeanField<VALUE>> createLocalExtension() {
    return new LocalBeanFieldExtension<VALUE, AbstractBeanField<VALUE>>(this);
  }

  @Override
  public void doAppLinkAction(String ref) {
    interceptAppLinkAction(ref);
  }

  protected class P_UIFacade implements IBeanFieldUIFacade {

    @Override
    public void fireAppLinkActionFromUI(String ref) {
      doAppLinkAction(ref);
    }
  }
}
