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
package org.eclipse.scout.migration.ecma6.model.api.less;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.migration.ecma6.Configuration;
import org.eclipse.scout.migration.ecma6.WorkingCopy;
import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.CompositeObject;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class LessApiParser {

  private static final Logger LOG = LoggerFactory.getLogger(LessApiParser.class);
  private static final String LESS_FILE_SUFFIX = ".less";
  public static final String API_FILE_SUFFIX = "-less-api.json";
  private static final String DEFAULT_THEME_NAME = "default-theme";
  private static final Pattern REGEX_COMMENT_REMOVE_1 = Pattern.compile("//.*?\r\n");
  private static final Pattern REGEX_COMMENT_REMOVE_2 = Pattern.compile("//.*?\n");
  private static final Pattern REGEX_COMMENT_REMOVE_3 = Pattern.compile("(?s)/\\*.*?\\*/");

  private static final Pattern MIXIN_FUNCTION_PAT = Pattern.compile("\\n\\s\\s\\.([\\w-]+)");
  private static final Pattern LESS_VAR_PAT = Pattern.compile("\\n@([\\w-]+):");
  private static final Pattern LESS_MIXIN_PAT = Pattern.compile("#(\\w+)\\s*\\{");

  private Map<String /* mixin namespace (e.g. #scout.vendor) */, String /* rel path */> m_mixins;
  private Map<String /* variable name*/, Map<String /* theme name */, String /* rel path */>> m_vars;
  private final Map<String /* lib name */, LessApiParser> m_libraries;
  private String m_name;
  private final ObjectMapper m_defaultJacksonObjectMapper;

  public LessApiParser() {
    m_vars = new HashMap<>();
    m_mixins = new HashMap<>();
    m_libraries = new LinkedHashMap<>();
    m_defaultJacksonObjectMapper = new ObjectMapper()
        .setSerializationInclusion(Include.NON_DEFAULT)
        .enable(SerializationFeature.INDENT_OUTPUT)
        .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
        .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
  }

  public void parseFromSourceDir(Path sourceRoot) throws IOException {
    m_vars.clear();
    m_mixins.clear();
    Files.walkFileTree(sourceRoot, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (file.getFileName().toString().endsWith(LESS_FILE_SUFFIX)) {
          parseLessFile(file, sourceRoot);
        }
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        String dirName = dir.getFileName().toString();
        if ("target".equalsIgnoreCase(dirName)) {
          return FileVisitResult.SKIP_SUBTREE;
        }
        if (".git".equals(dirName)) {
          return FileVisitResult.SKIP_SUBTREE;
        }
        if ("node_modules".equals(dirName)) {
          return FileVisitResult.SKIP_SUBTREE;
        }
        return FileVisitResult.CONTINUE;
      }
    });
  }

  public void parseFromApiFiles(Path apiFileDir) throws IOException {
    if (apiFileDir == null || !Files.exists(apiFileDir)) {
      return;
    }
    //noinspection resource
    Files
        .list(apiFileDir)
        .filter(file -> file.getFileName().toString().endsWith(API_FILE_SUFFIX))
        .map(this::importApiFile)
        .filter(Objects::nonNull)
        .sorted(new LibrarySortOrderComparator())
        .forEach(lib -> {
          LessApiParser previous = m_libraries.put(lib.getName(), lib);
          if (previous != null) {
            LOG.warn("Duplicate Less API definition with name '{}'.", lib.getName());
          }
        });
  }

  public List<String> getRequiredImportsFor(WorkingCopy less, Context context) {
    Set<String> requiredImports = new HashSet<>();
    for (LessApiParser lib : m_libraries.values()) {
      collectRequiredImportsForMixin(less, context, lib, requiredImports, true);
      collectRequiredImportsForVariables(less, context, lib, requiredImports, true);
    }
    collectRequiredImportsForMixin(less, context, this, requiredImports, false);
    collectRequiredImportsForVariables(less, context, this, requiredImports, false);

    List<String> imports = new ArrayList<>(requiredImports);
    Collections.sort(imports);
    return imports;
  }

  protected static void collectRequiredImportsForVariables(WorkingCopy less, Context ctx, LessApiParser lib, Set<String> requiredImports, boolean isExternal) {
    String lessSrc = less.getSource();
    String theme = parseTheme(less.getPath());
    String libImportPrefix = getExternalLibPrefix(lib);
    for (Entry<String, Map<String, String>> variable : lib.m_vars.entrySet()) {
      String var = variable.getKey();
      if (lessSrc.contains(var)) {
        Map<String, String> filesDefiningVariable = variable.getValue();
        String fileDeclaringVariable = filesDefiningVariable.get(theme);
        if (fileDeclaringVariable == null) {
          fileDeclaringVariable = filesDefiningVariable.get(DEFAULT_THEME_NAME);
        }
        if (fileDeclaringVariable == null) {
          throw new ProcessingException("Cannot find less import for variable '{}'.", var);
        }
        if (isExternal) {
          requiredImports.add(toExternalImport(libImportPrefix, fileDeclaringVariable));
        }
        else {
          requiredImports.add(toInternalImport(less, fileDeclaringVariable));
        }
      }
    }
  }

  protected static String getExternalLibPrefix(LessApiParser lib) {
    return '~' + lib.getName() + "/src/";
  }

  protected static void collectRequiredImportsForMixin(WorkingCopy less, Context ctx, LessApiParser lib, Set<String> requiredImports, boolean isExternal) {
    String lessSrc = less.getSource();
    String libImportPrefix = getExternalLibPrefix(lib);
    for (Entry<String, String> mixin : lib.m_mixins.entrySet()) {
      String mixinFqn = mixin.getKey();
      if (lessSrc.contains(mixinFqn)) {
        if (isExternal) {
          requiredImports.add(toExternalImport(libImportPrefix, mixin.getValue()));
        }
        else {
          requiredImports.add(toInternalImport(less, mixin.getValue()));
        }
      }
    }
  }

  protected static String toExternalImport(String libImportPrefix, String relPath) {
    Path path = Paths.get(relPath);
    return removeLessFileExtension(libImportPrefix + path.subpath(4, path.getNameCount()).toString().replace('\\', '/'));
  }

  protected static String toInternalImport(WorkingCopy less, String relPath) {
    Path absolutePathToImport = Configuration.get().getSourceModuleDirectory().resolve(relPath);
    Path relPathToImport = less.getPath().getParent().relativize(absolutePathToImport);
    return removeLessFileExtension(relPathToImport.toString().replace('\\', '/'));
  }

  protected static String removeLessFileExtension(String path) {
    if (path.endsWith(LESS_FILE_SUFFIX)) {
      return path.substring(0, path.length() - LESS_FILE_SUFFIX.length());
    }
    return path;
  }

  private static class LibrarySortOrderComparator implements Comparator<LessApiParser> {
    @Override
    public int compare(LessApiParser o1, LessApiParser o2) {
      return getOrder(o1).compareTo(getOrder(o2));
    }

    private CompositeObject getOrder(LessApiParser parser) {
      return new CompositeObject(getScore(parser), parser.getName(), parser);
    }

    private int getScore(LessApiParser parser) {
      switch (parser.getName()) {
        case "eclipse-scout":
          return 10;
        case "bsi-scout":
          return 20;
        case "bsi-crm":
        case "bsi-briefcase":
        case "bsi-portal":
        case "bsi-studio":
          return 30;
        default:
          return 100;
      }
    }
  }

  protected LessApiParser importApiFile(Path apiFile) {
    try {
      LessApiParser api = m_defaultJacksonObjectMapper.readValue(Files.newInputStream(apiFile), LessApiParser.class);
      if (api.getName().equals(getName())) {
        LOG.warn("Skip Less API definition from file '{}' with name '{}' because it has the same name as the current module.", apiFile, getName());
      }
      else {
        return api;
      }
    }
    catch (IOException e) {
      throw new ProcessingException("Unable to read less api {}.", apiFile, e);
    }
    return null;
  }

  public void writeApiFile(Path apiFileDir) throws IOException {
    String fileName = getName().replace('\\', '-').replace("@", "").replace('/', '-');
    Path outFile = apiFileDir.resolve(fileName + API_FILE_SUFFIX);
    if (Files.exists(outFile)) {
      Files.delete(outFile);
    }
    else {
      Files.createDirectories(apiFileDir);
    }
    Files.createFile(outFile);
    m_defaultJacksonObjectMapper.writeValue(Files.newBufferedWriter(outFile), this);
  }

  public void setName(String newName) {
    m_name = newName;
  }

  public String getName() {
    return m_name;
  }

  public void setGlobalVariables(Map<String, Map<String, String>> newVariables) {
    m_vars = newVariables;
  }

  public Map<String, Map<String, String>> getGlobalVariables() {
    return Collections.unmodifiableMap(m_vars);
  }

  public void setMixins(Map<String, String> mix) {
    m_mixins = mix;
  }

  public Map<String, String> getMixins() {
    return Collections.unmodifiableMap(m_mixins);
  }

  protected void parseLessFile(Path file, Path sourceRoot) throws IOException {
    String fileContent = removeComments(new String(Files.readAllBytes(file)));
    String relPath = sourceRoot.relativize(file).toString().replace('\\', '/');
    String theme = parseTheme(file);
    parseMixins(fileContent, relPath);
    parseGlobalVariables(fileContent, theme, relPath);
  }

  protected static String parseTheme(Path lessFile) {
    String filenameWithExtension = lessFile.getFileName().toString();
    String fileName = filenameWithExtension.substring(0, filenameWithExtension.length() - LESS_FILE_SUFFIX.length());
    int firstDelimiterPos = fileName.indexOf('-');
    if (firstDelimiterPos < 0) {
      // no theme in file name: default theme
      return DEFAULT_THEME_NAME;
    }
    return fileName.substring(firstDelimiterPos + 1);
  }

  protected void parseMixins(String content, String relPath) {
    Matcher matcher = LESS_MIXIN_PAT.matcher(content);
    while (matcher.find()) {
      int endOfMixinStartDeclaration = matcher.end(1);
      int endOfMixinEndDeclaration = content.indexOf("\n}\n", endOfMixinStartDeclaration);
      String mixin = '#' + matcher.group(1);
      parseMixinContent(content.substring(endOfMixinStartDeclaration, endOfMixinEndDeclaration), mixin, relPath);
    }
  }

  protected void parseMixinContent(String source, String prefix, String relPath) {
    Matcher matcher = MIXIN_FUNCTION_PAT.matcher(source);
    while (matcher.find()) {
      String functionName = matcher.group(1);
      String mixinFqn = prefix + '.' + functionName;
      String previous = m_mixins.put(mixinFqn, relPath);
      if (previous != null && !previous.equals(relPath)) {
        LOG.warn("Duplicate less mixin: '{}'.", mixinFqn);
      }
    }
  }

  protected void parseGlobalVariables(String content, String theme, String relPath) {
    Matcher matcher = LESS_VAR_PAT.matcher(content);
    while (matcher.find()) {
      String variable = '@' + matcher.group(1);
      String previousPath = m_vars.computeIfAbsent(variable, k -> new HashMap<>()).put(theme, relPath);
      if (previousPath != null && !previousPath.equals(relPath)) {
        LOG.warn("Duplicate global less variable on location '{}' and '{}'.", relPath, previousPath);
      }
    }
  }

  public static String removeComments(CharSequence methodBody) {
    if (methodBody == null) {
      return null;
    }
    if (!StringUtility.hasText(methodBody)) {
      return methodBody.toString();
    }
    String retVal = REGEX_COMMENT_REMOVE_1.matcher(methodBody).replaceAll("");
    retVal = REGEX_COMMENT_REMOVE_2.matcher(retVal).replaceAll("");
    retVal = REGEX_COMMENT_REMOVE_3.matcher(retVal).replaceAll("");
    return retVal;
  }
}
