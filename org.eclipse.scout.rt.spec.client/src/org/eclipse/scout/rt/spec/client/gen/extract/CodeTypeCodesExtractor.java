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

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.code.CODES;
import org.eclipse.scout.rt.shared.services.common.code.ICode;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;

/**
 * An {@link IDocTextExtractor} with the fully qualified class name as text.
 */
public class CodeTypeCodesExtractor extends AbstractNamedTextExtractor<Class<?>> {

  public CodeTypeCodesExtractor() {
    super(TEXTS.get("org.eclipse.scout.rt.spec.protectedCodes"));
  }

  /**
   * @return fully qualified class name.
   */
  @SuppressWarnings("unchecked")
  @Override
  public String getText(Class<?> c) {
    SimpleTypeTextExtractor<Class<?>> fallBackExtractor = new SimpleTypeTextExtractor<Class<?>>();
    if (!ICodeType.class.isAssignableFrom(c)) {
      // TODO ASA scout 4.0: can generic param be further typed Class<? extends ICodeType>?
      return null;
    }
    ArrayList<String> codes = new ArrayList<String>();
    for (Class innerClass : c.getDeclaredClasses()) {
      if (ICode.class.isAssignableFrom(innerClass)) {
        ICode code = CODES.getCode((Class<ICode>) innerClass);
        String codeText = (code != null) ? code.getText() : null;
        codes.add(StringUtility.nvl(codeText, fallBackExtractor.getText(innerClass)));
      }
    }
    return CollectionUtility.format(codes);
  }

}
