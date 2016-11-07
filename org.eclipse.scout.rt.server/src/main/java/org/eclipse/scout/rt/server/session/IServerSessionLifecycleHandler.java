package org.eclipse.scout.rt.server.session;

import org.eclipse.scout.rt.server.IServerSession;

/**
 * Handler for creating and destroying server sessions.
 */
public interface IServerSessionLifecycleHandler {

  String getId();

  IServerSession create();

  void destroy(IServerSession session);

}
