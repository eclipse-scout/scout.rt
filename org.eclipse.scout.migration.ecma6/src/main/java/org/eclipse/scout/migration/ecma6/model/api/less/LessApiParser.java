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

import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNullOrEmpty;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.scout.migration.ecma6.Configuration;
import org.eclipse.scout.migration.ecma6.MigrationUtility;
import org.eclipse.scout.migration.ecma6.WorkingCopy;
import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.migration.ecma6.model.api.INamedElement;
import org.eclipse.scout.migration.ecma6.model.api.INamedElement.Type;
import org.eclipse.scout.migration.ecma6.model.api.Libraries;
import org.eclipse.scout.migration.ecma6.model.api.NamedElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class LessApiParser {

  private static final Logger LOG = LoggerFactory.getLogger(LessApiParser.class);
  public static final String LESS_FILE_SUFFIX = ".less";
  public static final String DEFAULT_THEME_NAME = "default-theme";
  public static final String PROP_PATH = "path";
  public static final String PROP_THEME = "theme";
  private static final Pattern MIXIN_FUNCTION_PAT = Pattern.compile("\\n\\s\\s\\.([\\w-]+)");
  private static final Pattern LESS_VAR_PAT = Pattern.compile("\\n@([\\w-]+):");
  private static final Pattern LESS_MIXIN_PAT = Pattern.compile("#(\\w+)\\s*\\{");

  private Map<String /* mixin name (e.g. #scout.animation) */, INamedElement> m_mixins;
  private Map<String /* variable name*/, Map<String /* theme name */, INamedElement>> m_vars;
  private Map<String /* lib name */, LessApiParser> m_libraries;
  private final Set<String> m_lessFilesOfCurrentModule;
  private String m_name;

  public LessApiParser() {
    m_vars = new HashMap<>();
    m_mixins = new HashMap<>();
    m_libraries = new LinkedHashMap<>();
    m_lessFilesOfCurrentModule = new HashSet<>();
  }

  public void parseFromSourceDir(Path sourceRoot, Context context) throws IOException {
    m_vars.clear();
    m_mixins.clear();
    m_lessFilesOfCurrentModule.clear();
    //noinspection Convert2Diamond
    Files.walkFileTree(sourceRoot, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (file.getFileName().toString().endsWith(LESS_FILE_SUFFIX)) {
          parseLessFile(file, sourceRoot, context);
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

  public void parseFromLibraries(Libraries libs) {
    m_libraries = libs.getChildren().stream()
        .map(this::parseLib)
        .filter(Objects::nonNull)
        .collect(Collectors.toMap(LessApiParser::getName, Function.identity(), (u, v) -> {
          throw new IllegalStateException(String.format("Duplicate Less API definition with name '%s'.", u.getName()));
        }, LinkedHashMap::new));
  }

  protected LessApiParser parseLib(INamedElement lib) {
    if (lib.getName().equals(getName())) {
      LOG.warn("Skip Less API definition with name '{}' because it has the same name as the current module.", getName());
      return null;
    }
    LessApiParser nested = new LessApiParser();
    nested.setName(lib.getCustomAttributeString(INamedElement.LIBRARY_MODULE_NAME));
    List<INamedElement> mixins = lib.getElements(Type.LessMixin);
    List<INamedElement> variables = lib.getElements(Type.LessVariable);

    Map<String, INamedElement> mixinMap = mixins.stream().collect(Collectors.toMap(INamedElement::getName, Function.identity()));
    nested.setMixins(mixinMap);

    Map<String, Map<String, INamedElement>> map = new HashMap<>();
    for (INamedElement var : variables) {
      map.computeIfAbsent(var.getName(), k -> new HashMap<>()).put(var.getCustomAttributeString(PROP_THEME), var);
    }
    nested.setGlobalVariables(map);

    return nested;
  }

  public List<String> getRequiredImportsFor(WorkingCopy less, Context context) {
    Map<String, String> requiredImports = new HashMap<>();
    for (LessApiParser lib : m_libraries.values()) {
      collectRequiredImportsForMixin(less, context, lib, requiredImports, true);
      collectRequiredImportsForVariables(less, context, lib, requiredImports, true);
    }
    collectRequiredImportsForMixin(less, context, this, requiredImports, false);
    collectRequiredImportsForVariables(less, context, this, requiredImports, false);

    return requiredImports.values().stream()
        .distinct()
        .sorted()
        .collect(Collectors.toList());
  }

  protected boolean isVariableDefinedIn(String var, String path) {
    Map<String, INamedElement> filesDefiningVariable = m_vars.get(var);
    if (filesDefiningVariable == null) {
      return false;
    }
    for (INamedElement lessVariable : filesDefiningVariable.values()) {
      String p = lessVariable.getCustomAttributeString(PROP_PATH);
      if (p.equals(path)) {
        return true;
      }
    }
    return false;
  }

  protected boolean isMixinDefinedIn(String mixin, String path) {
    INamedElement element = m_mixins.get(mixin);
    if (element == null) {
      return false;
    }
    String p = element.getCustomAttributeString(PROP_PATH);
    return p.equals(path);
  }

  protected void collectRequiredImportsForVariables(WorkingCopy less, Context ctx, LessApiParser lib, Map<String, String> requiredImports, boolean isExternal) {
    String lessSrc = less.getSource();
    String theme = parseTheme(less.getPath());
    String libImportPrefix = getExternalLibPrefix(lib);
    String moduleRelPath = Configuration.get().getSourceModuleDirectory().relativize(less.getPath()).toString().replace('\\', '/');
    for (Entry<String, Map<String, INamedElement>> variable : lib.m_vars.entrySet()) {
      String var = variable.getKey();
      if (lessSrc.contains(var) && !isVariableDefinedIn(var, moduleRelPath) /* do not create import if the variable is defined in the same file */) {
        Map<String, INamedElement> filesDefiningVariable = variable.getValue();
        INamedElement lessVariable = filesDefiningVariable.get(theme);
        if (lessVariable == null) {
          lessVariable = filesDefiningVariable.get(DEFAULT_THEME_NAME);
        }
        if (lessVariable == null) {
          LOG.debug("Cannot find less import for variable '{}' and theme '{}' in lib '{}'.", var, theme, lib.getName());
          continue;
        }
        String path = lessVariable.getCustomAttributeString(PROP_PATH);
        if (isExternal) {
          requiredImports.put(var, toExternalImport(libImportPrefix, path));
        }
        else {
          requiredImports.put(var, toInternalImport(less, path));
        }
      }
    }
  }

  protected static String getExternalLibPrefix(LessApiParser lib) {
    return '~' + lib.getName() + "/src/";
  }

  protected void collectRequiredImportsForMixin(WorkingCopy less, Context ctx, LessApiParser lib, Map<String, String> requiredImports, boolean isExternal) {
    String lessSrc = less.getSource();
    String libImportPrefix = getExternalLibPrefix(lib);
    String moduleRelPath = Configuration.get().getSourceModuleDirectory().relativize(less.getPath()).toString().replace('\\', '/');
    for (Entry<String, INamedElement> mixin : lib.m_mixins.entrySet()) {
      String mixinFqn = mixin.getKey();
      if (lessSrc.contains(mixinFqn) && !isMixinDefinedIn(mixinFqn, moduleRelPath) /* do not create an import if the mixin is defined in the same file*/) {
        String path = mixin.getValue().getCustomAttributeString(PROP_PATH);
        if (isExternal) {
          requiredImports.put(mixinFqn, toExternalImport(libImportPrefix, path));
        }
        else {
          requiredImports.put(mixinFqn, toInternalImport(less, path));
        }
      }
    }
  }

  protected static String toExternalImport(String libImportPrefix, String relPath) {
    return removeLessFileExtension(libImportPrefix + MigrationUtility.removeFirstSegments(relPath, 4));
  }

  protected static String toInternalImport(WorkingCopy less, String relPath) {
    Path absolutePathToImport = Configuration.get().getSourceModuleDirectory().resolve(relPath);
    Path relPathToImport = less.getPath().getParent().relativize(absolutePathToImport);
    return removeLessFileExtension(relPathToImport.toString().replace('\\', '/'));
  }

  public static String removeLessFileExtension(String path) {
    if (path.endsWith(LESS_FILE_SUFFIX)) {
      return path.substring(0, path.length() - LESS_FILE_SUFFIX.length());
    }
    return path;
  }

  public void setName(String newName) {
    m_name = assertNotNullOrEmpty(newName);
  }

  public String getName() {
    return m_name;
  }

  public void setGlobalVariables(Map<String, Map<String, INamedElement>> newVariables) {
    m_vars = newVariables;
  }

  public Map<String, Map<String, INamedElement>> getGlobalVariables() {
    return Collections.unmodifiableMap(m_vars);
  }

  public void setMixins(Map<String, INamedElement> mix) {
    m_mixins = mix;
  }

  @JsonIgnore
  public Set<String> getLessFilesOfCurrentModule() {
    return Collections.unmodifiableSet(m_lessFilesOfCurrentModule);
  }

  public Map<String, INamedElement> getMixins() {
    return Collections.unmodifiableMap(m_mixins);
  }

  protected void parseLessFile(Path file, Path sourceRoot, Context context) throws IOException {
    String fileContent = MigrationUtility.removeComments(new String(Files.readAllBytes(file)));
    String relPath = sourceRoot.relativize(file).toString().replace('\\', '/');
    String theme = parseTheme(file);
    m_lessFilesOfCurrentModule.add(MigrationUtility.removeFirstSegments(relPath, 4));
    parseMixins(fileContent, relPath, context);
    parseGlobalVariables(fileContent, theme, relPath, context);
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

  protected void parseMixins(String content, String relPath, Context context) {
    Matcher matcher = LESS_MIXIN_PAT.matcher(content);
    while (matcher.find()) {
      int endOfMixinStartDeclaration = matcher.end(1);
      int endOfMixinEndDeclaration = content.indexOf("\n}\n", endOfMixinStartDeclaration);
      String mixin = '#' + matcher.group(1);
      parseMixinContent(content.substring(endOfMixinStartDeclaration, endOfMixinEndDeclaration), mixin, relPath, context);
    }
  }

  protected void parseMixinContent(String source, String prefix, String relPath, Context context) {
    Matcher matcher = MIXIN_FUNCTION_PAT.matcher(source);
    while (matcher.find()) {
      String functionName = matcher.group(1);
      String mixinFqn = prefix + '.' + functionName;
      INamedElement mixin = new NamedElement(Type.LessMixin, mixinFqn, context.getApi());
      mixin.getCustomAttributes().put(PROP_PATH, relPath);
      INamedElement previous = m_mixins.put(mixinFqn, mixin);
      if (previous != null && !previous.getCustomAttributes().get(PROP_PATH).equals(relPath)) {
        LOG.warn("Duplicate less mixin: '{}'.", mixinFqn);
      }
    }
  }

  protected void parseGlobalVariables(String content, String theme, String relPath, Context context) {
    Matcher matcher = LESS_VAR_PAT.matcher(content);
    while (matcher.find()) {
      String variable = '@' + matcher.group(1);
      INamedElement varDeclaration = new NamedElement(Type.LessVariable, variable, context.getApi());
      varDeclaration.getCustomAttributes().put(PROP_PATH, relPath);
      varDeclaration.getCustomAttributes().put(PROP_THEME, theme);
      INamedElement previousPath = m_vars.computeIfAbsent(variable, k -> new HashMap<>()).put(theme, varDeclaration);
      if (previousPath != null && !previousPath.getCustomAttributes().get(PROP_PATH).equals(relPath)) {
        LOG.warn("Duplicate global less variable on location '{}' and '{}'.", relPath, previousPath);
      }
    }
  }
}
