package org.eclipse.scout.rt.ui.html.res;

import java.net.URL;

import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;

/**
 * {@link IJsonAdapter}s can implements {@link IBinaryResourceProvider} in order to provide public {@link URL} calling
 * back to them.
 * <p>
 * URLs that call back to this method are defined using
 * {@link BinaryResourceUrlUtility#createDynamicAdapterResourceUrl(IJsonAdapter, String)}
 */
public interface IBinaryResourceProvider {

  BinaryResourceHolder provideBinaryResource(String filename);
}
