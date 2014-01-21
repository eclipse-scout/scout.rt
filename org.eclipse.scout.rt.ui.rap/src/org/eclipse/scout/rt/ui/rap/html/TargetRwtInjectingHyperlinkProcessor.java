package org.eclipse.scout.rt.ui.rap.html;

import java.util.Map;
import java.util.Map.Entry;

public class TargetRwtInjectingHyperlinkProcessor implements IHyperlinkProcessor {
  private Map<String, String> m_additionalParams;

  public TargetRwtInjectingHyperlinkProcessor(Map<String, String> additionalParams) {
    m_additionalParams = additionalParams;
  }

  public TargetRwtInjectingHyperlinkProcessor() {
  }

  public Map<String, String> getAdditionalParams() {
    return m_additionalParams;
  }

  public void setAdditionalParams(Map<String, String> additionalParams) {
    m_additionalParams = additionalParams;
  }

  @Override
  public String processUrl(String url, boolean local) {
    if (url == null) {
      return "";
    }
    url = adjustUrl(url, getAdditionalParams());
    return url;
  }

  @Override
  public String processTarget(String target, boolean local) {
    return "_rwt";
  }

  protected String adjustUrl(String url, Map<String, String> additionalParams) {
    if (additionalParams == null) {
      return url;
    }

    String paramChar = "?";
    if (url.contains("?") || url.contains("&#63;")) {
      paramChar = "&amp;";
    }
    for (Entry<String, String> entry : additionalParams.entrySet()) {
      url += paramChar + entry.getKey() + "=" + entry.getValue();
      paramChar = "&amp;";
    }
    return url;
  }

}
