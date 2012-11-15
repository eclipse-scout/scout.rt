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
package org.eclipse.scout.rt.shared.legacy.texts.services.common.text;

import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.rt.shared.services.common.text.AbstractDynamicNlsTextProviderService;

@Priority(-200.0f)
public class ScoutLegacyTextProviderService extends AbstractDynamicNlsTextProviderService {

  @Override
  protected String getDynamicNlsBaseName() {
    return "resources.texts.LegacyScoutTexts";//$NON-NLS-1$
  }
}
