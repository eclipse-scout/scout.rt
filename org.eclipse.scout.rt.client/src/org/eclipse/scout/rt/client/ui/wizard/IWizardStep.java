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
package org.eclipse.scout.rt.client.ui.wizard;

import org.eclipse.scout.commons.ITypeWithClassId;
import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.IForm;

public interface IWizardStep<T extends IForm> extends IPropertyObserver, ITypeWithClassId {

  String PROP_ENABLED = "enabled";
  String PROP_TITLE = "title";
  String PROP_TOOLTIP_TEXT = "tooltipText";
  String PROP_TITLE_HTML = "titleHtml";
  String PROP_DESCRIPTION_HTML = "descriptionHtml";
  String PROP_ICON_ID = "iconId";
  /**
   * The step was activated by a "next" operation
   */
  int STEP_NEXT = 1;
  /**
   * The step was activated by a "back" operation
   */
  int STEP_PREVIOUS = 2;

  IWizard getWizard();

  /**
   * Do not call this internal method.
   */
  void setWizardInternal(IWizard w);

  String getTitle();

  void setTitle(String s);

  String getIconId();

  void setIconId(String s);

  String getTooltipText();

  void setTooltipText(String s);

  boolean isEnabled();

  void setEnabled(boolean b);

  String getTitleHtml();

  void setTitleHtml(String s);

  String getDescriptionHtml();

  void setDescriptionHtml(String s);

  /**
   * @return the cached for this step or null
   */
  T getForm();

  /**
   * cache the form for this step for later usage
   */
  void setForm(T form);

  /**
   * @param stepKind
   *          any of the STEP_* constants activate this step normally creates a
   *          form, calls {@link IForm#startWizardStep(IWizardStep2, Class)} on
   *          the form and places the form inside the wizard
   *          {@link IWizard#setWizardForm(org.eclipse.scout.rt.client.ui.form.IForm)}
   */
  void activate(int stepKind) throws ProcessingException;

  /**
   * @param stepKind
   *          any of the STEP_* constants deactivate this step
   */
  void deactivate(int stepKind) throws ProcessingException;

  /**
   * dispose this step The default implementation closes the form at {@link #getForm()}
   */
  void dispose() throws ProcessingException;
}
