package org.eclipse.scout.rt.ui.html.res.loader;

import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.TreeMap;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.nls.NlsResourceBundle;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.resource.BinaryResources;
import org.eclipse.scout.rt.platform.util.FileUtility;
import org.eclipse.scout.rt.shared.services.common.text.AbstractDynamicNlsTextProviderService;
import org.eclipse.scout.rt.ui.html.res.loader.AbstractResourceLoader;
import org.json.JSONObject;

public class TextsLoader extends AbstractResourceLoader {
  private Map<String, Map<String, String>> m_textsByLanguageTag = new LinkedHashMap<String, Map<String, String>>();

  @Override
  public BinaryResource loadResource(String pathInfo) throws IOException {
    String[] languageTags = {null, "de", "de-CH"}; //FIXME CGU where to define this?
    JSONObject jsonTexts = new JSONObject();

    // Gather all texts and group them by language tags
    for (AbstractDynamicNlsTextProviderService textService : BEANS.all(AbstractDynamicNlsTextProviderService.class)) {
      for (String tag : languageTags) {
        NlsResourceBundle bundle = getResourceBundle(textService, tag);
        if (bundle == null) {
          continue;
        }

        Map<String, String> map = m_textsByLanguageTag.get(tag);
        if (map == null) {
          map = new TreeMap<String, String>();
          m_textsByLanguageTag.put(tag, map);
        }
        putTextsFromBundle(bundle, map);
      }
    }

    // Convert the texts into json
    for (Entry<String, Map<String, String>> entry : m_textsByLanguageTag.entrySet()) {
      String languageTag = entry.getKey();
      JSONObject jsonTextMap = textsToJson(languageTag, entry.getValue());
      if (languageTag == null) {
        languageTag = "default";
      }
      jsonTexts.put(languageTag, jsonTextMap);
    }

    // Create a binary resource
    byte[] content = jsonTexts.toString(2).getBytes();
    return BinaryResources.create()
        .withFilename("texts.json")
        .withContentType(FileUtility.getContentTypeForExtension("json"))
        .withContent(content)
        .build();
  }

  protected Map<String, String> putTextsFromBundle(ResourceBundle bundle, Map<String, String> textMap) {
    for (Enumeration<String> en = bundle.getKeys(); en.hasMoreElements();) {
      String key = en.nextElement();
      String text = bundle.getString(key);
      if (!textMap.containsKey(key)) {
        textMap.put(key, text);
      }
    }
    return textMap;
  }

  protected JSONObject textsToJson(String languageTag, Map<String, String> textMap) {
    JSONObject texts = new JSONObject();
    for (Entry<String, String> entry : textMap.entrySet()) {
      texts.put(entry.getKey(), entry.getValue());
    }
    return texts;
  }

  protected static NlsResourceBundle getResourceBundle(AbstractDynamicNlsTextProviderService textService, String languageTag) throws IOException {
    String suffix = "";
    if (languageTag != null) {
      // The text property files work with '_' instead of '-' -> convert them.
      suffix = "_" + languageTag.replace("-", "_");
    }
    return NlsResourceBundle.getBundle(textService.getDynamicNlsBaseName(), suffix, textService.getClass().getClassLoader());
  }

}
