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

/**
 * Used by {@link AbstractWizardStatusField} and {@link DefaultWizardContainerForm} to customize html presentation of
 * wizard status content
 */
public interface IWizardStatusHtmlProvider {

  /**
   * initialize, load html template and inline images
   */
  void initialize(AbstractWizardStatusField htmlField);

  /**
   * Initialize the HTML template. <br>
   * Template should contain the following placeholders:<br>
   * <ul>
   * <li><b>#TOP#</b>: Wizard titleHtml ({@link IWizard#getTitleHtml()}) or tooltipText (
   * {@link IWizard#getTooltipText()}) will be placed here.</li>
   * <li><b>#FONT_SIZE_UNIT#</b>: Unit for font size calculation will be placed here.</li>
   * <li><b>#LIST#</b>: Step list will be placed here.</li>
   * <li><b>#BOTTOM#</b>: WizardStep descriptionHtml ({@link IWizardStep#getDescriptionHtml()}) or tooltipText (
   * {@link IWizardStep#getTooltipText()}) will be placed here.</li>
   * </ul>
   * 
   * @return template HTML file
   */
  String initHtmlTemplate();

  String createHtml(IWizard w);

}
