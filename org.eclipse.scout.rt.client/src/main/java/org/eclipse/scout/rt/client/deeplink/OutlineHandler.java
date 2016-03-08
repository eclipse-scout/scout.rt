package org.eclipse.scout.rt.client.deeplink;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// FIXME awe: (deep-links) ausprobieren ob wir die angezeigte URL im browser ändern können wenn wir die outline
// in der applikation wechseln. Andernfalls überlegen ob wir die URL zur Outline im Share Menü öffnen können
@Order(1000)
public class OutlineHandler extends AbstractDeepLinkHandler {

  private static final Logger LOG = LoggerFactory.getLogger(OutlineHandler.class);

  public OutlineHandler() {
    super(Pattern.compile("^outline/(\\d+)/(.*)$"));
  }

  @Override
  public void handleImpl(Matcher matcher) throws DeepLinkException {
    String outlineId = matcher.group(1);
    String outlineName = matcher.group(2);
    LOG.info("Handling deep-link request for outline id=" + outlineId + " name=" + outlineName);

    IOutline selectedOutline = null;
    IDesktop desktop = ClientSessionProvider.currentSession().getDesktop();
    for (IOutline outline : desktop.getAvailableOutlines()) {
      String tmpOutlineId = outlineId(outline);
      if (tmpOutlineId.equals(outlineId)) {
        selectedOutline = outline;
        break;
      }
    }

    if (selectedOutline == null) {
      throw new DeepLinkException();
    }

    if (!selectedOutline.isVisible() || !selectedOutline.isEnabled()) {
      throw new DeepLinkException();
    }

    desktop.activateOutline(selectedOutline);
    LOG.info("Activate outline " + selectedOutline);
  }

  // FIXME awe: (deep-links) Schauen was wir von processAppLink in CRM verwenden können
  // -> für Forms in CRM gleiches konzept wie in ClientDomain#processAppLink verwenden /view/domain/*
  // -> IOutline#getOutlineId -> anschauen ob man das handling von forms und outlines in CRM irgendwie vereinheitlichen kann
  private String outlineId(IOutline outline) {
    int nameChecksum = fletcher16(outline.getClass().getName());
    nameChecksum = Math.abs(nameChecksum);
    String outlineId = StringUtility.lpad(String.valueOf(nameChecksum), "0", 5);
    System.out.println(outlineId + " " + outline.getClass().getSimpleName());
    return outlineId;
  }

  private static short fletcher16(String data) {
    short sum1 = 0;
    short sum2 = 0;
    short modulus = 255;

    for (int i = 0; i < data.length(); i++) {
      sum1 = (short) ((sum1 + data.charAt(i)) % modulus);
      sum2 = (short) ((sum2 + sum1) % modulus);
    }
    return (short) ((sum2 << 8) | sum1);
  }

}
