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
package org.eclipse.scout.rt.server.services.lookup;

import org.eclipse.scout.commons.annotations.Priority;

/**
 * @deprecated Use {@link org.eclipse.scout.rt.shared.services.lookup.BatchLookupService} instead. Will be removed with
 *             the N release.
 */
@Deprecated
@Priority(-1)
public class BatchLookupService extends org.eclipse.scout.rt.shared.services.lookup.BatchLookupService {

  public BatchLookupService() {
  }
}
