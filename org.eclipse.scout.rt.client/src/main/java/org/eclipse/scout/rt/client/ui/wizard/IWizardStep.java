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

import org.eclipse.scout.rt.client.ui.IStyleable;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.IOrdered;
import org.eclipse.scout.rt.platform.classid.ITypeWithClassId;
import org.eclipse.scout.rt.platform.reflect.IPropertyObserver;

public interface IWizardStep<FORM extends IForm> extends IPropertyObserver, ITypeWithClassId, IOrdered, IStyleable {

  String PROP_TITLE = "title";
  String PROP_SUB_TITLE = "subTitle";
  String PROP_TOOLTIP_TEXT = "tooltipText";
  String PROP_TITLE_HTML = "titleHtml";
  String PROP_DESCRIPTION_HTML = "descriptionHtml";
  String PROP_ICON_ID = "iconId";
  String PROP_ENABLED = "enabled";
  String PROP_VISIBLE = "visible";
  String PROP_ORDER = "order";
  String PROP_ACTION_ENABLED = "actionEnabled";

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

  void setTitle(String title);

  String getSubTitle();

  void setSubTitle(String subTitle);

  String getTooltipText();

  void setTooltipText(String tooltipText);

  String getIconId();

  void setIconId(String iconId);

  boolean isEnabled();

  void setEnabled(boolean enabled);

  boolean isVisible();

  void setVisible(boolean visible);

  boolean isActionEnabled();

  void setActionEnabled(boolean actionEnabled);

  /**
   * @return the cached for this step or null
   */
  FORM getForm();

  /**
   * cache the form for this step for later usage
   */
  void setForm(FORM form);

  /**
   * @param stepKind
   *          any of the STEP_* constants activate this step normally creates a form, calls
   *          {@link IForm#startWizardStep(IWizardStep2, Class)} on the form and places the form inside the wizard
   *          {@link IWizard#setWizardForm(org.eclipse.scout.rt.client.ui.form.IForm)}
   */
  void activate(int stepKind);

  /**
   * @param stepKind
   *          any of the STEP_* constants deactivate this step
   */
  void deactivate(int stepKind);

  /**
   * dispose this step The default implementation closes the form at {@link #getForm()}
   */
  void dispose();

  /**
   * Performs the "wizard step action"
   */
  void doAction();
}
