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
package org.eclipse.scout.rt.shared.services.lookup;

import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.service.AbstractService;

/**
 * @since 3.8.1
 */
@Priority(-1)
public class DefaultCodeLookupCallFactoryService extends AbstractService implements ICodeLookupCallFactoryService {

  @Override
  public CodeLookupCall newInstance(Class<? extends ICodeType> codeTypeClass) {
    return new CodeLookupCall(codeTypeClass);
  }

}
