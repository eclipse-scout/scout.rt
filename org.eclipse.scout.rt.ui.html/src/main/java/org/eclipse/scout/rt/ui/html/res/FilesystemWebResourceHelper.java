package org.eclipse.scout.rt.ui.html.res;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.scout.rt.platform.exception.PlatformException;

public class FilesystemWebResourceHelper extends AbstractWebResourceHelper {

  private final Path m_root;

  protected FilesystemWebResourceHelper() {
    m_root = findModuleRoot().resolve("dist");
  }

  @Override
  protected URL getResourceImpl(String resourcePath) {
    Path candidate = m_root.resolve(resourcePath);
    if (Files.isReadable(candidate) && Files.isRegularFile(candidate)) {
      try {
        return candidate.toUri().toURL();
      }
      catch (MalformedURLException e) {
        throw new PlatformException("Invalid URL for request '{}' resulting in '{}'.", resourcePath, candidate, e);
      }
    }
    return null;
  }

  @SuppressWarnings("findbugs:NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  protected static Path findModuleRoot() {
    Path workingDir = Paths.get("").toAbsolutePath();
    Path parentDir = workingDir.getParent();
    String folderName = workingDir.getFileName().toString();
    String appModuleName = folderName;
    if (folderName.endsWith(".dev") || folderName.endsWith("-dev")) {
      appModuleName = folderName.substring(0, folderName.length() - 4);
    }
    Path resourceRoot = parentDir.resolve(appModuleName);
    if (Files.isDirectory(resourceRoot) && Files.isReadable(resourceRoot)) {
      return resourceRoot;
    }
    return workingDir;
  }
}
