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

import org.eclipse.scout.rt.server.services.common.clustersync.IClusterNotification;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;

public class UnloadCodeTypeCacheClusterNotification implements IClusterNotification {
  private static final long serialVersionUID = 3498451762775759388L;

  private final List<Class<? extends ICodeType<?, ?>>> m_types;

  public UnloadCodeTypeCacheClusterNotification(List<Class<? extends ICodeType<?, ?>>> types) {
    m_types = types;
  }

  public List<Class<? extends ICodeType<?, ?>>> getTypes() {
    return m_types;
  }

  @Override
  public String toString() {
    return "UnloadCodeTypeCacheClusterNotification [m_types=" + m_types + "]";
  }
}
