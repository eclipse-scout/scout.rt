package org.eclipse.scout.rt.ui.html;

import javax.servlet.http.HttpSession;

import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonAdapterRegistry;

/**
 * Utility providing access to protected methods of {@link UiSession} - for testing purposes only!
 */
public class UiSessionTestUtility {

  public static void endRequest(UiSession uiSession) {
    uiSession.setCurrentJsonResponseInternal(uiSession.createJsonResponse());
    uiSession.httpContext().clear();
  }

  public static <M, A extends IJsonAdapter<? super M>> A newJsonAdapter(UiSession uiSession, M model, IJsonAdapter<?> parent) {
    return uiSession.newJsonAdapter(model, parent);
  }

  public static JsonAdapterRegistry getJsonAdapterRegistry(UiSession session) {
    return session.jsonAdapterRegistry();
  }

  public static HttpSession getHttpSession(UiSession session) {
    return session.sessionStore().getHttpSession();
  }
}
