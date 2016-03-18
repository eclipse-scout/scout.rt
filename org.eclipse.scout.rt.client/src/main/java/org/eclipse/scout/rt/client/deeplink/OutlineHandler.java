package org.eclipse.scout.rt.client.deeplink;

import java.util.regex.Matcher;

import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.desktop.BrowserHistoryEntry;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.StringUtility;

@Order(1000)
public class OutlineHandler extends AbstractDeepLinkHandler {

  private static final String HANDLER_NAME = "outline";

  public OutlineHandler() {
    super(defaultPattern(HANDLER_NAME, "\\d+"));
  }

  @Override
  public void handleImpl(Matcher matcher) throws DeepLinkException {
    String outlineId = matcher.group(1);
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
      throw new DeepLinkException("No outline with ID " + outlineId + " found");
    }
    if (!selectedOutline.isVisible() || !selectedOutline.isEnabled()) {
      throw new DeepLinkException("Outline ID " + outlineId + " is not enabled or visible");
    }
    desktop.activateOutline(selectedOutline);
  }

  public BrowserHistoryEntry createBrowserHistoryEntry(IOutline outline) {
    return DeepLinkUriBuilder.createRelative()
        .parameterInfo(outline.getTitle())
        .parameterPath(toDeepLinkPath(outlineId(outline)))
        .createBrowserHistoryEntry();
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
