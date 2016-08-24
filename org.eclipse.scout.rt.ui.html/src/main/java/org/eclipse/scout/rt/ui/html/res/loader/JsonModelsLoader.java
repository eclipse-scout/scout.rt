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
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.ui.html.res.IWebContentService;
import org.eclipse.scout.rt.ui.html.res.WebContentService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads JSON files containing model-configuration used in JavaScript-only / offline Scout apps. This loader excepts a
 * file matching the pattern <em>*-models.json</em>. The JSON file contains an array of files, pointing to other
 * *-models.json or regular *.json files containing the model- configuration for individual widgets. Example for a file
 * <i>myproject-models.json</i>:
 *
 * <pre>
 * {
 *   "files": [
 *     "myproject/BarForm.json",
 *     "base-models.json"
 *   ]
 * }
 * </pre>
 *
 * <strong>Important:</strong> All files referenced in the files array must be available on the classpath. Typically
 * these files are placed in the JavaScript root of your Scout project. Example:
 * <i>scr/main/js/myproject-models.json</i>.
 */
public class JsonModelsLoader extends AbstractResourceLoader {

  private static final Logger LOG = LoggerFactory.getLogger(JsonModelsLoader.class);

  private IWebContentService m_resourceLocator = BEANS.get(WebContentService.class);

  private final String m_outputFilename;

  private static final String REGEX_PATTERN = "^.*-(module|all-macro).json$";

  private static final String DEFAULT_OUTPUT_FILENAME = "models.json";

  public JsonModelsLoader() {
    this(DEFAULT_OUTPUT_FILENAME);
  }

  public JsonModelsLoader(String outputFilename) {
    this.m_outputFilename = outputFilename;
  }

  public static boolean matchesFile(String file) {
    return file != null && file.matches(REGEX_PATTERN);
  }

  @Override
  public BinaryResource loadResource(String pathInfo) throws IOException {
    JSONObject json = getJson(pathInfo);
    if (json != null) {
      JSONObject output = buildModelsJson(json);
      LOG.info("Successfully loaded models JSON '" + m_outputFilename + "'");
      return new BinaryResource(m_outputFilename, output.toString().getBytes());
    }
    return null;
  }

  /**
   * Builds JSON model files recursively, which means a *-models.json may contain other *-model.json's in it files list.
   *
   * @return the complete models JSON
   */
  protected JSONObject buildModelsJson(JSONObject json) throws IOException {
    JSONArray files = json.getJSONArray("files");
    JSONObject output = new JSONObject();
    for (int i = 0; i < files.length(); i++) {
      String file = files.getString(i);
      JSONObject jsonFragment = getJson(file);
      if (jsonFragment == null) {
        continue;
      }
      // For modules, we don't add a JSON property for the module file-name
      if (isModule(file)) {
        jsonFragment = buildModelsJson(jsonFragment);
        Iterator<String> keysIterator = jsonFragment.keys();
        while (keysIterator.hasNext()) {
          String propertyName = keysIterator.next();
          output.put(propertyName, jsonFragment.get(propertyName));
        }
      }
      else {
        output.put(toPropertyName(file), jsonFragment);
      }
    }
    return output;
  }

  protected String toPropertyName(String file) {
    if (file.endsWith(".json")) {
      file = file.substring(0, file.length() - 5);
    }
    return file.replace('/', '.');
  }

  protected boolean isModule(String file) {
    return file != null && file.endsWith("-module.json");
  }

  protected boolean isMacro(String file) {
    return file != null && file.endsWith("-macro.json");
  }

  protected JSONObject getJson(String pathInfo) throws IOException {
    URL jsonUrl;
    if (isMacro(pathInfo)) {
      jsonUrl = m_resourceLocator.getWebContentResource(pathInfo);
    }
    else {
      jsonUrl = m_resourceLocator.getScriptSource(pathInfo);
    }
    if (jsonUrl == null) {
      LOG.warn("Failed to load resource {}", pathInfo);
      return null;
    }
    byte[] jsonBytes = IOUtility.readFromUrl(jsonUrl);
    return new JSONObject(new String(jsonBytes, StandardCharsets.UTF_8));
  }

}
