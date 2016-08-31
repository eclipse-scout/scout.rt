package org.eclipse.scout.rt.ui.html.res;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;

public class BinaryResourceMediator {

  private final IJsonAdapter<?> m_jsonAdapter;
  private Map<String, BinaryResource> m_binaryResources = new HashMap<>(0);

  public BinaryResourceMediator(IJsonAdapter<?> jsonAdapter) {
    m_jsonAdapter = jsonAdapter;
  }

  public void addBinaryResource(BinaryResource binaryResource) {
    m_binaryResources.put(binaryResource.getFilename(), binaryResource);
  }

  public String createUrl(BinaryResource binaryResource) {
    return BinaryResourceUrlUtility.createDynamicAdapterResourceUrl(m_jsonAdapter, binaryResource.getFilename());
  }

  public BinaryResourceHolder getBinaryResourceHolder(String filename) {
    BinaryResource binaryResource = m_binaryResources.get(filename);
    if (binaryResource != null) {
      return new BinaryResourceHolder(binaryResource);
    }
    return null;
  }
}
