package org.eclipse.scout.rt.ui.html.deeplink;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.client.IClientSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles GET requests having an URL starting with /view/.
 */
// FIXME AWE: mit J.GU besprechen... sollte wahrscheinlich ein singleton / bean sein?

// FIXME AWE: mit J.GU besprechen... wie sehr soll Scout eine Web-applikation sein?
// Wir könnten die deep-link URL nämlich einfach dem deskopOpened event als parameter
// mitgeben. So könnte man die ganze deep-link logik im Desktop handeln. Gut wäre es,
// wenn man die deep-link info beim Desktop#execOpened() zur Verfügung hätte, sonst
// wird nämlich zuerst das execOpened ausgeführt und dann noch die deep-link logik.

// FIXME AWE: mit J.GU besprechen... nach deep-link einen redirect machen?
// sonst bleibt die URL in der Location bar stehen

public class DeepLinks {
  private static final Logger LOG = LoggerFactory.getLogger(DeepLinks.class);

  // FIXME AWE: mit J.GU besprechen... präfix für deep links?
  private static Pattern DEEP_LINK_REGEX = Pattern.compile("^/view/(.*)$");

  private List<IDeepLinkHandler> m_handlers;

  public DeepLinks() {
    m_handlers = new ArrayList<>();
    m_handlers.add(new OutlineHandler());
  }

  /**
   * @return True if the given path is a valid deep-link request (only syntax is checked at this point).
   */
  public boolean isRequestValid(String path) {
    if (path == null) {
      return false;
    }
    Matcher matcher = DEEP_LINK_REGEX.matcher(path);
    if (!matcher.matches()) {
      return false;
    }
    String deepLinkPath = matcher.group(1);
    for (IDeepLinkHandler handler : m_handlers) {
      if (handler.matches(deepLinkPath)) {
        return true;
      }
    }
    return false;
  }

  // FIXME AWE: mit J.GU besprechen: wie soll ein Projekt deepLinksHandler ergänzen / austauschen?
  // wahrscheinlich machen wir die handlers einfach zu beans? Es braucht auch eine order.
  public void handleDeepLink(String path, IClientSession clientSession) {
    Matcher matcher = DEEP_LINK_REGEX.matcher(path);
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid path " + path);
    }
    String deepLinkPath = matcher.group(1);
    for (IDeepLinkHandler handler : m_handlers) {
      try {
        if (handler.handle(deepLinkPath, clientSession)) {
          break;
        }
      }
      catch (DeepLinkException e) {
        // FIXME AWE: mit J.GU besprechen: wie machen wir error-handling? Vorschlag:
        // - beim validieren (GET) -> 404
        // - beim handle (POST/Startup) -> message-box o.ä.
        // clientSession.getDesktop().showMessageBox(messageBox);
        LOG.warn("Failed to handle deep-link", e);
      }
    }
  }

}
