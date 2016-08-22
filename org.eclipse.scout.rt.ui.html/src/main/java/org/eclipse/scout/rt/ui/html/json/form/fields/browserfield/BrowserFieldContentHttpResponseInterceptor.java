package org.eclipse.scout.rt.ui.html.json.form.fields.browserfield;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.server.commons.servlet.ContentSecurityPolicy;
import org.eclipse.scout.rt.server.commons.servlet.HttpServletControl;
import org.eclipse.scout.rt.server.commons.servlet.cache.IHttpResponseInterceptor;

public class BrowserFieldContentHttpResponseInterceptor implements IHttpResponseInterceptor {
  private static final long serialVersionUID = 1L;

  @Override
  public void intercept(HttpServletRequest req, HttpServletResponse resp) {
    String cspToken = BEANS.get(ContentSecurityPolicy.class)
        .withScriptSrc("'self' 'unsafe-inline'")
        .toToken();
    resp.setHeader(HttpServletControl.HTTP_HEADER_CSP, cspToken);
    resp.setHeader(HttpServletControl.HTTP_HEADER_CSP_LEGACY, cspToken);
  }
}
