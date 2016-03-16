package org.eclipse.scout.rt.client.deeplink;

import java.util.regex.Matcher;

import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.desktop.BrowserHistory;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.UriBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Order(1000)
public class OutlineHandler extends AbstractDeepLinkHandler {

  private static final String HANDLER_NAME = "outline";

  private static final Logger LOG = LoggerFactory.getLogger(OutlineHandler.class);

  public OutlineHandler() {
    super(defaultPattern(HANDLER_NAME, "\\d+"));
  }

  @Override
  public void handleImpl(Matcher matcher) throws DeepLinkException {
    String outlineId = matcher.group(1);
    LOG.info("Handling deep-link request for outline id=" + outlineId);

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

  public BrowserHistory createBrowserHistory(IDesktop desktop, IOutline outline) {
    UriBuilder uri = new UriBuilder("./");
    if (StringUtility.hasText(outline.getTitle())) {
      uri.parameter(IDeepLinks.PARAM_NAME_INFO, toSlug(outline.getTitle()));
    }
    uri.parameter(IDeepLinks.PARAM_NAME_DEEP_LINK, toParameter(outlineId(outline)));
    String historyTitle = desktop.getTitle() + " - " + outline.getTitle();
    return new BrowserHistory(uri.createURI().toString(), historyTitle);
  }

  private static String outlineId(IOutline outline) {
    int nameChecksum = fletcher16(outline);
    nameChecksum = Math.abs(nameChecksum);
    return StringUtility.lpad(String.valueOf(nameChecksum), "0", 5);
  }

  private static short fletcher16(IOutline outline) {
    short sum1 = 0;
    short sum2 = 0;
    short modulus = 255;
    String data = outline.getClass().getName();

    for (int i = 0; i < data.length(); i++) {
      sum1 = (short) ((sum1 + data.charAt(i)) % modulus);
      sum2 = (short) ((sum2 + sum1) % modulus);
    }
    return (short) ((sum2 << 8) | sum1);
  }

  @Override
  public String getName() {
    return HANDLER_NAME;
  }

}
