package org.eclipse.scout.commons.osgi;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.eclipse.scout.commons.StringUtility;
import org.osgi.framework.Bundle;

/**
 * Class loader implementation that uses a list of bundles to load classes.
 * 
 * @since 3.8.2
 */
public class BundleListClassLoader extends ClassLoader {

  /** table mapping primitive type names to corresponding class objects */
  private static final HashMap<String, Class> PRIMITIVE_TYPES;
  static {
    PRIMITIVE_TYPES = new HashMap<String, Class>(8, 1f);
    PRIMITIVE_TYPES.put("boolean", boolean.class);
    PRIMITIVE_TYPES.put("byte", byte.class);
    PRIMITIVE_TYPES.put("char", char.class);
    PRIMITIVE_TYPES.put("short", short.class);
    PRIMITIVE_TYPES.put("int", int.class);
    PRIMITIVE_TYPES.put("long", long.class);
    PRIMITIVE_TYPES.put("float", float.class);
    PRIMITIVE_TYPES.put("double", double.class);
    PRIMITIVE_TYPES.put("void", void.class);
    //
    PRIMITIVE_TYPES.put("Z", boolean.class);
    PRIMITIVE_TYPES.put("B", byte.class);
    PRIMITIVE_TYPES.put("C", char.class);
    PRIMITIVE_TYPES.put("S", short.class);
    PRIMITIVE_TYPES.put("I", int.class);
    PRIMITIVE_TYPES.put("J", long.class);
    PRIMITIVE_TYPES.put("F", float.class);
    PRIMITIVE_TYPES.put("D", double.class);
    PRIMITIVE_TYPES.put("V", void.class);
  }

  private final Bundle[] m_bundles;
  private final Bundle[] m_bundlesSortedByBundleSymbolicNameLenght;
  private final Map<String, Class<?>> m_classCache;

  public BundleListClassLoader(Bundle... bundles) {
    if (bundles == null || bundles.length == 0) {
      throw new IllegalArgumentException("bundle list must not be null or empty");
    }
    // filter given list of bundles.
    m_bundles = BundleInspector.filterPluginBundles(bundles);
    if (m_bundles.length == 0) {
      throw new IllegalArgumentException("filtered bundle list must not be empty. [bundles=" + Arrays.toString(bundles) + "]");
    }
    //
    m_bundlesSortedByBundleSymbolicNameLenght = new Bundle[m_bundles.length];
    System.arraycopy(m_bundles, 0, m_bundlesSortedByBundleSymbolicNameLenght, 0, m_bundles.length);
    Arrays.sort(m_bundlesSortedByBundleSymbolicNameLenght, new Comparator<Bundle>() {
      @Override
      public int compare(Bundle b1, Bundle b2) {
        if (b1 == null && b2 == null) {
          return 0;
        }
        if (b1 == null) {
          return -1;
        }
        if (b2 == null) {
          return 1;
        }
        return StringUtility.length(b2.getSymbolicName()) - StringUtility.length(b1.getSymbolicName());
      }
    });
    //
    m_classCache = new HashMap<String, Class<?>>();
  }

  private Class<?> putInCache(String name, Class<?> c) {
    if (c != null) {
      synchronized (m_classCache) {
        m_classCache.put(name, c);
      }
    }
    return c;
  }

  private void putInCache(String className) {
    synchronized (m_classCache) {
      m_classCache.put(className, null);
    }
  }

  public void clearCaches() {
    synchronized (m_classCache) {
      m_classCache.clear();
    }
  }

  @Override
  public Class<?> loadClass(String className) throws ClassNotFoundException {
    // 1. check primitive classes
    Class<?> c = PRIMITIVE_TYPES.get(className);
    if (c != null) {
      return c;
    }

    // 2. check if class name denotes an array
    int arrayDim = 0;
    while (className.startsWith("[")) {
      className = className.substring(1);
      arrayDim++;
    }
    if (className.matches("L.*;")) {
      className = className.substring(1, className.length() - 1);
    }
    if (arrayDim > 0) {
      c = loadClass(className);
      int[] dimensions = new int[arrayDim];
      c = Array.newInstance(c, dimensions).getClass();
      return c;
    }

    // 3. delegate classes starting with 'java.' to parent class loader
    if (className.startsWith("java.")) {
      return getParent().loadClass(className);
    }

    // 4. check if class is already in the cache
    synchronized (m_classCache) {
      c = m_classCache.get(className);
      if (c != null) {
        return c;
      }
      if (m_classCache.containsKey(className)) {
        throw new ClassNotFoundException(className);
      }
    }

    Set<Bundle> usedBundles = new HashSet<Bundle>();

    // 5. search in best matching bundles based on class and bundle symbolic name
    for (Bundle bundle : m_bundlesSortedByBundleSymbolicNameLenght) {
      if (usedBundles.contains(bundle)) {
        continue;
      }
      if (className.startsWith(bundle.getSymbolicName() + ".")) {
        usedBundles.add(bundle);
        try {
          c = bundle.loadClass(className);
          return putInCache(className, c);
        }
        catch (Exception e) {
          //nop
        }
      }
    }

    // 6. search in active bundles
    for (Bundle bundle : m_bundlesSortedByBundleSymbolicNameLenght) {
      if (usedBundles.contains(bundle)) {
        continue;
      }
      if (bundle.getState() == Bundle.ACTIVE) {
        usedBundles.add(bundle);
        try {
          c = bundle.loadClass(className);
          return putInCache(className, c);
        }
        catch (Exception e) {
          //nop
        }
      }
    }

    // 7. search in remaining bundles
    for (Bundle b : m_bundles) {
      if (usedBundles.contains(b)) {
        continue;
      }
      try {
        c = b.loadClass(className);
        return putInCache(className, c);
      }
      catch (Exception e) {
        //nop
      }
    }

    // 8. try context class loader
    try {
      c = Class.forName(className);
      return putInCache(className, c);
    }
    catch (Exception e) {
      //nop
    }

    // 9. class not found
    putInCache(className);
    throw new ClassNotFoundException(className);
  }

  @Override
  public URL getResource(String name) {
    for (Bundle b : m_bundles) {
      try {
        URL url = b.getResource(name);
        if (url != null) {
          return url;
        }
      }
      catch (Exception e) {
        //nop
      }
    }
    return null;
  }

  @Override
  public InputStream getResourceAsStream(String name) {
    try {
      URL u = getResource(name);
      if (u != null) {
        return u.openStream();
      }
    }
    catch (Exception e) {
      //nop
    }
    return null;
  }

  @Override
  public Enumeration<URL> getResources(String name) throws IOException {
    Vector<URL> urlList = new Vector<URL>();
    for (Bundle b : m_bundles) {
      try {
        Enumeration<URL> en = b.getResources(name);
        if (en != null && en.hasMoreElements()) {
          while (en.hasMoreElements()) {
            URL url = en.nextElement();
            urlList.add(url);
          }
        }
      }
      catch (Exception e) {
        //nop
      }
    }
    return urlList.elements();
  }
}
