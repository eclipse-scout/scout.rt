/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.server.mom;

import org.eclipse.scout.rt.mom.api.IDestination;
import org.eclipse.scout.rt.mom.api.MOM;
import org.eclipse.scout.rt.platform.config.AbstractConfigProperty;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.server.services.common.clustersync.IClusterNotificationMessage;

/**
 * Definition of destinations used for messaging over {@link MOM}.
 *
 * @since 6.1
 */
public interface IMomDestinations {

  /**
   * Topic to transfer cluster notifications.
   */
  IDestination<IClusterNotificationMessage> CLUSTER_NOTIFICATION_TOPIC = CONFIG.getPropertyValue(ClusterSyncTopic.class);

  /**
   * Specifies the JNDI name to lookup the topic to publish and consume 'cluster notifications'.
   */
  class ClusterSyncTopic extends AbstractConfigProperty<IDestination<IClusterNotificationMessage>> {

    @Override
    public String getKey() {
      return "scout.mom.topic.clusterNotificationTopic";
    }

    @Override
    protected IDestination<IClusterNotificationMessage> parse(String jndiName) {
      return MOM.newDestination(jndiName, IDestination.JNDI_LOOKUP);
    }
  }
}
