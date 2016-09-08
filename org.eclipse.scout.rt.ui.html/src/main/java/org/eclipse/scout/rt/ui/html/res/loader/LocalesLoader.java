package org.eclipse.scout.rt.ui.html.res.loader;

import java.io.IOException;
import java.util.Locale;

import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.resource.BinaryResources;
import org.eclipse.scout.rt.platform.util.FileUtility;
import org.eclipse.scout.rt.ui.html.json.JsonLocale;
import org.eclipse.scout.rt.ui.html.res.loader.AbstractResourceLoader;
import org.json.JSONArray;
import org.json.JSONObject;

public class LocalesLoader extends AbstractResourceLoader {

  @Override
  public BinaryResource loadResource(String pathInfo) throws IOException {
    String[] languageTags = {"de-CH"}; //FIXME CGU where to define this?

    JSONArray jsonLocales = new JSONArray();
    for (String tag : languageTags) {
      jsonLocales.put(jsonLocale(tag));
    }

    byte[] localeBytes = jsonLocales.toString(2).getBytes();
    return BinaryResources.create()
        .withFilename("locales.json")
        .withContentType(FileUtility.getContentTypeForExtension("json"))
        .withContent(localeBytes)
        .build();
  }

  protected JSONObject jsonLocale(String languageTag) {
    Locale locale = Locale.forLanguageTag(languageTag);
    return JsonLocale.toJson(locale);
  }

}
