package org.eclipse.scout.rt.ui.html.res.loader;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.platform.util.ImmutablePair;
import org.eclipse.scout.rt.platform.util.Pair;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.ui.html.script.ScriptRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

@Bean
public class ScriptResourceIndexBuilder {

  private static final Logger LOG = LoggerFactory.getLogger(ScriptResourceIndexBuilder.class);
  public static final String INDEX_FILE_NAME = "file-list";

  public Map<String, String> build(URL url) {
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
    if(!StringUtility.hasText(fileEntry)) {
      // skip empty lines
      return Optional.empty();
    }

    String path = fileEntry.trim();
    if (path.indexOf('/') < 0) {
      // add path element because it is required by ScriptRequest.tryParse
      // it will be excluded in the toString later on
      path = "./" + path;
    }
    Optional<ScriptRequest> result = ScriptRequest.tryParse(path);
    if (!result.isPresent()) {
      LOG.warn("Invalid entry in script resource index: '{}'.", fileEntry);
    }
    return result
        .map(sr -> sr.toString(false, true, false, false, true))
        .map(devName -> new ImmutablePair<>(devName, fileEntry));
  }

  protected Stream<String> readAllLines(URL url) {
    try {
      return IOUtility.readAllLinesFromUrl(url, StandardCharsets.UTF_8).stream();
    }
    catch (IOException e) {
      throw new PlatformException("Unable to read from URL '{}'.", url, e);
    }
  }
}
