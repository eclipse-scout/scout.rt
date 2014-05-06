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
package org.eclipse.scout.rt.spec.client.gen.extract;

import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.code.CODES;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;

public class CodeTypeNameExtractor extends AbstractNamedTextExtractor<Class<?>> {

  public CodeTypeNameExtractor() {
    super(TEXTS.get("org.eclipse.scout.rt.spec.type"));
  }

  @SuppressWarnings("unchecked")
  @Override
  public String getText(Class c) {
    if (!ICodeType.class.isAssignableFrom(c)) {
      // TODO ASA scout 4.0: can generic param be further typed Class<? extends ICodeType>?
      return null;
    }
    return CODES.getCodeType((Class<ICodeType<?,?>>) c).getText();
  }
}
