/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.shared.ui.webresource;

import static java.util.Collections.*;
import static java.util.stream.Collectors.*;
import static org.eclipse.scout.rt.shared.ui.webresource.AbstractWebResourceResolver.stripLeadingSlash;
import static org.eclipse.scout.rt.shared.ui.webresource.WebResources.resolveScriptResource;

import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.FinalValue;
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

  private final FinalValue<Map<String /* entry point */, Set<String> /* assets */>> m_devEntryPointToAssets = new FinalValue<>();
  private final FinalValue<Map<String /* entry point */, Set<String> /* assets */>> m_prodEntryPointToAssets = new FinalValue<>();
  private final FinalValue<Map<String /* asset name */, String /* minified asset path */>> m_assetNameToMinifiedName = new FinalValue<>();

  /**
   * Gets all assets that are required by the given entry point.
   *
   * @param entryPoint
   *          The entry point name. Must not be {@code null}.
   * @param minified
   *          Specifies if minified or unminified assets should be returned.
   * @return An unmodifiable {@link Set} holding the path to all assets (chunks) that are required by the given entry
   *         point. Never returns {@code null}.
   */
  public static Set<String> getAssetsForEntryPoint(String entryPoint, boolean minified) {
    return INSTANCE.get().getAssets(entryPoint, minified);
  }

  /**
   * This method can be used to convert an unminified path to its corresponding minified path (optionally including a
   * asset fingerprint).
   *
   * @param path
   *          The unminified path for which the minified path should be returned.
   * @return The minified path corresponding to the input or the input path if no minified version can be found.
   */
  public static String getMinifiedPath(String path) {
    return INSTANCE.get().toMinifiedPath(path);
  }

  public Set<String> getAssets(String entryPoint, boolean minified) {
    Set<String> assets = entryPointToAssetsIndex(minified).get(entryPoint);
    if (assets == null) {
      return emptySet();
    }
    return unmodifiableSet(assets);
  }

  public String toMinifiedPath(String path) {
    if (INDEX_FILE_NAME.equals(path)) {
      return path; // prevent StackOverflowError
    }
    String indexValue = assetNameToMinifiedNameIndex().get(stripLeadingSlash(path));
    if (indexValue == null) {
      return path; // return the input if no mapping could be found
    }
    return indexValue;
  }

  protected Map<String, Set<String>> entryPointToAssetsIndex(boolean minified) {
    FinalValue<Map<String, Set<String>>> index = minified ? m_prodEntryPointToAssets : m_devEntryPointToAssets;
    return index.setIfAbsentAndGet(() -> createIndexEntryPointToAssets(minified));
  }

  protected Map<String, String> assetNameToMinifiedNameIndex() {
    return m_assetNameToMinifiedName.setIfAbsentAndGet(this::createIndexAssetNameToMinifiedName);
  }

  protected Map<String, String> createIndexAssetNameToMinifiedName() {
    return getFileListEntries(true)
      .collect(toMap(this::getEntryBasePath, FileListEntry::rawLine /* throws on duplicates */));
  }

  protected String getEntryBasePath(FileListEntry entry) {
    return entry.asset().toString(true, true, false, false, true);
  }

  protected Map<String, Set<String>> createIndexEntryPointToAssets(boolean minified) {
    return getFileListEntries(minified)
      .flatMap(entry -> entry.entryPoints().stream()
        .map(ep -> new ImmutablePair<>(ep, entry.rawLine())))
      .collect(groupingBy(Pair::getLeft, mapping(Pair::getRight, toSet())));
  }

  public Stream<FileListEntry> getFileListEntries(boolean minified) {
    return resolveScriptResource(INDEX_FILE_NAME, minified, null)
      .map(WebResourceDescriptor::getUrl)
      .map(this::parseFileListEntries)
      .orElseGet(Stream::empty);
  }

  protected Stream<FileListEntry> parseFileListEntries(URL url) {
    return BEANS.get(FileListParser.class).parse(url);
  }
}
