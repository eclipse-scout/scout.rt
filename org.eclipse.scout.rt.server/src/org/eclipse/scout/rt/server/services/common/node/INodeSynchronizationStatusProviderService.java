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
package org.eclipse.scout.rt.server.services.common.node;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.shared.services.common.node.NodeServiceStatus;
import org.eclipse.scout.service.IService;

/**
 *
 */
public interface INodeSynchronizationStatusProviderService extends IService {

  NodeServiceStatus getClusterStatus() throws ProcessingException;

  boolean reloadClusterCache(String resourceName) throws ProcessingException;
}
