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

import static java.util.Collections.unmodifiableSet;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Stream;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses the contents of a file list file
 */
@Bean
public class FileListParser {

  private static final Logger LOG = LoggerFactory.getLogger(FileListParser.class);
  public static final String DEFAULT_ENTRY_POINT_DELIMITER = "~";

  /**
   * @param url
   *     The {@link URL} pointing to the file list to parse. Must not be {@code null}.
   * @return The valid {@link FileListEntry entries} found at the given {@link URL}.
   */
  public Stream<FileListEntry> parse(URL url) {
    return readAllLinesFromUrl(url)
        .map(this::getFileListEntryFor)
        .filter(Optional::isPresent)
        .map(Optional::get);
  }

  protected Optional<FileListEntry> getFileListEntryFor(String line) {
    return Optional.ofNullable(line)
        .filter(StringUtility::hasText)
        .map(String::trim)
        .flatMap(this::parse);
  }

  protected String getEntryPointDelimiter() {
    return DEFAULT_ENTRY_POINT_DELIMITER;
  }

  protected Optional<FileListEntry> parse(String rawLine) {
    String path = rawLine;
    if (path.indexOf('/') < 0) {
      // add path element because it is required by ScriptRequest.tryParse
      path = "./" + path;
    }
    Optional<ScriptRequest> result = ScriptRequest.tryParse(path);
    if (result.isEmpty()) {
      LOG.debug("Entry in script resource index ignored: '{}'.", rawLine);
      return Optional.empty();
    }
    ScriptRequest scriptRequest = result.get();
    StringTokenizer tokenizer = new StringTokenizer(scriptRequest.baseName(), getEntryPointDelimiter());
    Set<String> entryPoints = new HashSet<>();
    while (tokenizer.hasMoreTokens()) {
      String entryPoint = tokenizer.nextToken();
      if (StringUtility.hasText(entryPoint)) {
        entryPoints.add(entryPoint);
      }
    }
    if (entryPoints.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(new FileListEntry(scriptRequest, rawLine, entryPoints));
  }

  protected Stream<String> readAllLinesFromUrl(URL url) {
    try {
      return IOUtility.readAllLinesFromUrl(url, StandardCharsets.UTF_8).stream();
    }
    catch (IOException e) {
      throw new PlatformException("Unable to read from URL '{}'.", url, e);
    }
  }

  public static class FileListEntry {
    private final ScriptRequest m_asset;
    private final String m_rawLine;
    private final Set<String> m_entryPoints;

    public FileListEntry(ScriptRequest asset, String rawLine, Set<String> entryPoints) {
      m_asset = asset;
      m_rawLine = rawLine;
      m_entryPoints = new HashSet<>(entryPoints);
    }

    /**
     * @return An unmodifiable {@link Set} holding all entry point names that require this {@link #asset()}.
     */
    public Set<String> entryPoints() {
      return unmodifiableSet(m_entryPoints);
    }

    /**
     * @return The raw line content
     */
    public String rawLine() {
      return m_rawLine;
    }

    /**
     * @return The {@link ScriptRequest JavaScript asset} of this line.
     */
    public ScriptRequest asset() {
      return m_asset;
    }

    @Override
    public String toString() {
      return m_rawLine;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      FileListEntry that = (FileListEntry) o;
      return m_rawLine.equals(that.m_rawLine);
    }

    @Override
    public int hashCode() {
      return Objects.hash(m_rawLine);
    }
  }
}
