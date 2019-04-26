package org.eclipse.scout.rt.ui.html.res;

import org.eclipse.scout.rt.platform.Bean;

import java.net.URL;
import java.util.Optional;

@Bean
public interface IWebResourceHelper {

  Optional<URL> getScriptResource(String path, boolean minified);

  Optional<URL> getWebResource(String path);
}
