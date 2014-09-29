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

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.shared.services.common.code.ICodeService;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;

/**
 * @deprecated will be removed in N Release
 */
@Deprecated
public interface IClusterSyncCodeService extends ICodeService {

  /**
   * reload code types without firing any client notifications
   * 
   * @since 4.0.0
   * @throws ProcessingException
   */
  List<ICodeType<?, ?>> reloadCodeTypesNoFire(List<Class<? extends ICodeType<?, ?>>> types) throws ProcessingException;

}
