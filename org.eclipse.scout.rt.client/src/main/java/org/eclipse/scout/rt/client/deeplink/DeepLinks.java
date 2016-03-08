package org.eclipse.scout.rt.client.deeplink;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.platform.BEANS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles GET requests having an URL starting with /view/.
 */
// FIXME awe: (deep-links) versuchen nach deep-link redirect auf / zu machen, sonst bleibt die URL in der Location bar stehen
public class DeepLinks implements IDeepLinks {
  private static final Logger LOG = LoggerFactory.getLogger(DeepLinks.class);

  private static Pattern DEEP_LINK_REGEX = Pattern.compile("^/view/(.*)$");

  private List<IDeepLinkHandler> m_handlers;

  public DeepLinks() {
    // FIXME AWE: (deep-links) mit A.BR besprechen - ich finde es ungünstig, dass DeepLinks mehrfach instanziert wird
    // wenn man als entwickler etwas mit Singleton charakter erwartet ist es gefährlich, wenn der Ctor mehrfach aufgerufen
    // wird. Auch der ctor kann Seiteneffekte haben. Vergleiche @Singleton annotation von Google Guice.
    m_handlers = new ArrayList<>(BEANS.all(IDeepLinkHandler.class));
    if (LOG.isInfoEnabled()) {
      StringBuilder sb = new StringBuilder();
      for (IDeepLinkHandler handler : m_handlers) {
        sb.append("\n- ").append(handler);
      }
      LOG.info("Registered {} deep-link handlers:{}", m_handlers.size(), sb.toString());
    }
  }

  @Override
  public boolean isRequestValid(String path) {
    String deepLinkPath = getDeepLinkPath(path);
    if (deepLinkPath == null) {
      return false;
    }

    for (IDeepLinkHandler handler : m_handlers) {
      if (handler.matches(deepLinkPath)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean handleRequest(String path) {
    String deepLinkPath = getDeepLinkPath(path);
    if (deepLinkPath == null) {
      throw new IllegalArgumentException("Called handleRequest but path is not a valid deep-link: " + path);
    }

    for (IDeepLinkHandler handler : m_handlers) {
      try {
        if (handler.handle(deepLinkPath)) {
          return true;
        }
      }
      catch (DeepLinkException e) {
        // FIXME awe: (deep-links) mit J.GU besprechen: wie machen wir error-handling? Vorschlag:
        // - beim validieren (GET) -> 404
        // - beim handle (POST/Startup) -> message-box o.ä.
        // clientSession.getDesktop().showMessageBox(messageBox); --> messagebox
        // -> so machen
        // im fehlerfall müssen wir aber zuerst open-deskop (default) machen... und _dann_ erst
        // die message-box anzeigen, das ist etwas komplizierter
        LOG.warn("Failed to handle deep-link", e);
      }
    }

    return false;
  }

  /**
   * @return The deep-link path without the deep-link prefix. Example path '/view/outline/123' will return
   *         'outline/123'.
   */
  protected String getDeepLinkPath(String path) {
    if (path == null) {
      return null;
    }

    Matcher matcher = DEEP_LINK_REGEX.matcher(path);
    if (!matcher.matches()) {
      return null;
    }

    return matcher.group(1);
  }

}
