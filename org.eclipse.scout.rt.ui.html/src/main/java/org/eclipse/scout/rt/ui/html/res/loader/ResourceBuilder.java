package org.eclipse.scout.rt.ui.html.res.loader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.text.ITextProviderService;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.ui.html.IUiTextContributor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceBuilder {
  private static final Logger LOG = LoggerFactory.getLogger(ResourceBuilder.class);
  private Path m_outputDir;
  private List<IUiTextContributor> m_contributors;

  public ResourceBuilder(Path outputDir, List<IUiTextContributor> contributors) {
    m_outputDir = outputDir;
    m_contributors = contributors;
  }

  /**
   * Available arguments:
   * <ul>
   * <li>texts.json: texts loaded by the {@link TextsLoader}</li>
   * <li>locales.json: locales loaded by the {@link LocalesLoader}</li>
   * <li>-outputDir: the directory to put the generated files.</li>
   * <li>-contributors: specify to filter the texts returned by the {@link TextsLoader}.<br>
   * Either use a list of {@link IUiTextContributor}s (fully qualified) separated by ,.<br>
   * Or use the key word 'all' to get all contributors available on the current class path.<br>
   * If contributors are not specified the texts are not filtered and all texts returned by the available
   * {@link ITextProviderService}s are returned.</li>
   * </ul>
   */
  public static void main(String[] args) {
    Platform.get().awaitPlatformStarted();
    try {
      Path outputDir = build(args);
      LOG.info("All files built and written to {}", outputDir);
    }
    finally {
      Platform.get().stop();
    }
  }

  protected static Path build(String[] args) {
    Path outputDir = null;
    List<IUiTextContributor> contributors = null;
    List<String> files = new LinkedList<>();
    for (int i = 0; i < args.length; i++) {
      switch (args[i]) {
        case "-outputDir": {
          i++;
          outputDir = Paths.get(args[i]);
          break;
        }
        case "-contributors": {
          i++;
          contributors = parseContributors(args[i]);
          break;
        }
        default: {
          files.add(args[i]);
        }
      }
    }

    new ResourceBuilder(outputDir, contributors).build(files);
    return outputDir;
  }

  protected static List<IUiTextContributor> parseContributors(String contributors) {
    if ("all".equals(contributors)) {
      return BEANS.all(IUiTextContributor.class);
    }
    return Arrays.stream(contributors.split(","))
        .map(contributor -> {
          try {
            return (IUiTextContributor) BEANS.get(Class.forName(contributor));
          }
          catch (ClassNotFoundException e) {
            throw new ProcessingException("Invalid contributor", e);
          }
        }).collect(Collectors.toList());
  }

  public void build(List<String> files) {
    if (CollectionUtility.isEmpty(files)) {
      throw new IllegalArgumentException("No files specified");
    }

    for (String file : files) {
      LOG.info("Building resource '{}'", file);
      try {
        BinaryResource resource = loadResource(file);
        writeResource(resource);
      }
      catch (IOException e) {
        throw new Error("Failed to load file " + file, e);
      }
    }
  }

  public IResourceLoader getLoader(String file) {
    if (file.matches("^locales.json$")) {
      return new LocalesLoader();
    }
    if (file.matches("^texts.json$")) {
      TextsLoader textsLoader = new TextsLoader();
      if (m_contributors != null) {
        LOG.info("Using contributors {}", m_contributors);
        textsLoader.setEntryFilter(new UiTextContributionFilter(m_contributors));
      }
      return textsLoader;
    }
    return null;
  }

  public BinaryResource loadResource(String file) throws IOException {
    IResourceLoader loader = getLoader(file);
    if (loader == null) {
      throw new IOException("No loader found for " + file);
    }
    return loader.loadResource(file);
  }

  public void writeResource(BinaryResource resource) throws IOException {
    Path path = m_outputDir.resolve(resource.getFilename());
    Files.createDirectories(path.getParent());
    Files.write(path, resource.getContent());
    LOG.info("File written to {} ", path);
  }
}
