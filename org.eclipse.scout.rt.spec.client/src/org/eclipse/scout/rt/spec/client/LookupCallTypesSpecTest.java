/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.spec.client;

import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;

/**
 * implementation of {@link AbstractTypeSpecTest} for lookup calls
 */
public class LookupCallTypesSpecTest extends AbstractTypeSpecTest {

  public LookupCallTypesSpecTest() {
    super("org.eclipse.scout.rt.spec.lookupcalltypes", TEXTS.get("org.eclipse.scout.rt.spec.lookupcalltypes"), LookupCall.class);
  }
}
