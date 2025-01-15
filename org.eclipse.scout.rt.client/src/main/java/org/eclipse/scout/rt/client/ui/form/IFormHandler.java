/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form;

import org.eclipse.scout.rt.platform.exception.ProcessingException;

/**
 * A form handler is state-less!<br>
 * State is only held on a IForm
 */
public interface IFormHandler {

  IForm getForm();

  /**
   * do not use this internal method
   */
  void setFormInternal(IForm form);

  boolean isGuiLess();

  void setOpenExclusive(boolean openExclusive);

  boolean isOpenExclusive();

  /**
   * Before the form is activated, this method loads its data.<br>
   * After this method call, the form is in the state "Saved / Unchanged" All field value changes done here appear as
   * unchanged in the form.
   */
  void onLoad();

  /**
   * Load additional form state<br>
   * this method call is after the form was loaded into the state "Saved / Unchanged"<br>
   * any changes to fields might result in the form ot fields being changed and therefore in the state "Save needed /
   * Changed"
   */
  void onPostLoad();

  /**
   * This method is called in order to check field validity.<br>
   * This method is called just after the {@link AbstractForm#execCheckFields()} but before the form is validated and stored.
   * <br>
   * After this method, the form is checking fields itself and displaying a dialog with missing and invalid fields.
   *
   * @return true when this check is done and further checks can continue, false to silently cancel the current process
   * @throws ProcessingException
   *     to cancel the current process with error handling and user notification such as a dialog
   */
  boolean onCheckFields();

  /**
   * This method is called in order to update derived states like button enablings.<br>
   * This method is called after the {@link AbstractForm#execValidate()} but before the form is stored.<br>
   *
   * @return true when validate is successful, false to silently cancel the current process
   * @throws ProcessingException
   *     to cancel the current process with error handling and user notification such as a dialog
   */
  boolean onValidate();

  /**
   * Store form state<br>
   * after this method call, the form is in the state "Saved / Unchanged" When the form is closed using Ok, Save,
   * Search, Next, etc.. this method is called to apply the changes to the persistency layer
   */
  void onStore();

  /**
   * When the form is closed using cancel or close this method is called to manage the case that no changes should be
   * performed (revert case)
   */
  void onDiscard();

  /**
   * Finalize form state<br>
   * called whenever the handler is finished and the form is closed When the form is closed in any way this method is
   * called to dispose of resources or deallocate services
   */
  void onFinally();
}
