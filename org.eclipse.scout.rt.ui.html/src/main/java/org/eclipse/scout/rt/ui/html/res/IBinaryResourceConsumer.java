package org.eclipse.scout.rt.ui.html.res;

import java.util.List;
import java.util.Map;

import org.eclipse.scout.commons.resource.BinaryResource;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;

/**
 * {@link IJsonAdapter}s can implements {@link IBinaryResourceConsumer} in order to consume files from the UI (file
 * upload).
 */
public interface IBinaryResourceConsumer {

  void consumeBinaryResource(List<BinaryResource> binaryResources, Map<String, String> uploadProperties);

  /**
   * Maximum upload size in bytes.
   */
  long getMaximumBinaryResourceUploadSize();
}
