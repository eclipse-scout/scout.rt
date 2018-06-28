#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.api;

import org.eclipse.scout.rt.platform.IPlatform.State;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.server.AbstractServerSession;
import org.eclipse.scout.rt.server.session.ServerSessionProvider;

public class ServerSession extends AbstractServerSession {

  private static final long serialVersionUID = 1L;

  public ServerSession() {
    super(true);
  }

  /**
   * @return The {@link ServerSession} which is associated with the current thread, or <code>null</code> if not found.
   */
  public static ServerSession get() {
    return ServerSessionProvider.currentSession(ServerSession.class);
  }

  @Override
  protected void execLoadSession() {
    Assertions.assertEquals(State.PlatformStarted, Platform.get().getState(), "Expected the platform to be started, but state is {}", Platform.get().getState());
    super.execLoadSession();
  }
}
