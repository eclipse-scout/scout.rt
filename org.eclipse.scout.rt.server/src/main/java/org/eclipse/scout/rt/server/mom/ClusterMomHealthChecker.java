/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.server.mom;

import org.eclipse.scout.rt.mom.api.ClusterMom;
import org.eclipse.scout.rt.mom.api.IMomTransport;

/**
 * @since 6.1
 */
public class ClusterMomHealthChecker extends AbstractMomHealthChecker {

  @Override
  protected Class<? extends IMomTransport> getConfiguredMomClass() {
    return ClusterMom.class;
  }

}
