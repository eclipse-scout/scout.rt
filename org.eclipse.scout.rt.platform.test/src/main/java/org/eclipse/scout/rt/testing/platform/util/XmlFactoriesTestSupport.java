package org.eclipse.scout.rt.testing.platform.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.platform.util.XmlUtility;
import org.junit.Assert;

/**
 * <h3>{@link XmlFactoriesTestSupport}</h3> Testing helper class to detect XXE vulnerabilities because of direct use of
 * the corresponding JRE factories instead of using {@link XmlUtility} which provides hardened factory methods.
 *
 * @see https://www.owasp.org/index.php/XML_External_Entity_(XXE)_Prevention_Cheat_Sheet
 */
public class XmlFactoriesTestSupport {

  private final List<Pattern> m_searchPatterns;
  private final List<Pattern> m_pathExclusions;
  private final List<String> m_errMsgs;
  private Path m_root;

  public XmlFactoriesTestSupport() {
    m_searchPatterns = new ArrayList<>();
    m_searchPatterns.add(Pattern.compile("DocumentBuilderFactory\\.newInstance\\("));
    m_searchPatterns.add(Pattern.compile("SAXParserFactory\\.newInstance\\("));
    m_searchPatterns.add(Pattern.compile("XMLInputFactory\\.newInstance\\("));
    m_searchPatterns.add(Pattern.compile("TransformerFactory\\.newInstance\\("));
    m_searchPatterns.add(Pattern.compile("SchemaFactory\\.newInstance\\("));
    m_searchPatterns.add(Pattern.compile("SAXTransformerFactory\\.newInstance\\("));
    m_searchPatterns.add(Pattern.compile("XMLReaderFactory\\.createXMLReader\\("));

    m_pathExclusions = new ArrayList<>();
    m_pathExclusions.add(buildFilePatternFor(XmlUtility.class));
    m_pathExclusions.add(buildFilePatternFor(XmlFactoriesTestSupport.class));

    m_errMsgs = new ArrayList<>();
    m_root = Paths.get("..").toAbsolutePath().normalize();
  }

  public static Pattern buildFilePatternFor(Class<?> clazz) {
    return Pattern.compile(Pattern.quote(clazz.getName().replace('.', File.separatorChar) + ".java"));
  }

  public void addFileExclusion(Pattern pat) {
    m_pathExclusions.add(pat);
  }

  public void addFileExclusion(Class<?> clazz) {
    m_pathExclusions.add(buildFilePatternFor(clazz));
  }

  public void doTest() throws IOException {
    Files.walkFileTree(getRoot(), new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        Path fileName = dir.getFileName();
        if (fileName != null && ".git".equals(fileName.toString())) {
          return FileVisitResult.SKIP_SUBTREE;
        }
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Path fileName = file.getFileName();
        if (fileName != null && fileName.toString().toLowerCase().endsWith(".java") && !isExcluded(file.toString())) {
          checkFile(file);
        }
        return FileVisitResult.CONTINUE;
      }
    });
  }

  public List<String> getErrorMessages() {
    return Collections.unmodifiableList(m_errMsgs);
  }

  public void failOnError() {
    List<String> err = getErrorMessages();
    if (err.isEmpty()) {
      return;
    }
    StringBuilder sb = new StringBuilder();
    sb.append(err.get(0));
    for (int i = 1; i < err.size(); i++) {
      sb.append("\n").append(err.get(i));
    }
    String message = sb.toString();
    Assert.fail(message);
  }

  private void checkFile(Path path) throws IOException {
    String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    for (Pattern pat : m_searchPatterns) {
      if (pat.matcher(content).find()) {
        Path subpath = path.subpath(getRoot().getNameCount(), path.getNameCount());
        m_errMsgs.add("Own XML factory usage ('" + pat.toString().replace("\\", "") + "') found in '" + subpath + "'. Use " + XmlUtility.class.getSimpleName() + " instead!");
      }
    }
  }

  private boolean isExcluded(String searchIn) {
    for (Pattern p : m_pathExclusions) {
      if (p.matcher(searchIn).find()) {
        return true;
      }
    }
    return false;
  }

  public Path getRoot() {
    return m_root;
  }

  public void setRoot(Path root) {
    m_root = root;
  }
}
