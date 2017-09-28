/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.mom;

import org.eclipse.scout.rt.mom.api.AbstractDestinationConfigProperty;
import org.eclipse.scout.rt.mom.api.ClusterMom;
import org.eclipse.scout.rt.mom.api.IBiDestination;
import org.eclipse.scout.rt.mom.api.IDestination;
import org.eclipse.scout.rt.mom.api.IDestination.DestinationType;
import org.eclipse.scout.rt.mom.api.IDestination.IDestinationType;
import org.eclipse.scout.rt.mom.api.IDestination.ResolveMethod;
import org.eclipse.scout.rt.mom.api.IMomDestinations;
import org.eclipse.scout.rt.mom.api.MOM;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.server.services.common.clustersync.IClusterNotificationMessage;

/**
 * Definition of destinations used for messaging over {@link ClusterMom}.
 *
 * @since 6.1
 */
public interface IClusterMomDestinations extends IMomDestinations {

  /**
   * Topic to transfer cluster notifications, or <code>null</code> if not configured in <i>'config.properties'</i>.
   */
  IDestination<IClusterNotificationMessage> CLUSTER_NOTIFICATION_TOPIC = CONFIG.getPropertyValue(ClusterSyncTopic.class);

  /**
   * Replace this bean to change the defaults for <i>all</i> destination properties inside {@link ClusterMom}. Override
   * the corresponding {@link AbstractDestinationConfigProperty} bean to change the defaults for a specific destination.
   */
  @ApplicationScoped
  class ClusterMomDestinationDefaults {

    public <DTO> IDestination<DTO> newDestination(String name, IDestinationType type) {
      return MOM.newDestination(name, type, ResolveMethod.DEFINE, null);
    }

    public <REQUEST, REPLY> IBiDestination<REQUEST, REPLY> newBiDestination(String name, IDestinationType type) {
      return MOM.newBiDestination(name, type, ResolveMethod.DEFINE, null);
    }
  }

  class ClusterSyncTopic extends AbstractDestinationConfigProperty<IClusterNotificationMessage> {

    @Override
    public String getKey() {
      return "scout.mom.cluster.destination.clusterNotificationTopic";
    }

    @Override
    protected IDestinationType getType() {
      return DestinationType.TOPIC;
    }
  }
}
