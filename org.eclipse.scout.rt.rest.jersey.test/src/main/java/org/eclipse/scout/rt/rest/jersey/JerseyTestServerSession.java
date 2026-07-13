package org.eclipse.scout.rt.rest.jersey;

import org.eclipse.scout.rt.server.AbstractServerSession;

public class JerseyTestServerSession extends AbstractServerSession {
  public JerseyTestServerSession() {
    super(true);
  }

  @Override
  public String getId() {
    return "default";
  }
}
