/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.services.common.text.AbstractDynamicNlsTextProviderService;

/**
 * Text provider service for text used in Html UI plugin.
 * <p>
 * Note: Texts that are required on the UI (javascript) have to be sent to the browser beforehand, by adding the
 * corresponding keys to {@link UiSession#getTextKeys()}.
 */
@Order(5010)
public class UiTextProviderService extends AbstractDynamicNlsTextProviderService {

  @Override
  protected String getDynamicNlsBaseName() {
    return "org.eclipse.scout.rt.ui.html.texts.Texts";
  }
}
