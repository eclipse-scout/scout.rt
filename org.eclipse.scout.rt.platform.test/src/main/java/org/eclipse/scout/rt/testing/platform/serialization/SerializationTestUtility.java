/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.testing.platform.serialization;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.inventory.IClassInfo;
import org.eclipse.scout.rt.platform.inventory.internal.JandexClassInventory;
import org.eclipse.scout.rt.platform.inventory.internal.JandexInventoryBuilder;
import org.eclipse.scout.rt.platform.inventory.internal.JandexInventoryBuilder.RebuildStrategy;
import org.eclipse.scout.rt.platform.serialization.DefaultSerializerBlacklist;
import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.JarIndexer;

/**
 * Utility for testing serializing and deserializing java objects.
 *
 * @since 11.0
 */
public final class SerializationTestUtility {

  private SerializationTestUtility() {
    // nop
  }

  /**
   * Note to developers: A call to this method must not start the Platform, thus not using {@link BEANS}
   *
   * @return a CSV table with the headers <code>Very dark | Dark | Custom blocked | Not whitelisted | Classname</code>
   * listing all vulnerable classes. Very dark and dark are sub categorizations of the default blacklist
   */
  public static String createVulnerabilityReport(Collection<String> serializableClasses, Predicate<String> whitelist) {
    DefaultSerializerBlacklist blacklist = new DefaultSerializerBlacklist();
    StringBuilder buf = new StringBuilder();
    buf.append("Very dark\tDark\tCustom blocked\tNot whitelisted\tClassname\n");
    serializableClasses
        .stream()
        .map(c -> {
          boolean v = blacklist.isVeryDark(c);
          boolean d = blacklist.isDark(c);
          boolean b = blacklist.isCustomBlocked(c);
          boolean w = whitelist.test(c);
          if (v || d || b || !w) {
            return (v ? "X" : "") + "\t" + (d ? "X" : "") + "\t" + (b ? "X" : "") + "\t" + (!w ? "X" : "") + "\t" + c;
          }
          return null;
        })
        .filter(Objects::nonNull)
        .forEach(s -> buf.append(s).append("\n"));
    return buf.toString();
  }

  /**
   * Collect all {@link Serializable} classes on the current classpath
   * <p>
   * Java 9+ requires the jvm option <code>--add-opens java.base/jdk.internal.loader=ALL-UNNAMED</code>
   *
   * @return list of fully qualified class names
   */
  public static List<String> collectAllSerializableClasses() {
    Map<ClassLoader, URL[]> map = new IdentityHashMap<>();
    collectClasspathRec(ClassLoader.getSystemClassLoader(), map);
    collectClasspathRec(SerializationTestUtility.class.getClassLoader(), map);
    List<File> roots = map.values()
        .stream()
        .filter(Objects::nonNull)
        .flatMap(Arrays::stream)
        .map(url -> {
          try {
            return url.toURI();
          }
          catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
          }
        })
        .distinct()
        .filter(uri -> uri.isAbsolute() && "file".equals(uri.getScheme()))
        .map(File::new)
        .filter(File::exists)
        .sorted()
        .collect(Collectors.toList());
    JandexInventoryBuilder inventoryBuilder = new JandexInventoryBuilder(RebuildStrategy.IF_MISSING) {
      @Override
      public void scanAllModules() {
        roots.parallelStream()
            .forEach(f -> {
              try {
                Index index;
                if (f.isDirectory()) {
                  Indexer indexer = new Indexer();
                  index = createFolderIndex(f.toPath(), indexer);
                }
                else {
                  Indexer indexer = new Indexer();
                  File tmp = File.createTempFile("jandex", ".idx");
                  try {
                    index = JarIndexer.createJarIndex(f, indexer, tmp, false, false, false).getIndex();
                  }
                  finally {
                    //noinspection ResultOfMethodCallIgnored
                    tmp.delete();
                  }
                }
                if (index != null) {
                  getIndexList().add(index);
                }
              }
              catch (Exception t) {
                throw new PlatformException("Error while building class inventory of {}", f, t);
              }
            });
      }
    };
    inventoryBuilder.scanAllModules();
    return new JandexClassInventory(inventoryBuilder.finish()).getAllKnownSubClasses(Serializable.class)
        .stream()
        .map(IClassInfo::name)
        .sorted()
        .collect(Collectors.toList());
  }

  @SuppressWarnings("rawtypes")
  private static void collectClasspathRec(ClassLoader loader, Map<ClassLoader, URL[]> map) {
    if (loader == null || map.containsKey(loader)) {
      return;
    }
    if (loader instanceof URLClassLoader) {
      map.put(loader, ((URLClassLoader) loader).getURLs());
    }
    else {
      //try to read jdk9+ 'ucp' field
      try {
        Class c = loader.getClass();
        Field field = c.getDeclaredField("ucp");
        field.setAccessible(true);
        Object ucp = field.get(loader);
        map.put(loader, (URL[]) (ucp.getClass().getMethod("getURLs").invoke(ucp)));
      }
      catch (NoSuchFieldException e) {
        map.put(loader, null);
      }
      catch (Exception e) {
        if ("java.lang.reflect.InaccessibleObjectException".equals(e.getClass().getName())) {
          throw new ProcessingException("Java 9+ requires the jvm option: --add-opens java.base/jdk.internal.loader=ALL-UNNAMED", e);
        }
        map.put(loader, null);
      }
    }
    collectClasspathRec(loader.getParent(), map);
  }
}
