/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.res.loader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.resource.BinaryResources;
import org.eclipse.scout.rt.ui.html.UiThemeUtility;
import org.eclipse.scout.rt.ui.html.cache.HttpCacheKey;
import org.eclipse.scout.rt.ui.html.cache.HttpCacheObject;
import org.eclipse.scout.rt.ui.html.cache.IHttpCacheControl;
import org.eclipse.scout.rt.ui.html.res.IWebContentService;
import org.eclipse.scout.rt.ui.html.script.ScriptFileBuilder;
import org.eclipse.scout.rt.ui.html.script.ScriptOutput;
import org.eclipse.scout.rt.ui.html.script.ScriptSource.FileType;
import org.eclipse.scout.rt.ui.html.scriptprocessor.ScriptProcessor;

/**
 * This class loads and parses CSS and JS files from WebContent/ folder.
 */
public class ScriptFileLoader extends AbstractResourceLoader {

  private ScriptProcessor m_scriptProcessor;

  public ScriptFileLoader(HttpServletRequest req, ScriptProcessor scriptProcessor) {
    super(req);
    m_scriptProcessor = scriptProcessor;
  }

  @Override
  public HttpCacheKey createCacheKey(String resourcePath, Locale locale) {
    if (FileType.JS == FileType.resolveFromFilename(resourcePath)) {
      // JavaScript files are always the same, no matter what the theme or the locale is
      return new HttpCacheKey(resourcePath);
    }
    else {
      // CSS files are different for depending on the current theme (but don't depend on the locale)
      Object[] cacheAttributes = new Object[]{UiThemeUtility.getThemeForLookup(getRequest())};
      return new HttpCacheKey(resourcePath, null, cacheAttributes);
    }
  }

  @Override
  public HttpCacheObject loadResource(HttpCacheKey cacheKey) throws IOException {
    ScriptFileBuilder builder = new ScriptFileBuilder(BEANS.get(IWebContentService.class), m_scriptProcessor);
    builder.setMinifyEnabled(isMinify());
    builder.setTheme(getTheme(cacheKey));
    String resourcePath = cacheKey.getResourcePath();
    ScriptOutput out = builder.buildScript(resourcePath);
    if (out != null) {
      BinaryResource content = BinaryResources.create()
          .withFilename(out.getPathInfo())
          .withCharset(StandardCharsets.UTF_8.name())
          .withContent(out.getContent())
          .withLastModified(out.getLastModified())
          .build();

      return new HttpCacheObject(cacheKey, true, IHttpCacheControl.MAX_AGE_ONE_YEAR, content);
    }
    return null;
  }

  protected String getTheme(HttpCacheKey cacheKey) {
    Object[] cacheAttributes = cacheKey.getCacheAttributes();
    if (cacheAttributes != null && cacheAttributes.length > 0) {
      // TODO [5.2] awe: Can we find a better way to retrieve the theme than "knowing" that the first element will be the theme? Maybe a map? (BSH)
      return UiThemeUtility.getThemeName((String) cacheAttributes[0]);
    }
    return null;
  }
}
