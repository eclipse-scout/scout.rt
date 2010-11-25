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
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.service.IService;

@Priority(-3)
public interface ILookupService extends IService {

  /**
   * Lookup by performing a "key" filter and activating the <key> tags
   */
  LookupRow[] getDataByKey(LookupCall call) throws ProcessingException;

  /**
   * Lookup by performing a "text" filter and activating the <text> tags
   */
  LookupRow[] getDataByText(LookupCall call) throws ProcessingException;

  /**
   * Lookup by performing a "all" filter and activating the <all> tags
   */
  LookupRow[] getDataByAll(LookupCall call) throws ProcessingException;

  /**
   * Lookup by performing a "recursion" filter and activating the <rec> tags
   */
  LookupRow[] getDataByRec(LookupCall call) throws ProcessingException;
}
