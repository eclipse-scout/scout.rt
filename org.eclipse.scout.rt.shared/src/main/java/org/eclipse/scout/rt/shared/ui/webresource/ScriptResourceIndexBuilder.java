/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.shared.ui.webresource;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toMap;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.platform.util.ImmutablePair;
import org.eclipse.scout.rt.platform.util.Pair;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Bean
public class ScriptResourceIndexBuilder {

  private static final Logger LOG = LoggerFactory.getLogger(ScriptResourceIndexBuilder.class);

  public Map<String, String> build(Enumeration<URL> url) {
    return readAllLines(url)
        .map(this::parse)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .filter(this::mappingNecessary)
        .collect(collectingAndThen(toMap(Pair::getLeft, Pair::getRight), Collections::unmodifiableMap));
  }

  protected boolean mappingNecessary(Pair<String, String> mapping) {
    return !Objects.equals(mapping.getLeft(), mapping.getRight());
  }

  protected Optional<Pair<String, String>> parse(String fileEntry) {
    if (!StringUtility.hasText(fileEntry)) {
      // skip empty lines
      return Optional.empty();
    }

    String path = fileEntry.trim();
    if (path.indexOf('/') < 0) {
      // add path element because it is required by ScriptRequest.tryParse
      path = "./" + path;
    }
    Optional<ScriptRequest> result = ScriptRequest.tryParse(path);
    if (!result.isPresent()) {
      LOG.debug("Invalid entry in script resource index: '{}'.", fileEntry);
    }
    return result
        .map(sr -> sr.toString(true, true, false, false, true))
        .map(devName -> new ImmutablePair<>(devName, fileEntry));
  }

  protected Stream<String> readAllLines(Enumeration<URL> urls) {
    List<String> linesCombined = new ArrayList<>();
    while (urls.hasMoreElements()) {
      URL url = urls.nextElement();
      try {
        linesCombined.addAll(IOUtility.readAllLinesFromUrl(url, StandardCharsets.UTF_8));
      }
      catch (IOException e) {
        throw new PlatformException("Unable to read from URL '{}'.", url, e);
      }
    }
    return linesCombined.stream();
  }
}
