package org.eclipse.scout.jaxws.internal.adapter;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IAdapterFactory;

import com.sun.xml.internal.ws.transport.http.ResourceLoader;

/**
 * Adapter Factory to adapt a {@link ResourceLoader} into a {@link com.sun.xml.internal.ws.api.ResourceLoader} object.
 */
@SuppressWarnings("restriction")
public class ResourceLoaderAdapterFactory implements IAdapterFactory {

  @Override
  public Object getAdapter(Object adaptableObject, Class adapterType) {
    final ResourceLoader resourceLoader = (ResourceLoader) adaptableObject;

    if (com.sun.xml.internal.ws.api.ResourceLoader.class.equals(adapterType)) {
      return new com.sun.xml.internal.ws.api.ResourceLoader() {

        @Override
        public URL getResource(String resource) throws MalformedURLException {
          return resourceLoader.getResource("/WEB-INF/" + resource);
        }
      };
    }
    return null;
  }

  @Override
  public Class<?>[] getAdapterList() {
    return new Class<?>[]{com.sun.xml.internal.ws.api.ResourceLoader.class};
  }
}
