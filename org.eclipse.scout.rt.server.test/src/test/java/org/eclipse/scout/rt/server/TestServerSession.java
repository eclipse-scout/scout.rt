package org.eclipse.scout.rt.server;

/**
 * A test server session with shared context variables. <br>
 * Has to be located in package <code>org.eclipse.scout.rt.server</code>, because ServerSessionRegistryService requires
 * .* it
 */
public class TestServerSession extends AbstractServerSession {
  private static final long serialVersionUID = 782294551137415747L;

  public TestServerSession() {
    super(true);
  }

  @Override
  protected void execLoadSession() {
    setSharedContextVariable("test", String.class, "testval");
  }
}
