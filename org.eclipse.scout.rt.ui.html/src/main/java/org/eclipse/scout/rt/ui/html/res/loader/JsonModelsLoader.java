/*******************************************************************************
 * Copyright (c) 2014-2016 BSI Business Systems Integration AG.
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.resource.BinaryResources;
import org.eclipse.scout.rt.platform.util.FileUtility;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.server.commons.servlet.cache.IHttpResourceCache;
import org.eclipse.scout.rt.ui.html.res.WebContentService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads JSON files containing model configurations used in JavaScript-only / offline Scout apps.
 * <p>
 * This loader expects a file matching the pattern <i>*-macro.json</i> or <i>*-module.json</i>. These JSON file contain
 * an array of files, pointing to other <i>*.json</i> or regular <i>*.json</i> files containing model configurations for
 * individual widgets.
 * <p>
 * Example for a file <i>myproject-models.json</i>:
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
 * <b>Important:</b> All files referenced in the <code>files</code> array must be available on the classpath. Typically
 * these files are placed in the JavaScript root of your Scout project. Example:
 * <i>scr/main/js/myproject-models.json</i>.
 */
public class JsonModelsLoader extends AbstractResourceLoader {

  private static final Logger LOG = LoggerFactory.getLogger(JsonModelsLoader.class);

  private static final Pattern REGEX_PATTERN = Pattern.compile("^(.*?)(-module|-macro)?(.json)$");

  public static boolean acceptFile(String file) {
    if (StringUtility.isNullOrEmpty(file)) {
      return false;
    }
    if (!REGEX_PATTERN.matcher(file).matches()) {
      return false;
    }
    // Check if file can really be resolved
    return new JsonModelsLoader(null).getJsonFileUrl(file) != null;
  }

  public JsonModelsLoader() {
    super();
  }

  /**
   * This constructor is only used to create a "cheap" instance of this class (without cache) in the static
   * {@link #acceptFile(String)} method. This allows
   */
  private JsonModelsLoader(IHttpResourceCache cache) {
    super(cache);
  }

  @Override
  public BinaryResource loadResource(String pathInfo) throws IOException {
    JSONObject json = getJson(pathInfo);
    JSONObject output = buildModelsJson(json);
    LOG.info("Successfully loaded models JSON '" + pathInfo + "'");
    return BinaryResources.create()
        .withFilename(pathInfo)
        .withCharset(StandardCharsets.UTF_8)
        .withContentType(FileUtility.getContentTypeForExtension("json"))
        .withContent(output.toString().getBytes(StandardCharsets.UTF_8))
        .build();
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
        String modelId = jsonFragment.optString("id");
        if (StringUtility.isNullOrEmpty(modelId)) {
          modelId = toPropertyName(file);
          LOG.info("JSON model file '{}' is missing mandatory property 'id'. Using file location as ID: {}", file, modelId);
        }
        output.put(modelId, jsonFragment);
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

  protected URL getJsonFileUrl(String pathInfo) {
    Matcher matcher = REGEX_PATTERN.matcher(pathInfo);
    if (!matcher.matches()) {
      return null;
    }

    String fileNameWithoutExtension = matcher.group(1);
    String fileExtension = matcher.group(3);

    URL jsonUrl;
    if (isMacro(pathInfo)) {
      jsonUrl = BEANS.get(WebContentService.class).getWebContentResource(pathInfo);
    }
    else {
      jsonUrl = BEANS.get(WebContentService.class).getScriptSource(pathInfo);
    }
    if (jsonUrl == null) {
      jsonUrl = BEANS.get(WebContentService.class).getWebContentResource(fileNameWithoutExtension + "-macro" + fileExtension);
      if (jsonUrl == null) {
        jsonUrl = BEANS.get(WebContentService.class).getScriptSource(fileNameWithoutExtension + "-module" + fileExtension);
      }
    }
    return jsonUrl;
  }

  protected JSONObject getJson(String pathInfo) throws IOException {
    URL jsonUrl = getJsonFileUrl(pathInfo);
    if (jsonUrl == null) {
      LOG.warn("Failed to load resource {}", pathInfo);
      return null;
    }
    byte[] jsonBytes = IOUtility.readFromUrl(jsonUrl);
    String json = new String(jsonBytes, StandardCharsets.UTF_8);
    return new JSONObject(json);
  }
}
