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
// FIXME awe: (deep-links) mit J.GU und C.RU diskutieren. Müssen wir via link den Baum aufklappen können oder reicht
// das Referenzieren der Outline? Wir haben dann ja auch noch die Bookmarks. RAP hat das auch irgendwie mit Bookmarks
// gemacht. Aber dort wurde dann das Bookmark über den (i18n) Node-Text aufgelöst, was natürlich auch nicht so gut
// funktioniert in mehrsprachigen Projekten. Wenn wir das haben wollen sollten wir also den fachlichen Schlüssel ver-
// wenden, was aber Zusatzaufwand bedeutet. Ich glaube im Moment können wir ohne das leben.
// Beispiel aus RAP: https://partner.bsiag.com/int/bsicrm_14_2/web#com.bsiag.crm.client.core.desktop.PersonalOutline-
//   Eigene-Person-%28BSI%29-Firmen-BSI-BADEN-Tickets
public class DeepLinks implements IDeepLinks {
  private static final Logger LOG = LoggerFactory.getLogger(DeepLinks.class);

  private static Pattern DEEP_LINK_REGEX = Pattern.compile("^/view/(.*)$");

  private List<IDeepLinkHandler> m_handlers;

  public DeepLinks() {
    // FIXME awe: (deep-links) mit A.BR besprechen - ich finde es ungünstig, dass DeepLinks mehrfach instanziert wird
    // wenn man als entwickler etwas mit Singleton charakter erwartet ist es gefährlich, wenn der Ctor mehrfach aufgerufen
    // wird. Auch der ctor kann Seiteneffekte haben. Vergleiche @Singleton annotation von Google Guice.
    // FIXME awe: auch mit A.BR besprechen: wenn etwas mit Bean annotiert ist und auf der Klasse kein geeigneter Ctor
    // gefunden wird, bitte eine Exception werfen, nicht einfach silent verschlucken!
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
  public boolean handleRequest(String path) throws DeepLinkException {
    String deepLinkPath = getDeepLinkPath(path);
    if (deepLinkPath == null) {
      throw new IllegalArgumentException("Called handleRequest but path is not a valid deep-link: " + path);
    }

    for (IDeepLinkHandler handler : m_handlers) {
      if (handler.handle(deepLinkPath)) {
        return true;
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
