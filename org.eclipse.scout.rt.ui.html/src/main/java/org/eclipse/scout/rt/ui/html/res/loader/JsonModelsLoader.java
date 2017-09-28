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

  private static final String REGEX_PATTERN = "^(.*?)(-module|-macro)?(.json)$";

  /**
   * Only file names for which a file (with '-macro' or '-module' suffix) exists are accepted.
   */
  public static boolean acceptFile(String file) {
    if (StringUtility.isNullOrEmpty(file)) {
      return false;
    }

    Pattern pattern = Pattern.compile(REGEX_PATTERN);
    Matcher matcher = pattern.matcher(file);
    if (!matcher.find()) {
      return false;
    }

    return getJsonFileUrl(file) != null;
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
          LOG.error("JSON model file '{}' is missing mandatory property 'id'. Using file location as id: {}", file, modelId);
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

  protected static boolean isModule(String file) {
    return file != null && file.endsWith("-module.json");
  }

  protected static boolean isMacro(String file) {
    return file != null && file.endsWith("-macro.json");
  }

  protected static URL getJsonFileUrl(String pathInfo) {
    Pattern pattern = Pattern.compile(REGEX_PATTERN);
    Matcher matcher = pattern.matcher(pathInfo);
    if (!matcher.find()) {
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
    return new JSONObject(new String(jsonBytes, StandardCharsets.UTF_8));
  }

}
