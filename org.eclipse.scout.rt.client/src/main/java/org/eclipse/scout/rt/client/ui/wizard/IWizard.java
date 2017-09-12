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
package org.eclipse.scout.rt.client.ui.wizard;

import java.util.List;

import org.eclipse.scout.rt.client.ui.IAppLinkCapable;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.classid.ITypeWithClassId;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.reflect.IPropertyObserver;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;

public interface IWizard extends IPropertyObserver, ITypeWithClassId, IAppLinkCapable {

  enum CloseType {
    /**
     * wizard is not yet closed so the close state is unknown, see also {@link IWizard#close()},
     * {@link IWizard#isClosed()}, {@link IWizard#getCloseType()}
     */
    Unknown,
    /**
     * wizard was just closed, see also {@link IWizard#close()}, {@link IWizard#isClosed()},
     * {@link IWizard#getCloseType()}
     */
    Closed,
    /**
     * wizard was finished, see also {@link IWizard#doFinish()}, {@link IWizard#isClosed()},
     * {@link IWizard#getCloseType()}
     */
    Finished,
    /**
     * wizard was cancelled, see also {@link IWizard#doCancel()}, {@link IWizard#isClosed()},
     * {@link IWizard#getCloseType()}
     */
    Cancelled,

    /**
     * wizard was suspended, see also {@link IWizard#doSuspend()}, {@link IWizard#isClosed()},
     * {@link IWizard#getCloseType()}
     */
    Suspended,
  }

  String PROP_TITLE = "title";
  String PROP_SUB_TITLE = "subTitle";
  /**
   * {@link IForm}
   */
  String PROP_WIZARD_FORM = "wizardForm";
  /**
   * boolean
   */
  String PROP_CLOSED = "closed";
  /**
   * {@link CloseType}
   */
  String PROP_CLOSE_TYPE = "closeType";

  void addWizardListener(WizardListener listener);

  void removeWizardListener(WizardListener listener);

  String getTitle();

  void setTitle(String title);

  String getSubTitle();

  void setSubTitle(String subTitle);

  /**
   * when the wizard is changed (for example the state) all these changes are accumulated and then fired in single
   * events Therefore the whole block is normally inside a try...finally
   *
   * @see IWizard#doNextStep()
   */
  void setChanging(boolean b);

  boolean isChanging();

  /**
   * No more operations are possible on a closed wizard.
   */
  boolean isClosed();

  /**
   * @return the type in which the wizard was closed
   */
  CloseType getCloseType();

  /**
   * start the wizard
   */
  void start();

  /**
   * Convenience method to get the current desktop.
   */
  IDesktop getDesktop();

  /**
   * @return current wizard form or <code>null</code> if there is no current wizard form.
   */
  IForm getWizardForm();

  /**
   * Set current wizard form or <code>null</code> if there is no current wizard form. Use handle
   */
  void setWizardForm(IForm form);

  /**
   * Wait until form is closed<br>
   * If the form is modal this method returns just after the modal handler has terminated<br>
   * If the form is non-modal this starts a sub event dispatcher that loops (and blocks) until form handling is false
   * (i.e. form has been closed)
   */
  void waitFor();

  int WAIT_FOR_ERROR_CODE = 69218;

  /**
   * This method by default calls {@link #execRefreshButtonPolicy()} which is used to make then wizard buttons
   * visible/invisible/enabled/disabled based on the current form and wizard state.
   */
  void refreshButtonPolicy();

  /**
   * close wizard (forced suspend)
   */
  void close();

  /**
   * @return all available steps, see also {@link #getSteps()}
   */
  List<IWizardStep<? extends IForm>> getAvailableSteps();

  /**
   * set all available steps, see also {@link #setSteps(List)}
   */
  void setAvailableSteps(List<IWizardStep<? extends IForm>> steps);

  /**
   * @return all steps including history, current, future
   */
  List<IWizardStep<? extends IForm>> getSteps();

  /**
   * set the list of active steps
   */
  void setSteps(IWizardStep<?>... steps);

  /**
   * set the list of active steps
   */
  void setSteps(List<IWizardStep<? extends IForm>> steps);

  <T extends IWizardStep<? extends IForm>> T getAvailableStep(Class<T> type);

  <T extends IWizardStep<? extends IForm>> T getStep(Class<T> type);

  IWizardStep<? extends IForm> getStep(int index);

  IWizardStep<? extends IForm> getStepBySimpleClassName(String simpleClassName);

  IWizardStep<? extends IForm> getStepByClassName(String className);

  int getStepIndex(IWizardStep<? extends IForm> step);

  /**
   * Convenience method
   *
   * @return {@link IWizardStep#STEP_NEXT} or {@link IWizardStep#STEP_PREVIOUS} Note: If from or to is null then
   *         STEP_NEXT is returned.
   */
  int getStepKind(IWizardStep<? extends IForm> from, IWizardStep<? extends IForm> to);

  /**
   * Convenience method
   *
   * @param from
   *          step
   * @param includeFrom
   *          if and only if this is true, the list begins with the <tt>from</tt> step, regardless <tt>from</tt> is null
   *          or not
   * @param to
   *          step
   * @param includeTo
   *          if and only if this is true, the list ends with the <tt>to</tt> step, regardless <tt>to</tt> is null or
   *          not
   * @return a new non null list of all steps between <tt>from</tt> and <tt>to</tt> step.
   */
  List<IWizardStep<? extends IForm>> getStepSpan(IWizardStep<? extends IForm> from, boolean includeFrom, IWizardStep<? extends IForm> to, boolean includeTo);

  /**
   * Convenience method
   *
   * @return step before active step
   */
  IWizardStep<? extends IForm> getPreviousStep();

  /**
   * Convenience method
   *
   * @return step after active step
   */
  IWizardStep<? extends IForm> getNextStep();

  /**
   * Convenience method
   *
   * @return first step before active step that is enabled
   */
  IWizardStep<? extends IForm> getPreviousEnabledStep();

  /**
   * Convenience method
   *
   * @return first step after active step that is enabled
   */
  IWizardStep<? extends IForm> getNextEnabledStep();

  /**
   * Set and activate a new wizard state, throws a {@link VetoException} if the new step could not be activated, for
   * example if the currently active step vetoed in its deactivate() method Detailed: 1. calculate direction of change,
   * either {@link IWizardStep#STEP_NEXT} or {@link IWizardStep#STEP_PREVIOUS} 2. call
   * {@link IWizardStep#deactivate(int)} on the old step if that step exists 3. call {@link IWizardStep#activate(int)}
   * on the new step if that step exists When calling {@link #activateStep(null)} with a null argument it simply calls
   * deactivate on the existing step.
   */
  void activateStep(IWizardStep<? extends IForm> step);

  /**
   * set the current wizard state
   *
   * @param jumpForward
   *          true simply jumps to the new step, false activates/deactivates every step inbetween the old and the new
   *          step
   * @param jumpBackward
   *          true simply jumps to the new step, false activates/deactivates every step inbetween the old and the new
   *          step The jump parameters are only relevant if the new step is not the direct previous or next step of the
   *          old step.
   */
  void activateStep(IWizardStep<? extends IForm> step, boolean jumpForward, boolean jumpBackward);

  IWizardStep<? extends IForm> getActiveStep();

  /**
   * next step
   */
  void doNextStep();

  /**
   * previous step
   */
  void doPreviousStep();

  /**
   * finish
   */
  void doFinish();

  /**
   * cancel
   */
  void doCancel();

  /**
   * suspend
   */
  void doSuspend();

  /**
   * reset
   */
  void doReset();

  /**
   * This is a delegate method that is normally called by the wizard status field in the {@link IWizardContainerForm}
   * whenever a link is clicked.
   */
  @Override
  void doAppLinkAction(String ref);

  void doStepAction(IWizardStep<? extends IForm> step);

  /**
   * The container form is created during initialization of the wizard. The container form cannot be changed later. The
   * default wizard container form is {@link DefaultWizardContainerForm}.
   *
   * @throws AssertionException
   *           when the return value is <code>null</code>.
   */
  IWizardContainerForm createContainerForm();

  /**
   * @return the wizard's container form (created in {@link #createContainerForm()} - thus, the result should never be
   *         <code>null</code>).
   */
  IWizardContainerForm getContainerForm();

  /**
   * The wizard was started and is active and open
   */
  boolean isOpen();
}
