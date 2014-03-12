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

import java.util.ArrayList;

import org.eclipse.scout.commons.ListUtility;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCode;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;

/**
 * An {@link IDocTextExtractor} with the fully qualified class name as text.
 */
public class CodeTypeCodesExtractor extends AbstractNamedTextExtractor<Class> {

  public CodeTypeCodesExtractor() {
    super(TEXTS.get("org.eclipse.scout.rt.spec.protectedCodes"));
  }

  /**
   * @return fully qualified class name.
   */
  @Override
  public String getText(Class c) {
    if (!ICodeType.class.isAssignableFrom(c)) {
      // TODO ASA scout 4.0: can generic param be further typed Class<? extends ICodeType>?
      return null;
    }
    ArrayList<String> codes = new ArrayList<String>();
    for (Class innerClass : c.getDeclaredClasses()) {
      if (AbstractCode.class.isAssignableFrom(innerClass)) {
        codes.add(innerClass.getSimpleName());
      }
    }
    return ListUtility.format(codes);
  }

}
