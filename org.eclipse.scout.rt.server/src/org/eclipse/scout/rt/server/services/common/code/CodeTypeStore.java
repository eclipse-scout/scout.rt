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
package org.eclipse.scout.rt.server.services.common.code;

/**
 * Title: BSI Scout V3
 *  Copyright (c) 2001,2009 BSI AG
 * @version 3.x
 */

import java.util.Locale;
import java.util.Map;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.ServerJob;
import org.eclipse.scout.rt.shared.services.common.code.CodeTypeCache;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;

/**
 * CodeType store in servlet context for global code providing to http sessions
 * Maintains a map of partition- and language-dependent code type caches
 *
 * @deprecated use {@link org.eclipse.scout.rt.shared.services.common.code.CodeTypeStore} instead.
 *             will be removed with the N-Release
 */
@Deprecated
public class CodeTypeStore extends org.eclipse.scout.rt.shared.services.common.code.CodeTypeStore {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(CodeTypeStore.class);

  public CodeTypeCache getCodeTypeCache(Locale locale) {
    Long partitionId = 0L;
    Map<String, Object> sharedVariableMap = ServerJob.getCurrentSession().getSharedVariableMap();
    if (sharedVariableMap.containsKey(ICodeType.PROP_PARTITION_ID)) {
      partitionId = (Long) sharedVariableMap.get(ICodeType.PROP_PARTITION_ID);
    }
    return getCodeTypeCache(partitionId, locale);
  }
}
