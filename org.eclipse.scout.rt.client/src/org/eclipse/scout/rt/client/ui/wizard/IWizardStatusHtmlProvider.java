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

import org.eclipse.scout.commons.exception.ProcessingException;

/**
 * Used by {@link AbstractWizardStatusField} and {@link DefaultWizardContainerForm} to customize html presentation of
 * wizard status content
 */
public interface IWizardStatusHtmlProvider {

  /**
   * initialize, load html template and inline images
   */
  void initialize(AbstractWizardStatusField htmlField) throws ProcessingException;

  String createHtml(IWizard w) throws ProcessingException;

}
