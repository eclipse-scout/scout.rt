/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.ui.webresource;

import static java.util.Collections.singleton;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.PlatformException;

public class FilesystemWebResourceResolver extends AbstractWebResourceResolver {

  private final List<Path> m_roots = new ArrayList<>();

  protected FilesystemWebResourceResolver() {
    BEANS.all(IFilesystemWebResourceRootContributor.class)
        .forEach(contributor -> m_roots.addAll(contributor.getRoots()));
  }

  @Override
  @SuppressWarnings("squid:S1166") // log or rethrow exception
  protected Stream<URL> getResourceImpl(String resourcePath) {
    try {
      return resolveUrls(m_roots, resourcePath);
    }
    catch (java.nio.file.InvalidPathException e) {
      // filesystem implementation does not understand/allow this path
      return null;
    }
  }

  /**
   * Tries to resolve the given relative path in the given root directory and returns the {@link URL} to the file if it
   * was found.
   *
   * @param root
   *     The root directory in which the given relative path should be resolved.
   * @param relPath
   *     The relative path to resolve.
   * @return The {@link URL} pointing to the file found in the given root or an empty {@link Stream} if the file could
   * not be found.
   */
  protected static Stream<URL> resolveUrls(Path root, String relPath) {
    return resolveUrls(singleton(root), relPath);
  }

  /**
   * Tries to resolve the given relative path in the given root directories and returns all {@link URL urls} that point
   * to existing files.
   *
   * @param roots
   *     The root directories in which the given relative path should be resolved.
   * @param relPath
   *     The relative path to resolve.
   * @return {@link URL Urls} pointing to all files found for the given relative path within the given roots.
   */
  protected static Stream<URL> resolveUrls(Collection<Path> roots, String relPath) {
    if (roots == null || relPath == null) {
      return Stream.empty();
    }
    return roots.stream()
        .map(root -> root.resolve(relPath))
        .map(FilesystemWebResourceResolver::toUrl)
        .filter(Objects::nonNull);
  }

  protected static URL toUrl(Path path) {
    if (Files.isReadable(path) && Files.isRegularFile(path)) {
      try {
        return path.toUri().toURL();
      }
      catch (MalformedURLException e) {
        throw new PlatformException("Invalid URL for path '{}'.", path, e);
      }
    }
    return null;
  }
}
