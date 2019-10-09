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
package org.eclipse.scout.migration.ecma6.task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.scout.migration.ecma6.Configuration;
import org.eclipse.scout.migration.ecma6.PathFilters;
import org.eclipse.scout.migration.ecma6.PathInfo;
import org.eclipse.scout.migration.ecma6.WorkingCopy;
import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.migration.ecma6.model.old.JsFile;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Order(26000)
public class T26000_BlockIndex extends AbstractTask {

  public static final Pattern SHARED_RESOURCE_PATTERN = Pattern.compile("^@(.*?)\\{(.+)}$");
  public static final Pattern FILE_EXTENSION_PATTERN = Pattern.compile("^(.*)(\\.[^.]+)$");
  private static final Logger LOG = LoggerFactory.getLogger(T26000_BlockIndex.class);

  private WorkingCopy m_current;

  @Override
  public boolean accept(PathInfo pathInfo, Context context) {
    return PathFilters.inSrcMainJs().test(pathInfo) && pathInfo.getPath().getFileName().toString().endsWith(".index");
  }

  @Override
  public void process(PathInfo pathInfo, Context context) {
    WorkingCopy workingCopy = context.getWorkingCopy(pathInfo.getPath());
    if (pathInfo.getPath().toString().replace('\\', '/').contains("/_shared/")) {
      // shared index file. this is no longer required
      workingCopy.setDeleted(true);
      return;
    }

    m_current = workingCopy;
    P_Index result = new P_Index();
    parse(workingCopy.getSource(), result, context);

    P_FileEntry entryPointScript = null;
    boolean hasScripts = result.m_entries.stream().anyMatch(type -> "SCRIPT".equals(type.m_name));
    if (hasScripts) {
      // if the index has no scripts the RUN entry is in the core and must not be searched in the steps
      entryPointScript = Assertions.assertNotNull(getEntryPointScript(result), "Cannot find a matching entry-point script for index '{}'.", m_current.getPath());
    }
    Set<P_FileEntry> removedDependencies = migrate(result, entryPointScript);
    registerDependenciesInEntryPoint(result, entryPointScript, removedDependencies, context);

    write(result);

    m_current = null;
  }

  protected void registerDependenciesInEntryPoint(P_Index index, P_FileEntry entryPointScript, Set<P_FileEntry> dependencies, Context context) {
    if (entryPointScript == null || dependencies.isEmpty()) {
      return;
    }

    Path srcMainJs = Configuration.get().getSourceModuleDirectory().resolve("src/main/js");
    JsFile entryPointClass = context.getJsClass(index.m_run).getJsFile();
    Path entryPointPath = srcMainJs.resolve(entryPointScript.m_localPath);
    WorkingCopy workingCopy = context.ensureWorkingCopy(entryPointPath);
    String nl = workingCopy.getLineDelimiter();

    StringBuilder entryPointSuffix = new StringBuilder();
    for (P_FileEntry dep : dependencies) {
      Path dependencyPath = srcMainJs.resolve(dep.m_localPath);
      String depFileName = dependencyPath.getFileName().toString();
      if (Files.isRegularFile(dependencyPath) && Files.isReadable(dependencyPath) && depFileName.endsWith(".js")) {
        String memberName = depFileName.substring(0, depFileName.length() - 3);
        String ref = entryPointClass.getOrCreateImport(memberName, dependencyPath, false, true).getReferenceName();
        entryPointSuffix.append("  ").append(ref).append(',').append(nl);
      }
    }
    if (entryPointSuffix.length() < 1) {
      return;
    }

    entryPointSuffix.delete(entryPointSuffix.length() - (1 + nl.length()), entryPointSuffix.length());
    entryPointSuffix.insert(0, "window.studio = Object.assign(window.studio || {}, {" + nl);
    entryPointSuffix.append(nl).append("});");

    workingCopy.setSource(workingCopy.getSource() + nl + nl + entryPointSuffix);
  }

  protected Set<P_FileEntry> migrate(P_Index index, P_FileEntry entryPointScript) {
    // to not touch STYLE, TEXT, RUN, DATA
    Set<P_FileEntry> removedDependencies = new LinkedHashSet<>();
    Iterator<P_IndexType> iterator = index.m_entries.iterator();
    while (iterator.hasNext()) {
      P_IndexType line = iterator.next();
      if ("INDEX".equals(line.m_name)) {
        iterator.remove(); // references to shared resources are imported in js imports
      }
      else if ("MODEL".equals(line.m_name)) {
        iterator.remove(); // references to model are imported in js
      }
      else if ("SCRIPT".equals(line.m_name)) {
        Iterator<P_FileEntry> lineIt = line.m_entry.iterator();
        while (lineIt.hasNext()) {
          P_FileEntry curFile = lineIt.next();
          if (curFile != entryPointScript) {
            lineIt.remove();
            removedDependencies.add(curFile);
          }
        }
        if (line.m_entry.isEmpty()) {
          iterator.remove();
        }
      }
    }
    return removedDependencies;
  }

  protected P_FileEntry getEntryPointScript(P_Index index) {
    String run = index.m_run;
    int lastDot = run.lastIndexOf('.');
    if (lastDot > 0) {
      run = run.substring(lastDot + 1);
    }
    final String runClass = run;
    return index.m_entries.stream()
        .filter(type -> "SCRIPT".equals(type.m_name))
        .flatMap(type -> type.m_entry.stream())
        .filter(entry -> entry.m_localPath.endsWith('/' + runClass + ".js"))
        .findAny()
        .orElse(null);
  }

  protected void write(P_Index index) {
    StringBuilder builder = new StringBuilder();
    String nl = m_current.getLineDelimiter();
    index.m_entries.forEach(line -> writeEntry(line, builder, nl));
    m_current.setSource(builder.toString());
  }

  protected void writeEntry(P_IndexType line, StringBuilder builder, String nl) {
    if (!"COMMENT".equals(line.m_name)) {
      builder.append(line.m_name).append(':');
      for (int i = 1; i < 8 - line.m_name.length(); i++) {
        builder.append(' ');
      }
    }

    String entryLine = line.m_entry.stream().map(e -> e.m_raw).collect(Collectors.joining(" > "));
    builder.append(entryLine).append(nl);
  }

  protected void parse(String content, P_Index result, Context context) {
    try (BufferedReader reader = new BufferedReader(new StringReader(content))) {

      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) {
          P_IndexType comment = new P_IndexType("COMMENT");
          comment.m_entry.add(new P_FileEntry(line));
          result.m_entries.add(comment);
          continue;
        }
        String[] a = line.split(":", 2);
        if (a.length != 2) {
          LOG.warn("Unparsable line (skipped): {}", line);
          continue;
        }

        String rawType = a[0].trim();
        String value = a[1].trim();
        if (!ObjectUtility.isOneOf(rawType, "INDEX", "SCRIPT", "STYLE", "TEXT", "MODEL", "RUN")) {
          LOG.warn("Unknown resource type '{}' (skipped): {}", rawType, line);
          continue;
        }

        if ("INDEX".equals(rawType)) {
          // Include data from another index file
          for (String file : value.split(">")) {
            Path refIndex = Configuration.get().getSourceModuleDirectory().resolve("src/main/js").resolve(new P_FileEntry(file).m_localPath);
            String nestedSrc = context.ensureWorkingCopy(refIndex).getSource();
            parse(nestedSrc, result, context);
          }
        }
        else {
          P_IndexType t = new P_IndexType(rawType);
          fillFromLine(t, result, value);
          result.m_entries.add(t);
        }
      }
    }
    catch (IOException e) {
      throw new ProcessingException("", e);
    }
  }

  protected void fillFromLine(P_IndexType type, P_Index result, String value) {
    if (type.m_name.equals("RUN")) {
      Assertions.assertNull(result.m_run);
      result.m_run = value;
      type.m_entry.add(new P_FileEntry(value));
    }
    else if (type.m_name.equals("TEXT")) {
      type.m_entry.add(new P_FileEntry(value));
    }
    else {
      String[] b = value.split(">");
      for (String part : b) {
        type.m_entry.add(new P_FileEntry(part));
      }
    }
  }

  private class P_Index {
    private String m_run;
    private final List<P_IndexType> m_entries = new ArrayList<>();
  }

  private class P_IndexType {
    private final String m_name;
    private final List<P_FileEntry> m_entry = new ArrayList<>();

    private P_IndexType(String name) {
      m_name = name;
    }
  }

  private class P_FileEntry {
    private String m_file;
    private final String m_raw;
    private final String m_localPath;

    private P_FileEntry(String raw) {
      m_raw = raw.trim();
      String sharedNamespace;
      Matcher sharedResourceMatcher = SHARED_RESOURCE_PATTERN.matcher(m_raw);
      if (sharedResourceMatcher.matches()) {
        sharedNamespace = sharedResourceMatcher.group(1);
        if (!StringUtility.hasText(sharedNamespace)) {
          sharedNamespace = "studio"; // default namespace
        }
        m_file = sharedResourceMatcher.group(2);
      }
      else if (m_current.getPath().toString().replace('\\', '/').contains("/_shared/")) {
        // Index is a shared resource -> assume everything is a shared resource
        sharedNamespace = Configuration.get().getNamespace();
        m_file = m_raw;
      }
      else {
        m_file = m_raw;
        sharedNamespace = null;
      }
      m_file = m_file.replaceAll("^/", "");

      String fileBase = m_file;
      String fileExt = "";
      Matcher fileExtMatcher = FILE_EXTENSION_PATTERN.matcher(m_file);
      if (fileExtMatcher.matches()) {
        fileBase = fileExtMatcher.group(1);
        fileExt = fileExtMatcher.group(2);
      }

      if (sharedNamespace != null) {
        m_localPath = sharedNamespace + "/" + Configuration.get().getStepConfigTypeName() + "/_shared/" + fileBase + fileExt;
      }
      else {
        Path basePath = Configuration.get().getSourceModuleDirectory().resolve("src/main/js").relativize(m_current.getPath()).getParent();
        m_localPath = basePath.toString().replace('\\', '/') + "/" + fileBase + fileExt;
      }
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      P_FileEntry that = (P_FileEntry) o;
      return Objects.equals(m_file, that.m_file) &&
          Objects.equals(m_raw, that.m_raw) &&
          Objects.equals(m_localPath, that.m_localPath);
    }

    @Override
    public int hashCode() {
      return Objects.hash(m_file, m_raw, m_localPath);
    }
  }
}
