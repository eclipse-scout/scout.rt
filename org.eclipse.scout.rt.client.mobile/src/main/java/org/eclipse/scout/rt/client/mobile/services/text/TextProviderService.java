/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.mobile.services.text;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.rt.shared.services.common.text.AbstractDynamicNlsTextProviderService;

@Order(25)
public class TextProviderService extends AbstractDynamicNlsTextProviderService {

  @Override
  protected String getDynamicNlsBaseName() {
    return "org.eclipse.scout.rt.client.mobile.texts.Texts";
  }

}
