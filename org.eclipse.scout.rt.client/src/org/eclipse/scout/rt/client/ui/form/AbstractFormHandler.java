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

import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;

public abstract class AbstractFormHandler implements IFormHandler {
  private IForm m_form;
  private boolean m_openExclusive;

  public AbstractFormHandler() {
    initConfig();
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(10)
  @ConfigPropertyValue("false")
  protected boolean getConfiguredGuiLess() {
    return false;
  }

  /**
   * @return false to open a new form anytime, true to open a form<br>
   *         with a same {@link IForm#computeExclusiveKey()} only once.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(20)
  @ConfigPropertyValue("false")
  protected boolean getConfiguredOpenExclusive() {
    return false;
  }

  /**
   * Before the form is activated, this method loads its data.<br>
   * After this method call, the form is in the state "Saved / Unchanged" All
   * field value changes done here appear as unchanged in the form.
   */
  @ConfigOperation
  @Order(10)
  protected void execLoad() throws ProcessingException {
  }

  /**
   * Load additional form state<br>
   * this method call is after the form was loaded into the state
   * "Saved / Unchanged"<br>
   * any changes to fields might result in the form ot fields being changed and
   * therefore in the state "Save needed / Changed"
   */
  @ConfigOperation
  @Order(20)
  protected void execPostLoad() throws ProcessingException {
  }

  /**
   * This method is called in order to check field validity.<br>
   * This method is called just after the {@link IForm#execCheckFields()} but
   * before the form is validated and stored.<br>
   * After this method, the form is checking fields itself and displaying a
   * dialog with missing and invalid fields.
   * 
   * @return true when this check is done and further checks can continue, false
   *         to silently cancel the current process
   * @throws ProcessingException
   *           to cancel the current process with error handling and user
   *           notification such as a dialog
   */
  @ConfigOperation
  @Order(40)
  protected boolean execCheckFields() throws ProcessingException {
    return true;
  }

  /**
   * This method is called in order to update derived states like button
   * enablings.<br>
   * This method is called after the {@link IForm#execValidate()} but before the
   * form is stored.<br>
   * 
   * @return true when validate is successful, false to silently cancel the
   *         current process
   * @throws ProcessingException
   *           to cancel the current process with error handling and user
   *           notification such as a dialog
   */
  @ConfigOperation
  @Order(50)
  protected boolean execValidate() throws ProcessingException {
    return true;
  }

  /**
   * Store form state<br>
   * after this method call, the form is in the state "Saved / Unchanged" When
   * the form is closed using Ok, Save, Search, Next, etc.. this method is
   * called to apply the changes to the persistency layer
   */
  @ConfigOperation
  @Order(40)
  protected void execStore() throws ProcessingException {
  }

  /**
   * When the form is closed using cancel or close this method is called to
   * manage the case that no changes should be performed (revert case)
   */
  @ConfigOperation
  @Order(30)
  protected void execDiscard() throws ProcessingException {
  }

  /**
   * Finalize form state<br>
   * called whenever the handler is finished and the form is closed When the
   * form is closed in any way this method is called to dispose of resources or
   * deallocate services
   */
  @ConfigOperation
  @Order(60)
  protected void execFinally() throws ProcessingException {
  }

  /*
   * Runtime
   */
  protected void initConfig() {
    setOpenExclusive(getConfiguredOpenExclusive());
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
  public final void onLoad() throws ProcessingException {
    try {
      execLoad();
    }
    catch (ProcessingException e) {
      throw e;
    }
    catch (Throwable e) {
      throw new ProcessingException("Unexpected", e);
    }
  }

  @Override
  public final void onPostLoad() throws ProcessingException {
    try {
      execPostLoad();
    }
    catch (ProcessingException e) {
      throw e;
    }
    catch (Throwable e) {
      throw new ProcessingException("Unexpected", e);
    }
  }

  @Override
  public final boolean onCheckFields() throws ProcessingException {
    try {
      return execCheckFields();
    }
    catch (ProcessingException e) {
      throw e;
    }
    catch (Throwable e) {
      throw new ProcessingException("Unexpected", e);
    }
  }

  @Override
  public final boolean onValidate() throws ProcessingException {
    try {
      return execValidate();
    }
    catch (ProcessingException e) {
      throw e;
    }
    catch (Throwable e) {
      throw new ProcessingException("Unexpected", e);
    }
  }

  @Override
  public final void onStore() throws ProcessingException {
    try {
      execStore();
    }
    catch (ProcessingException e) {
      throw e;
    }
    catch (Throwable e) {
      throw new ProcessingException("Unexpected", e);
    }
  }

  @Override
  public final void onDiscard() throws ProcessingException {
    try {
      execDiscard();
    }
    catch (ProcessingException e) {
      throw e;
    }
    catch (Throwable e) {
      throw new ProcessingException("Unexpected", e);
    }
  }

  @Override
  public final void onFinally() throws ProcessingException {
    try {
      execFinally();
    }
    catch (ProcessingException e) {
      throw e;
    }
    catch (Throwable e) {
      throw new ProcessingException("Unexpected", e);
    }
  }

}
