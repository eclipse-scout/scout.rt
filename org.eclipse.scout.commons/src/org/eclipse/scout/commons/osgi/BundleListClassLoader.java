package org.eclipse.scout.commons.osgi;

import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.internal.Activator;
import org.eclipse.scout.commons.serialization.SerializationUtility;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * Class loader implementation that uses a list of bundles to load classes.
 * 
 * @since 3.8.2
 */
public class BundleListClassLoader extends ClassLoader {

  /**
   * Constant <em>bundleresource</em> taken from
   * org.eclipse.osgi.framework.internal.core.Constants.OSGI_RESOURCE_URL_PROTOCOL
   */
  private static final String OSGI_RESOURCE_URL_PROTOCOL = "bundleresource";

  /**
   * Constant <em>.fwk</em> taken from
   * org.eclipse.osgi.framework.internal.core.BundleResourceHandler.BID_FWKID_SEPARATOR
   */
  private static final Pattern BUNDLE_ID_URL_HOST_NAME_PATTERN = Pattern.compile("(\\d+)" + Pattern.quote(".fwk") + ".*");

  /** table mapping primitive type names to corresponding class objects */
  private static final Map<String, Class<?>> PRIMITIVE_TYPES = new HashMap<String, Class<?>>();
  static {
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

  private static final String BUNDLE_INCLUDE_FILTER_PROPERTY = "org.eclipse.scout.commons.osgi.BundleListClassLoader#includeBundles";
  private static final String BUNDLE_EXCLUDE_FILTER_PROPERTY = "org.eclipse.scout.commons.osgi.BundleListClassLoader#excludeBundles";
  private static final String REGEX_MARKER = "regex:";

  private final ClassLoader m_parentClassLoader;
  private final String[] m_bundleOrderPrefixes;
  private final BundleContext m_bundleContext;
  private final Bundle[] m_bundles;

  private final boolean m_useResourceFiltering;
  private final boolean m_useResourceCaching;

  private final Map<String, Class<?>> m_classCache = new ConcurrentHashMap<String, Class<?>>();
  private final Map<String, List<URL>> m_resourceCache = new ConcurrentHashMap<String, List<URL>>();

  private final ThreadLocal<Set<String>> LOOP_DETECTOR = new ThreadLocal<Set<String>>() {
    @Override
    protected Set<String> initialValue() {
      return new HashSet<String>();
    }
  };

  public BundleListClassLoader(ClassLoader parent, Bundle... bundles) {
    super(parent);
    m_parentClassLoader = parent != null ? parent : new ClassLoader(Object.class.getClassLoader()) {
      // boot loader
    };
    if (bundles == null || bundles.length == 0) {
      throw new IllegalArgumentException("bundle list must not be null or empty");
    }

    // filter given list of bundles.
    m_bundleContext = Activator.getDefault().getBundle().getBundleContext();
    String bundleIncludeFilter = m_bundleContext.getProperty(BUNDLE_INCLUDE_FILTER_PROPERTY);
    String bundleExcludeFilter = m_bundleContext.getProperty(BUNDLE_EXCLUDE_FILTER_PROPERTY);
    Pattern[] bundleIncludePatterns = parseFilterPatterns(bundleIncludeFilter);
    Pattern[] bundleExcludePatterns = parseFilterPatterns(bundleExcludeFilter);
    List<Bundle> filteredBundleList = new ArrayList<Bundle>();
    for (Bundle b : BundleInspector.filterPluginBundles(bundles)) {
      if (accept(b.getSymbolicName(), bundleIncludePatterns, bundleExcludePatterns)) {
        filteredBundleList.add(b);
      }
    }
    if (filteredBundleList.isEmpty()) {
      throw new IllegalArgumentException("filtered bundle list must not be empty. [bundles=" + Arrays.toString(bundles) + "]");
    }
    m_bundles = filteredBundleList.toArray(new Bundle[filteredBundleList.size()]);
    //
    m_useResourceFiltering = SerializationUtility.isUseBundleOrderPrefixListAsResourceFilterEnabled();
    m_useResourceCaching = SerializationUtility.isResourceUrlCachingInBundleListClassLoaderEnabled();
    m_bundleOrderPrefixes = SerializationUtility.getBundleOrderPrefixes();
  }

  @Override
  protected Class<?> loadClass(String className, boolean resolve) throws ClassNotFoundException {
    final Set<String> prev = LOOP_DETECTOR.get();
    if (prev.contains(className)) {
      throw new ClassNotFoundException(className);
    }

    try {
      prev.add(className);
      return doLoadClass(className);
    }
    finally {
      prev.remove(className);
      if (prev.isEmpty()) {
        LOOP_DETECTOR.remove();
      }
    }
  }

  private Class<?> doLoadClass(String className) throws ClassNotFoundException {
    // check the cache
    final Class<?> fromCache = m_classCache.get(className);
    if (fromCache == P_Unknown.class) {
      throw new ClassNotFoundException(className);
    }
    if (fromCache != null) {
      return fromCache;
    }

    // special handling for primitives
    if (PRIMITIVE_TYPES.containsKey(className)) {
      return PRIMITIVE_TYPES.get(className);
    }

    // special handling for arrays
    final Class<?> arrayClass = tryLoadAsArray(className);
    if (arrayClass != null) {
      return arrayClass;
    }

    // try the loading from the m_bundles
    final Class<?> fromBundles = tryLoadFromBundles(className);
    if (fromBundles != null) {
      return fromBundles;
    }

    // try parent
    try {
      final Class<?> fromParent = m_parentClassLoader.loadClass(className);
      if (fromParent != null) {
        m_classCache.put(className, fromParent);
        return fromParent;
      }
    }
    catch (ClassNotFoundException e) {
      // ignore
    }

    m_classCache.put(className, P_Unknown.class); // remember that we do not know this class
    throw new ClassNotFoundException(className);
  }

  private Class<?> tryLoadAsArray(String className) throws ClassNotFoundException {
    int arrayDim = 0;
    String memberClassName = className;

    while (memberClassName.startsWith("[")) {
      arrayDim += 1;
      memberClassName = memberClassName.substring(1);
    }
    if (memberClassName.matches("L.*;")) {
      memberClassName = memberClassName.substring(1, memberClassName.length() - 1);
    }
    if (arrayDim > 0) {
      final Class<?> memberClass = loadClass(memberClassName);
      final int[] dimensions = new int[arrayDim];
      final Class<?> arrayClass = Array.newInstance(memberClass, dimensions).getClass();
      m_classCache.put(className, arrayClass);
      return arrayClass;
    }

    return null;
  }

  private Class<?> tryLoadFromBundles(String className) {
    final Set<Bundle> triedBundles = new HashSet<Bundle>();

    // try m_bundles with matching names
    for (Bundle bundle : m_bundles) {
      if (className.startsWith(bundle.getSymbolicName())) {
        final Class<?> candidate = tryLoadFromSingleBundle(className, bundle, triedBundles);
        if (candidate != null) {
          return candidate;
        }
      }
    }

    // try active m_bundles
    for (Bundle bundle : m_bundles) {
      if (bundle.getState() == Bundle.ACTIVE) {
        final Class<?> candidate = tryLoadFromSingleBundle(className, bundle, triedBundles);
        if (candidate != null) {
          return candidate;
        }
      }
    }

    // try remaining m_bundles
    for (Bundle bundle : m_bundles) {
      final Class<?> candidate = tryLoadFromSingleBundle(className, bundle, triedBundles);
      if (candidate != null) {
        return candidate;
      }
    }

    return null;
  }

  private Class<?> tryLoadFromSingleBundle(String className, Bundle bundle, Set<Bundle> triedBundles) {
    if (triedBundles.contains(bundle)) {
      return null;
    }
    triedBundles.add(bundle);

    try {
      // Querying the same bundle repeatedly should return the same result --> it is ok to "load" some
      //  classes several times. We avoid locks this way, read locks in particular.
      final Class<?> result = bundle.loadClass(className);
      m_classCache.put(className, result);
      return result;
    }
    catch (Exception exc) {
      // ignore
    }
    return null;
  }

  @Override
  public URL getResource(String name) {
    final Enumeration<URL> all = getResources(name);
    if (all != null && all.hasMoreElements()) {
      return all.nextElement();
    }
    return null;
  }

  @Override
  public Enumeration<URL> getResources(String resourceName) {
    final Set<String> prev = LOOP_DETECTOR.get();
    if (prev.contains(resourceName)) {
      return null;
    }

    try {
      prev.add(resourceName);
      return doGetResources(resourceName);
    }
    finally {
      prev.remove(resourceName);
      if (prev.isEmpty()) {
        LOOP_DETECTOR.remove();
      }
    }
  }

  private Enumeration<URL> doGetResources(String resourceName) {
    final List<URL> fromCache = m_resourceCache.get(resourceName);
    if (fromCache != null) {
      return new P_ListEnumeration(fromCache);
    }

    // collect URLs from the m_bundles
    final List<URL> all = new ArrayList<URL>();
    for (Bundle b : m_bundles) {
      try {
        final Enumeration<?> en = b.getResources(resourceName);
        while (en != null && en.hasMoreElements()) {
          all.add((URL) en.nextElement());
        }
      }
      catch (Exception e) {
        //nop
      }
    }

    // filter
    final List<URL> filtered = filterResources(all);

    if (m_useResourceCaching) {
      m_resourceCache.put(resourceName, filtered);
    }
    return new P_ListEnumeration(filtered);
  }

  private List<URL> filterResources(List<URL> all) {
    if (!m_useResourceFiltering) {
      return all;
    }

    final List<URL> customUrlList = new ArrayList<URL>();

    for (URL url : all) {
      if (isUrlFromBundlePrefixes(url)) {
        customUrlList.add(url);
      }
    }

    if (customUrlList.size() > 0) {
      return customUrlList;
    }

    return all;
  }

  /**
   * return true if resource {@link URL} is located in a bundle from the list of bundleOrderPrefixes
   */
  private boolean isUrlFromBundlePrefixes(URL resource) {
    if (!OSGI_RESOURCE_URL_PROTOCOL.equalsIgnoreCase(resource.getProtocol()) || resource.getHost() == null) {
      return false;
    }

    Matcher m = BUNDLE_ID_URL_HOST_NAME_PATTERN.matcher(resource.getHost());
    if (!m.matches()) {
      return false;
    }

    long bundleId = Long.valueOf(m.group(1));
    Bundle bundle = m_bundleContext.getBundle(bundleId);
    if (bundle != null) {
      for (String bundlePrefix : m_bundleOrderPrefixes) {
        if (StringUtility.contains(bundle.getSymbolicName(), bundlePrefix)) {
          return true;
        }
      }
    }
    return false;
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

  private static class P_ListEnumeration implements Enumeration<URL> {
    private final Iterator<URL> m_iterator;

    public P_ListEnumeration(Iterable<URL> inner) {
      m_iterator = inner.iterator();
    }

    @Override
    public boolean hasMoreElements() {
      return m_iterator.hasNext();
    }

    @Override
    public URL nextElement() {
      return m_iterator.next();
    }
  }

  /**
   * Marker class used for caching unknown classes ({@link ConcurrentHashMap} does not accept <code>null</code> keys or
   * values).
   */
  private static class P_Unknown {
  }
}
