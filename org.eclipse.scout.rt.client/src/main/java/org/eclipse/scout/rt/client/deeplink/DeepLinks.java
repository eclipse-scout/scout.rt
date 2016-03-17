package org.eclipse.scout.rt.client.deeplink;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides access to registered deep-link handlers.
 * 
 * @see IDeepLinkHandler
 */
public class DeepLinks implements IDeepLinks {
  private static final Logger LOG = LoggerFactory.getLogger(DeepLinks.class);

  private List<IDeepLinkHandler> m_handlers;

  public DeepLinks() {
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
  public boolean canHandleDeepLink(String deepLinkPath) {
    if (StringUtility.isNullOrEmpty(deepLinkPath)) {
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
  public boolean handleDeepLink(String deepLinkPath) throws DeepLinkException {
    for (IDeepLinkHandler handler : m_handlers) {
      if (handler.handle(deepLinkPath)) {
        return true;
      }
    }
    return false;
  }

}
