package org.eclipse.scout.rt.ui.html.deeplink;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OutlineHandler extends AbstractDeepLinkHandler {

  private static final Logger LOG = LoggerFactory.getLogger(OutlineHandler.class);

  protected OutlineHandler() {
    super(Pattern.compile("^outline/(\\d+)/(.*)$"));
  }

  @Override
  public void handleImpl(Matcher matcher, IClientSession clientSession) throws DeepLinkException {
    String outlineId = matcher.group(1);
    String outlineName = matcher.group(2);
    LOG.info("Handling deep-link request for outline id=" + outlineId + " name=" + outlineName);

    IOutline selectedOutline = null;
    IDesktop desktop = clientSession.getDesktop();
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

  // FIXME AWE: mit J.GU diskutieren - schöne, kurze nümmerli generieren oder einfach den simple class name nehmen?
  // ich finde die nümmerli noch schön, gerade weil man sie als mensch beim lesen einfach ignoriert und eher den i18n text
  // betrachtet
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
