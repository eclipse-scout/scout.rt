package org.eclipse.scout.commons.osgi;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.internal.Activator;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleReference;

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
  private static final ThreadLocal<Set<String>> LOOP_DETECTOR = new ThreadLocal<Set<String>>();
  private static final Enumeration<URL> EMPTY_URL_ENUMERATION = new Enumeration<URL>() {

    @Override
    public boolean hasMoreElements() {
      return false;
    }

    @Override
    public URL nextElement() {
      throw new NoSuchElementException();
    }
  };
  private static final String BUNDLE_INCLUDE_FILTER_PROPERTY = "org.eclipse.scout.commons.osgi.BundleListClassLoader#includeBundles";
  private static final String BUNDLE_EXCLUDE_FILTER_PROPERTY = "org.eclipse.scout.commons.osgi.BundleListClassLoader#excludeBundles";
  private static final String REGEX_MARKER = "regex:";

  private static P_FastClassContextFinder s_fastClassContextFinder;
  private static ClassLoader s_myClassLoader;
  static {
    AccessController.doPrivileged(new PrivilegedAction<Object>() {
      @Override
      public Object run() {
        s_fastClassContextFinder = new P_FastClassContextFinder();
        s_myClassLoader = BundleListClassLoader.class.getClassLoader();
        return null;
      }
    });
  }

  private final Bundle[] m_bundles;
  private final Bundle[] m_bundlesSortedByBundleSymbolicNameLenght;
  private final ClassLoader m_parentContextClassLoader;
  private final ReadWriteLock m_cacheLock = new ReentrantReadWriteLock();
  private final Map<String, WeakReference<Class<?>>> m_classCache;

  private final PrivilegedAction<List<ClassLoader>> m_getClassLoaderImplPrivilegedAction = new PrivilegedAction<List<ClassLoader>>() {
    @Override
    public List<ClassLoader> run() {
      return getClassLoaderContextImpl();
    }
  };

  public BundleListClassLoader(ClassLoader parent, Bundle... bundles) {
    super(parent);
    m_parentContextClassLoader = parent != null ? parent : new ClassLoader(Object.class.getClassLoader()) {
      // boot classloader
    };
    if (bundles == null || bundles.length == 0) {
      throw new IllegalArgumentException("bundle list must not be null or empty");
    }
    // filter given list of bundles.
    String bundleIncludeFilter = Activator.getDefault().getBundle().getBundleContext().getProperty(BUNDLE_INCLUDE_FILTER_PROPERTY);
    String bundleExcludeFilter = Activator.getDefault().getBundle().getBundleContext().getProperty(BUNDLE_EXCLUDE_FILTER_PROPERTY);
    Pattern[] bundleIncludePatterns = parseFilterPatterns(bundleIncludeFilter);
    Pattern[] bundleExcludePatterns = parseFilterPatterns(bundleExcludeFilter);
    List<Bundle> filteredBundleList = new ArrayList<Bundle>();
    for (Bundle b : BundleInspector.filterPluginBundles(bundles)) {
      if (accept(b.getSymbolicName(), bundleIncludePatterns, bundleExcludePatterns)) {
        filteredBundleList.add(b);
      }
    }
    m_bundles = filteredBundleList.toArray(new Bundle[filteredBundleList.size()]);
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
    m_classCache = new HashMap<String, WeakReference<Class<?>>>();
  }

  private Class<?> putInCache(String name, Class<?> c) {
    m_cacheLock.writeLock().lock();
    try {
      m_classCache.put(name, new WeakReference<Class<?>>(c));
    }
    finally {
      m_cacheLock.writeLock().unlock();
    }
    return c;
  }

  public void clearCaches() {
    m_cacheLock.writeLock().lock();
    try {
      m_classCache.clear();
    }
    finally {
      m_cacheLock.writeLock().unlock();
    }
  }

  @Override
  public Class<?> loadClass(String className) throws ClassNotFoundException {
    if (!registerLoadingItem(className)) {
      throw new ClassNotFoundException(className);
    }
    try {
      return doLoadClass(className);
    }
    finally {
      unregisterLoadingItem(className);
    }
  }

  protected Class<?> doLoadClass(String className) throws ClassNotFoundException {
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
      return m_parentContextClassLoader.loadClass(className);
    }

    // 4. check if class is already in the cache
    m_cacheLock.readLock().lock();
    try {
      WeakReference<Class<?>> ref = m_classCache.get(className);
      if (ref != null) {
        c = ref.get();
        if (c != null) {
          return c;
        }
      }
      if (m_classCache.containsKey(className)) {
        throw new ClassNotFoundException(className);
      }
    }
    finally {
      m_cacheLock.readLock().unlock();
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
      // do not call super.loadClass because it checks the native cache (see eclipse equinox bug 127963)
      c = m_parentContextClassLoader.loadClass(className);
      return putInCache(className, c);
    }
    catch (Exception e) {
      //nop
    }

    // 9. class not found
    putInCache(className, null);
    throw new ClassNotFoundException(className);
  }

  @Override
  public URL getResource(String name) {
    if (!registerLoadingItem(name)) {
      return null;
    }
    try {
      List<ClassLoader> clList = getClassLoaderContext();
      for (ClassLoader cl : clList) {
        URL res = cl.getResource(name);
        if (res != null) {
          return res;
        }
      }
      return super.getResource(name);
    }
    finally {
      unregisterLoadingItem(name);
    }
  }

  @Override
  protected Enumeration<URL> findResources(String name) throws IOException {
    if (!registerLoadingItem(name)) {
      return EMPTY_URL_ENUMERATION;
    }
    try {
      List<ClassLoader> clList = getClassLoaderContext();
      for (ClassLoader cl : clList) {
        Enumeration<URL> res = cl.getResources(name);
        if (res != null && res.hasMoreElements()) {
          return res;
        }
      }
      return super.findResources(name);
    }
    finally {
      unregisterLoadingItem(name);
    }
  }

  private List<ClassLoader> getClassLoaderContext() {
    if (System.getSecurityManager() != null) {
      return AccessController.doPrivileged(m_getClassLoaderImplPrivilegedAction);
    }
    return getClassLoaderContextImpl();
  }

  private List<ClassLoader> getClassLoaderContextImpl() {
    Class<?>[] currentStack = s_fastClassContextFinder.getClassContext();
    List<ClassLoader> result = new ArrayList<ClassLoader>(1);
    ClassLoader prevCl = null;
    for (Class<?> classOnStack : currentStack) {
      if (classOnStack == BundleListClassLoader.class || classOnStack == P_FastClassContextFinder.class) {
        continue;
      }
      ClassLoader cl = classOnStack.getClassLoader();
      if (cl == null || cl == this) {
        continue;
      }
      if (prevCl != cl && isDistinctClassLoaderHierarchy(cl)) {
        prevCl = cl;
        result.add(cl);
      }
      if (cl == s_myClassLoader || cl instanceof BundleReference) {
        // Stop search at our own class loader or the first bundle class loader in the stack.
        // (Implementation note: because "BundleClassLoader" is an internal class, we check
        // for "BundleReference" instead)
        break;
      }
    }
    return result;
  }

  private boolean isDistinctClassLoaderHierarchy(ClassLoader cl) {
    if (cl == null || cl == getParent()) {
      return false;
    }
    for (ClassLoader parent = cl.getParent(); parent != null; parent = parent.getParent()) {
      if (parent == this) {
        return false;
      }
    }
    // Okay, the given class loader really has nothing ourself, so it is safe to use (without
    // this check, we would risk an endless loop).
    return true;
  }

  private boolean registerLoadingItem(String name) {
    Set<String> loadingItems = LOOP_DETECTOR.get();
    if (loadingItems != null && loadingItems.contains(name)) {
      return false;
    }

    if (loadingItems == null) {
      loadingItems = new HashSet<String>(3);
      LOOP_DETECTOR.set(loadingItems);
    }
    loadingItems.add(name);
    return true;
  }

  private void unregisterLoadingItem(String name) {
    // invariant: register has already been invoked
    LOOP_DETECTOR.get().remove(name);
  }

  /**
   * Checks if the given string is included in the list of include patterns and that it is not excluded by the list of
   * exclude patterns. If the include or exclude pattern list is null or empty, the string is assumed to be included and
   * not excluded, respectively.
   */
  private static boolean accept(String s, Pattern[] includePatterns, Pattern[] excludePatterns) {
    if (s == null) {
      return false;
    }
    boolean included = true;
    boolean excluded = false;
    if (includePatterns != null && includePatterns.length > 0) {
      included = false;
      for (Pattern p : includePatterns) {
        if (p.matcher(s).matches()) {
          included = true;
          break;
        }
      }
    }
    if (included && excludePatterns != null && excludePatterns.length > 0) {
      for (Pattern p : excludePatterns) {
        if (p.matcher(s).matches()) {
          excluded = true;
          break;
        }
      }
    }
    return included && !excluded;
  }

  /**
   * Parses a comma-separated list of filter patterns. A filter pattern is either a wildcard pattern or a regular
   * expression. Latter must be prefixed by <em>regex:</em>
   */
  private static Pattern[] parseFilterPatterns(String filter) {
    if (filter == null) {
      return null;
    }
    List<Pattern> patterns = new ArrayList<Pattern>();
    for (String f : filter.split(",")) {
      f = f.trim();
      if (f.length() > 0) {
        try {
          f = toRegexPattern(f);
          Pattern pattern = Pattern.compile(f);
          patterns.add(pattern);
        }
        catch (Exception e) {
          System.err.println("invalid filter pattern: " + e);
        }
      }
    }
    if (patterns.isEmpty()) {
      return null;
    }
    return patterns.toArray(new Pattern[patterns.size()]);
  }

  /**
   * Transforms the given string into a regular expression pattern. The string is assumed to be a wildcard pattern or
   * already a regular expression pattern. The latter must be prefixed by <em>regex:</em>.
   */
  private static String toRegexPattern(String s) {
    if (s == null) {
      return null;
    }
    String pattern = s.trim();
    if (pattern.startsWith(REGEX_MARKER)) {
      return pattern.substring(REGEX_MARKER.length());
    }
    pattern = pattern.replaceAll("[.]", "\\\\.");
    pattern = pattern.replaceAll("[*]", ".*");
    pattern = pattern.replaceAll("[?]", ".");
    return pattern;
  }

  /**
   * Helper class which allows public access to {@link SecurityManager}s protected <code>getClassContext()</code>
   * method, which is faster than {@link Thread#getStackTrace()} (according to http://stackoverflow.com/a/2924426).
   */
  private static final class P_FastClassContextFinder extends SecurityManager {

    @Override
    public Class<?>[] getClassContext() {
      return super.getClassContext();
    }
  }
}
