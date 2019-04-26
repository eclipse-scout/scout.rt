package org.eclipse.scout.rt.ui.html.res.loader;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.util.FinalValue;
import org.eclipse.scout.rt.ui.html.res.WebResourceHelpers;

import java.util.Map;

@ApplicationScoped
public class ScriptResourceIndexes {

  private final FinalValue<Map<String, String>> m_index = new FinalValue<>();

  public static String getExternalForm(String path, boolean minified) {
    return BEANS.get(ScriptResourceIndexes.class).get(path, minified);
  }

  public String get(String path, boolean minified) {
    if (!minified) {
      return path;
    }

    Map<String, String> index = m_index.setIfAbsentAndGet(this::createNewIndex);
    String indexValue = index.get(path);
    if (indexValue == null) {
      return path; // return the input if no mapping could be found
    }
    return indexValue;
  }

  protected Map<String, String> createNewIndex() {
    return WebResourceHelpers.create()
        .getScriptResource(ScriptResourceIndexBuilder.INDEX_FILE_NAME, true)
        .map(url -> BEANS.get(ScriptResourceIndexBuilder.class).build(url))
        .orElseThrow(() -> new PlatformException("{} is missing.", ScriptResourceIndexBuilder.INDEX_FILE_NAME));
  }
}
