package org.eclipse.scout.rt.server.fixture;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.server.AbstractServerSession;

/**
 * A test server session with shared context variables.
 */
public class TestServerSession extends AbstractServerSession {
  private static final long serialVersionUID = 782294551137415747L;

  public TestServerSession() {
    super(true);
  }

  @Override
  protected void execLoadSession() throws ProcessingException {
    setSharedContextVariable("test", String.class, "testval");
  }
}
