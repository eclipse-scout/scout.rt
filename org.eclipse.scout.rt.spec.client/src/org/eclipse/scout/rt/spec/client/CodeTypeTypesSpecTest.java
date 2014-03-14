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
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.spec.client.config.entity.IDocEntityTableConfig;

/**
 * implementation of {@link AbstractTypeSpecTest} for lookup calls
 */
public class CodeTypeTypesSpecTest extends AbstractTypeSpecTest {

  public static final String ID = "org.eclipse.scout.rt.spec.codetypetypes";

  public CodeTypeTypesSpecTest() {
    super(ID, TEXTS.get(ID), TEXTS.getWithFallback(ID + ".introduction", null), ICodeType.class, true);
  }

  @Override
  protected IDocEntityTableConfig<Class> getEntityListConfig() {
    return getConfiguration().getCodeTypeTypesTableConfig();
  }

}
