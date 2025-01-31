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

import static java.util.Collections.*;
import static java.util.stream.Collectors.*;
import static org.eclipse.scout.rt.platform.util.StreamUtility.ignoringMerger;
import static org.eclipse.scout.rt.shared.ui.webresource.AbstractWebResourceResolver.stripLeadingSlash;
import static org.eclipse.scout.rt.shared.ui.webresource.WebResources.resolveScriptResources;

import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.ImmutablePair;
import org.eclipse.scout.rt.platform.util.LazyValue;
import org.eclipse.scout.rt.platform.util.Pair;
import org.eclipse.scout.rt.shared.ui.webresource.FileListParser.FileListEntry;

/**
 * Main access to the parsed contents of a {@code file-list} file created by the Scout webpack build.<br>
 * See @eclipse-scout/cli/post-build.js#createFileList
 */
@ApplicationScoped
public class ScriptResourceIndexes {

  public static final String INDEX_FILE_NAME = "file-list";
  private static final LazyValue<ScriptResourceIndexes> INSTANCE = new LazyValue<>(ScriptResourceIndexes.class);

  private final AtomicReference<Map<String /* entry point */, Set<String> /* assets */>> m_devEntryPointToAssets = new AtomicReference<>();
  private final AtomicReference<Map<String /* entry point */, Set<String> /* assets */>> m_prodEntryPointToAssets = new AtomicReference<>();
  private final AtomicReference<Map<String /* asset name */, String /* minified asset path */>> m_assetNameToMinifiedName = new AtomicReference<>();

  /**
   * Gets all assets that are required by the given entry point.
   *
   * @param entryPoint
   *     The entry point name. Must not be {@code null}.
   * @param minified
   *     Specifies if minified or unminified assets should be returned.
   * @param cacheEnabled
   *     Specifies if the assets should be cached.
   * @return An unmodifiable {@link Set} holding the path to all assets (chunks) that are required by the given entry
   * point. Never returns {@code null}.
   */
  public static Set<String> getAssetsForEntryPoint(String entryPoint, boolean minified, boolean cacheEnabled) {
    return INSTANCE.get().getAssets(entryPoint, minified, cacheEnabled);
  }

  /**
   * This method can be used to convert an unminified path to its corresponding minified path (optionally including a
   * asset fingerprint).
   *
   * @param path
   *     The unminified path for which the minified path should be returned.
   * @param cacheEnabled
   *     Specifies if the assets should be cached.
   * @return The minified path corresponding to the input or the input path if no minified version can be found.
   */
  public static String getMinifiedPath(String path, boolean cacheEnabled) {
    return INSTANCE.get().toMinifiedPath(path, cacheEnabled);
  }

  public Set<String> getAssets(String entryPoint, boolean minified, boolean cacheEnabled) {
    Set<String> assets = entryPointToAssetsIndex(minified, cacheEnabled).get(entryPoint);
    if (assets == null) {
      return emptySet();
    }
    return unmodifiableSet(assets);
  }

  public String toMinifiedPath(String path, boolean cacheEnabled) {
    if (INDEX_FILE_NAME.equals(path)) {
      return path; // prevent StackOverflowError
    }
    String indexValue = assetNameToMinifiedNameIndex(cacheEnabled).get(stripLeadingSlash(path));
    if (indexValue == null) {
      return path; // return the input if no mapping could be found
    }
    return indexValue;
  }

  protected Map<String, Set<String>> entryPointToAssetsIndex(boolean minified, boolean cacheEnabled) {
    AtomicReference<Map<String, Set<String>>> index = minified ? m_prodEntryPointToAssets : m_devEntryPointToAssets;
    return computeIfAbsentOrDevMode(index, cacheEnabled, () -> createIndexEntryPointToAssets(minified, cacheEnabled));
  }

  protected Map<String, String> assetNameToMinifiedNameIndex(boolean cacheEnabled) {
    return computeIfAbsentOrDevMode(m_assetNameToMinifiedName, cacheEnabled, () -> createIndexAssetNameToMinifiedName(cacheEnabled));
  }

  protected Map<String, String> createIndexAssetNameToMinifiedName(boolean cacheEnabled) {
    return getFileListEntries(true, cacheEnabled)
        .collect(toMap(this::getEntryBasePath, FileListEntry::rawLine, ignoringMerger(), ConcurrentHashMap::new));
  }

  protected String getEntryBasePath(FileListEntry entry) {
    return entry.asset().toString(true, true, false, false, true);
  }

  protected Map<String, Set<String>> createIndexEntryPointToAssets(boolean minified, boolean cacheEnabled) {
    return getFileListEntries(minified, cacheEnabled)
        .flatMap(entry -> entry.entryPoints().stream()
            .map(ep -> new ImmutablePair<>(ep, entry.rawLine())))
        .collect(groupingBy(Pair::getLeft, ConcurrentHashMap::new, mapping(Pair::getRight, toSet())));
  }

  public Stream<FileListEntry> getFileListEntries(boolean minified, boolean cacheEnabled) {
    return resolveScriptResources(INDEX_FILE_NAME, minified, cacheEnabled, null).stream()
        .map(WebResourceDescriptor::getUrl)
        .flatMap(this::parseFileListEntries);
  }

  protected Stream<FileListEntry> parseFileListEntries(URL url) {
    return BEANS.get(FileListParser.class).parse(url);
  }

  /**
   * Returns the value in the cache {@link AtomicReference} (if available). If not yet available, the given
   * {@link Supplier} is executed and its result is stored in the cache and returned. The cache is not used and always
   * the supplier is executed if the cache is not enabled.
   *
   * @param cache
   *     The cache to use. Must not be {@code null}.
   * @param cacheEnabled
   *     Specifies if the caching is enabled. if {@code false} always the {@link Supplier} is executed and the
   *     cache is not touched.
   * @param elementSupplier
   *     The {@link Supplier} that returns the value. Must not be {@code null} and must not return {@code null}.
   * @return The computed (or cached) value.
   */
  protected <R> R computeIfAbsentOrDevMode(AtomicReference<R> cache, boolean cacheEnabled, Supplier<R> elementSupplier) {
    if (!cacheEnabled) {
      return elementSupplier.get();
    }
    R current = cache.get();
    if (current != null) {
      return current;
    }
    current = elementSupplier.get();
    cache.set(current);
    return current;
  }
}
