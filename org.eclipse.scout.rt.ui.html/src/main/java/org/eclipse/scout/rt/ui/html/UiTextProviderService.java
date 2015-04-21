package org.eclipse.scout.rt.ui.html;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.rt.shared.services.common.text.AbstractDynamicNlsTextProviderService;

/**
 * Text provider service for text used in Html UI plugin.
 * <p>
 * Note: Texts that are required on the UI (javascript) have to be sent to the browser beforehand, by adding the
 * corresponding keys to {@link UiSession#getTextKeys()}.
 */
@Order(10)
public class UiTextProviderService extends AbstractDynamicNlsTextProviderService {

  @Override
  protected String getDynamicNlsBaseName() {
    return "org.eclipse.scout.rt.ui.html.texts.Texts";
  }
}
