/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.deeplink;

import java.util.regex.Matcher;

import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.desktop.BrowserHistoryEntry;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Deep-link handler for outlines.
 * <ul>
 * <li>Format: <code>outline-[outlineId]</code>, Outline ID is checksum generated from the absolute class name</li>
 * <li>Example: <code>outline-1234567</code></li>
 * </ul>
 */
@Order(1000)
public class OutlineDeepLinkHandler extends AbstractDeepLinkHandler {

  private static final String HANDLER_NAME = "outline";

  public OutlineDeepLinkHandler() {
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

  /**
   * @param outline
   * @param startup Set to true on startup while the default view is activated. Hides the
   *                path in the URL when set to true.
   * @return
   */
  public BrowserHistoryEntry createBrowserHistoryEntry(IOutline outline, boolean startup) {
    return DeepLinkUriBuilder.createRelative()
        .parameterPath(toDeepLinkPath(outlineId(outline)))
        .parameterInfo(outline.getTitle())
        .pathVisible(!startup)
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
