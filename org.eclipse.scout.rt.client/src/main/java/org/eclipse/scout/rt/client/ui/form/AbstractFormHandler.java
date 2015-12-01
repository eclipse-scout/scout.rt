/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form;

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.form.FormHandlerChains.FormHandlerCheckFieldsChain;
import org.eclipse.scout.rt.client.extension.ui.form.FormHandlerChains.FormHandlerDiscardChain;
import org.eclipse.scout.rt.client.extension.ui.form.FormHandlerChains.FormHandlerFinallyChain;
import org.eclipse.scout.rt.client.extension.ui.form.FormHandlerChains.FormHandlerLoadChain;
import org.eclipse.scout.rt.client.extension.ui.form.FormHandlerChains.FormHandlerPostLoadChain;
import org.eclipse.scout.rt.client.extension.ui.form.FormHandlerChains.FormHandlerStoreChain;
import org.eclipse.scout.rt.client.extension.ui.form.FormHandlerChains.FormHandlerValidateChain;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.IFormHandlerExtension;
import org.eclipse.scout.rt.shared.extension.AbstractExtension;
import org.eclipse.scout.rt.shared.extension.IExtensibleObject;
import org.eclipse.scout.rt.shared.extension.IExtension;
import org.eclipse.scout.rt.shared.extension.ObjectExtensions;

public abstract class AbstractFormHandler implements IFormHandler, IExtensibleObject {
  private IForm m_form;
  private boolean m_openExclusive;
  private final ObjectExtensions<AbstractFormHandler, IFormHandlerExtension<? extends AbstractFormHandler>> m_objectExtensions;

  public AbstractFormHandler() {
    m_objectExtensions = new ObjectExtensions<AbstractFormHandler, IFormHandlerExtension<? extends AbstractFormHandler>>(this);
    interceptInitConfig();
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(10)
  protected boolean getConfiguredGuiLess() {
    return false;
  }

  /**
   * @return false to open a new form anytime, true to open a form<br>
   *         with a same {@link IForm#computeExclusiveKey()} only once.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(20)
  protected boolean getConfiguredOpenExclusive() {
    return false;
  }

  /**
   * Before the form is activated, this method loads its data.<br>
   * After this method call, the form is in the state "Saved / Unchanged" All field value changes done here appear as
   * unchanged in the form.
   */
  @ConfigOperation
  @Order(10)
  protected void execLoad() {
  }

  /**
   * Load additional form state<br>
   * this method call is after the form was loaded into the state "Saved / Unchanged"<br>
   * any changes to fields might result in the form ot fields being changed and therefore in the state
   * "Save needed / Changed"
   */
  @ConfigOperation
  @Order(20)
  protected void execPostLoad() {
  }

  /**
   * This method is called in order to check field validity.<br>
   * This method is called just after the {@link IForm#interceptCheckFields()} but before the form is validated and
   * stored.<br>
   * After this method, the form is checking fields itself and displaying a dialog with missing and invalid fields.
   *
   * @return true when this check is done and further checks can continue, false to silently cancel the current process
   * @throws ProcessingException
   *           to cancel the current process with error handling and user notification such as a dialog
   */
  @ConfigOperation
  @Order(40)
  protected boolean execCheckFields() {
    return true;
  }

  /**
   * This method is called in order to update derived states like button enablings.<br>
   * This method is called after the {@link IForm#interceptValidate()} but before the form is stored.<br>
   *
   * @return true when validate is successful, false to silently cancel the current process
   * @throws ProcessingException
   *           to cancel the current process with error handling and user notification such as a dialog
   */
  @ConfigOperation
  @Order(50)
  protected boolean execValidate() {
    return true;
  }

  /**
   * Store form state<br>
   * after this method call, the form is in the state "Saved / Unchanged" When the form is closed using Ok, Save,
   * Search, Next, etc.. this method is called to apply the changes to the persistency layer
   */
  @ConfigOperation
  @Order(40)
  protected void execStore() {
  }

  /**
   * When the form is closed using cancel or close this method is called to manage the case that no changes should be
   * performed (revert case)
   */
  @ConfigOperation
  @Order(30)
  protected void execDiscard() {
  }

  /**
   * Finalize form state<br>
   * called whenever the handler is finished and the form is closed When the form is closed in any way this method is
   * called to dispose of resources or deallocate services
   */
  @ConfigOperation
  @Order(60)
  protected void execFinally() {
  }

  /*
   * Runtime
   */
  protected final void interceptInitConfig() {
    m_objectExtensions.initConfig(createLocalExtension(), new Runnable() {
      @Override
      public void run() {
        initConfig();
      }
    });
  }

  protected void initConfig() {
    setOpenExclusive(getConfiguredOpenExclusive());
  }

  protected IFormHandlerExtension<? extends AbstractFormHandler> createLocalExtension() {
    return new LocalFormHandlerExtension<AbstractFormHandler>(this);
  }

  @Override
  public final List<? extends IFormHandlerExtension<? extends AbstractFormHandler>> getAllExtensions() {
    return m_objectExtensions.getAllExtensions();
  }

  @Override
  public <T extends IExtension<?>> T getExtension(Class<T> c) {
    return m_objectExtensions.getExtension(c);
  }

  @Override
  public void setOpenExclusive(boolean openExclusive) {
    m_openExclusive = openExclusive;
  }

  @Override
  public boolean isOpenExclusive() {
    return m_openExclusive;
  }

  @Override
  public IForm getForm() {
    return m_form;
  }

  /**
   * do not use this internal method
   */
  @Override
  public void setFormInternal(IForm form) {
    m_form = form;
  }

  @Override
  public String getHandlerId() {
    String s = getClass().getName();
    int i = Math.max(s.lastIndexOf('$'), s.lastIndexOf('.'));
    s = s.substring(i + 1);
    return s;
  }

  @Override
  public boolean isGuiLess() {
    return getConfiguredGuiLess();
  }

  @Override
  public final void onLoad() {
    interceptLoad();
  }

  @Override
  public final void onPostLoad() {
    interceptPostLoad();
  }

  @Override
  public final boolean onCheckFields() {
    return interceptCheckFields();
  }

  @Override
  public final boolean onValidate() {
    return interceptValidate();
  }

  @Override
  public final void onStore() {
    interceptStore();
  }

  @Override
  public final void onDiscard() {
    interceptDiscard();
  }

  @Override
  public final void onFinally() {
    interceptFinally();
  }

  /**
   * The extension delegating to the local methods. This Extension is always at the end of the chain and will not call
   * any further chain elements.
   */
  protected static class LocalFormHandlerExtension<OWNER extends AbstractFormHandler> extends AbstractExtension<OWNER> implements IFormHandlerExtension<OWNER> {

    public LocalFormHandlerExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public void execPostLoad(FormHandlerPostLoadChain chain) {
      getOwner().execPostLoad();
    }

    @Override
    public boolean execValidate(FormHandlerValidateChain chain) {
      return getOwner().execValidate();
    }

    @Override
    public void execLoad(FormHandlerLoadChain chain) {
      getOwner().execLoad();
    }

    @Override
    public void execStore(FormHandlerStoreChain chain) {
      getOwner().execStore();
    }

    @Override
    public void execDiscard(FormHandlerDiscardChain chain) {
      getOwner().execDiscard();
    }

    @Override
    public boolean execCheckFields(FormHandlerCheckFieldsChain chain) {
      return getOwner().execCheckFields();
    }

    @Override
    public void execFinally(FormHandlerFinallyChain chain) {
      getOwner().execFinally();
    }

  }

  protected final void interceptPostLoad() {
    List<? extends IFormHandlerExtension<? extends AbstractFormHandler>> extensions = getAllExtensions();
    FormHandlerPostLoadChain chain = new FormHandlerPostLoadChain(extensions);
    chain.execPostLoad();
  }

  protected final boolean interceptValidate() {
    List<? extends IFormHandlerExtension<? extends AbstractFormHandler>> extensions = getAllExtensions();
    FormHandlerValidateChain chain = new FormHandlerValidateChain(extensions);
    return chain.execValidate();
  }

  protected final void interceptLoad() {
    List<? extends IFormHandlerExtension<? extends AbstractFormHandler>> extensions = getAllExtensions();
    FormHandlerLoadChain chain = new FormHandlerLoadChain(extensions);
    chain.execLoad();
  }

  protected final void interceptStore() {
    List<? extends IFormHandlerExtension<? extends AbstractFormHandler>> extensions = getAllExtensions();
    FormHandlerStoreChain chain = new FormHandlerStoreChain(extensions);
    chain.execStore();
  }

  protected final void interceptDiscard() {
    List<? extends IFormHandlerExtension<? extends AbstractFormHandler>> extensions = getAllExtensions();
    FormHandlerDiscardChain chain = new FormHandlerDiscardChain(extensions);
    chain.execDiscard();
  }

  protected final boolean interceptCheckFields() {
    List<? extends IFormHandlerExtension<? extends AbstractFormHandler>> extensions = getAllExtensions();
    FormHandlerCheckFieldsChain chain = new FormHandlerCheckFieldsChain(extensions);
    return chain.execCheckFields();
  }

  protected final void interceptFinally() {
    List<? extends IFormHandlerExtension<? extends AbstractFormHandler>> extensions = getAllExtensions();
    FormHandlerFinallyChain chain = new FormHandlerFinallyChain(extensions);
    chain.execFinally();
  }
}
