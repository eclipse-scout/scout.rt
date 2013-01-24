/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons.osgi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.ManifestElement;
import org.eclipse.scout.commons.internal.Activator;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;

public final class BundleInspector {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(BundleInspector.class);

  private BundleInspector() {
  }

  /**
   * @return the breath-first traversed list of all bundles that are reachable starting with the rootBundle
   */
  public static Bundle[] getBundleDependencyTree(Bundle rootBundle) {
    ArrayList<Bundle> list = new ArrayList<Bundle>();
    ArrayList<Bundle> parents = new ArrayList<Bundle>();
    if (rootBundle != null) {
      list.add(rootBundle);
      parents.add(rootBundle);
    }
    // breath first tree traversal
    while (parents.size() > 0) {
      ArrayList<Bundle> nextParents = new ArrayList<Bundle>();
      for (Bundle bundle : parents) {
        try {
          ManifestElement[] bundleNames;
          bundleNames = ManifestElement.parseHeader(Constants.REQUIRE_BUNDLE, (String) bundle.getHeaders().get(Constants.REQUIRE_BUNDLE));
          if (bundleNames != null) {
            for (int i = 0; i < bundleNames.length; i++) {
              Bundle child = Platform.getBundle(bundleNames[i].getValue());
              if (child != null && !list.contains(child)) {
                list.add(child);
                nextParents.add(child);
              }
            }
          }
        }
        catch (BundleException e) {
          LOG.warn(null, e);
        }
      }
      parents = nextParents;
    }
    return list.toArray(new Bundle[0]);
  }

  /**
   * Transforms the dependency tree of all installed bundles into a list using the following algorithm:
   * <ul>
   * <li>Compute all leaf bundles and sort them according to the given prefix list.</li>
   * <li>For each leaf bundle, compute the bundle dependency tree, flatten and merge it with the resulting ordered
   * dependency list.</li>
   * </ul>
   * A leaf bundle's dependency list is merged into the resulting ordered list by iterating through the leaf bundle's
   * dependency list. As soon as a common bundle is found in both lists, the already iterated sub list of the leaf
   * bundle's dependency list is added in front of the common bundle in the resulting bundle list. This procedure is
   * repeated until the end of the leaf bundle's dependency list is reached. The potentially available tail list is
   * added to the end of the resulting ordered list.
   * <p>
   * <b>Example</b>
   * 
   * <pre>
   *                      a.bundle1
   *                          |
   *                          |
   *                          |
   *                      a.bundle2
   *                          |
   *     /--------------------+--------------------\
   *     |             |             |             |
   * a.bundle4     a.bundle3     b.bundle1     c.bundle1
   *                                 |             |
   *                             b.bundle2     c.bundle2
   * </pre>
   * 
   * The invocation <code>getOrderedBundleList("c", "b")</code> returns one of the following two resulting lists:
   * <table>
   * <tr>
   * <th>Solution 1</th>
   * <th>Solution 2</th>
   * </tr>
   * <tr>
   * <td>c.bundle2</td>
   * <td>c.bundle2</td>
   * </tr>
   * <tr>
   * <td>c.bundle1</td>
   * <td>c.bundle1</td>
   * </tr>
   * <tr>
   * <td>b.bundle2</td>
   * <td>b.bundle2</td>
   * </tr>
   * <tr>
   * <td>b.bundle1</td>
   * <td>b.bundle1</td>
   * </tr>
   * <tr>
   * <td>a.bundle3</td>
   * <td>a.bundle4</td>
   * </tr>
   * <tr>
   * <td>a.bundle4</td>
   * <td>a.bundle3</td>
   * </tr>
   * <tr>
   * <td>a.bundle2</td>
   * <td>a.bundle2</td>
   * </tr>
   * <tr>
   * <td>a.bundle1</td>
   * <td>a.bundle1</td>
   * </tr>
   * </table>
   * 
   * @param prefixList
   *          list of bundle name prefixes that are used for ordering the bundles in the resulting list.
   * @return list of all bundles, ordered according to the dependency tree and the list of prefixes; all remaining are
   *         added at the end.
   */
  public static Bundle[] getOrderedBundleList(final String... prefixList) {
    Bundle[] allBundles;
    if (Activator.getDefault() != null) {
      allBundles = Activator.getDefault().getBundle().getBundleContext().getBundles();
    }
    else {
      allBundles = new Bundle[0];
    }
    if (allBundles == null || allBundles.length == 0) {
      return new Bundle[0];
    }

    Set<Bundle> leafBundles = new HashSet<Bundle>();
    Map<String, Bundle> allBundleMap = new HashMap<String, Bundle>();
    Map<Bundle, List<Bundle>> bundleDependencies = new HashMap<Bundle, List<Bundle>>();
    for (Bundle bundle : allBundles) {
      allBundleMap.put(bundle.getSymbolicName(), bundle);
      leafBundles.add(bundle);
    }

    // compute leaf bundles and bundle dependencies
    for (Bundle bundle : allBundles) {
      try {
        List<Bundle> bundleDependencyList = new ArrayList<Bundle>();
        bundleDependencies.put(bundle, bundleDependencyList);
        ManifestElement[] bundleNames = ManifestElement.parseHeader(Constants.REQUIRE_BUNDLE, (String) bundle.getHeaders().get(Constants.REQUIRE_BUNDLE));
        if (bundleNames != null) {
          for (ManifestElement bundleName : bundleNames) {
            Bundle dependingBundle = allBundleMap.get(bundleName.getValue());
            leafBundles.remove(dependingBundle);
            if (dependingBundle != null) {
              bundleDependencyList.add(dependingBundle);
            }
          }
        }
      }
      catch (BundleException e) {
        LOG.warn(null, e);
      }
    }

    // sort leaf bundles according to the given prefix list
    List<Bundle> leafBundleList = new ArrayList<Bundle>(leafBundles);
    if (prefixList != null && prefixList.length > 0) {
      Comparator<Bundle> bundleComparator = new P_BundleNamePrefixComparator(prefixList);
      Collections.sort(leafBundleList, bundleComparator);
    }

    // build resulting ordered bundle list
    ArrayList<Bundle> orderedBundles = new ArrayList<Bundle>();
    for (Bundle leafBundle : leafBundleList) {
      ArrayList<Bundle> currentLeafBundleList = new ArrayList<Bundle>();
      ArrayList<Bundle> parents = new ArrayList<Bundle>();
      currentLeafBundleList.add(leafBundle);
      parents.add(leafBundle);
      // breath first tree traversal
      while (parents.size() > 0) {
        ArrayList<Bundle> nextParents = new ArrayList<Bundle>();
        for (Bundle bundle : parents) {
          for (Bundle bundleDep : bundleDependencies.get(bundle)) {
            if (!currentLeafBundleList.contains(bundleDep)) {
              currentLeafBundleList.add(bundleDep);
              if (!orderedBundles.contains(bundleDep)) {
                // Element is already in resulting ordered bundle list hence its dependencies are contained as well.
                // The element is still required in the currentLeafBundleList in order to find the point of
                // insertion in the resulting ordered bundle list.
                nextParents.add(bundleDep);
              }
            }
          }
        }
        parents = nextParents;
      }

      // merge current ordered leaf bundle list with resulting ordered bundle list
      if (orderedBundles.isEmpty()) {
        orderedBundles.addAll(currentLeafBundleList);
      }
      else {
        int curStart = 0;
        int curEnd = 0;
        for (Bundle bundle : currentLeafBundleList) {
          if (orderedBundles.contains(bundle)) {
            if (curStart != curEnd) {
              int insertIndex = orderedBundles.indexOf(bundle);
              orderedBundles.addAll(insertIndex, currentLeafBundleList.subList(curStart, curEnd));
            }
            curEnd++;
            curStart = curEnd;
          }
          else {
            curEnd++;
          }
        }
        if (curStart != curEnd) {
          orderedBundles.addAll(currentLeafBundleList.subList(curStart, curEnd));
        }
      }
    }

    return orderedBundles.toArray(new Bundle[orderedBundles.size()]);
  }

  /**
   * Resolves the given bundle's host or returns itself, if it is not a fragment bundle.
   * 
   * @param bundle
   *          <code>null</code> or an arbitrary bundle.
   * @return Returns <code>null</code> if the given bundle is <code>null</code>, the given bundle itself if its not a
   *         fragment or the fragment's host bundle. If the resolution of a bundle throws an exception <code>null</code>
   *         is returned as well.
   */
  public static Bundle getHostBundle(Bundle bundle) {
    if (bundle == null) {
      return null;
    }
    if (!Platform.isFragment(bundle)) {
      return bundle;
    }
    try {
      ManifestElement[] hostBundles = ManifestElement.parseHeader(Constants.FRAGMENT_HOST, (String) bundle.getHeaders().get(Constants.FRAGMENT_HOST));
      String hostBundleName = hostBundles[0].getValue();
      return Platform.getBundle(hostBundleName);
    }
    catch (Exception e) {
      LOG.warn("Could not resolve host of fragment bundle [" + bundle.getBundleId() + "]", e);
    }
    return null;
  }

  /**
   * Filters all Plug-in bundles. Fragment bundles are replaced by their corresponding host bundle. Every bundle is
   * contained at most one time in the resulting bundle list, at its first position it occurred in the original
   * original bundle list.
   * 
   * @return Returns never <code>null</code>.
   * @since 3.8.2
   */
  public static Bundle[] filterPluginBundles(Bundle... bundles) {
    if (bundles == null) {
      return new Bundle[0];
    }

    List<Bundle> filteredBundles = new ArrayList<Bundle>();
    for (Bundle b : bundles) {
      if (b == null) {
        continue;
      }
      if (Platform.isFragment(b)) {
        b = getHostBundle(b);
        if (b == null) {
          continue;
        }
      }
      if (!filteredBundles.contains(b)) {
        // add the the bundle only if it is not already part of the list (which could happen if a fragment's host was already added before)
        filteredBundles.add(b);
      }
    }
    return filteredBundles.toArray(new Bundle[filteredBundles.size()]);
  }

  private static final class P_BundleNamePrefixComparator implements Comparator<Bundle> {
    private final String[] prefixList;

    private P_BundleNamePrefixComparator(String... prefixList) {
      if (prefixList == null) {
        throw new IllegalArgumentException("prefixList must not be null.");
      }
      this.prefixList = prefixList;
    }

    @Override
    public int compare(Bundle b1, Bundle b2) {
      int b1SortIndex = getBundleSortIndex(b1);
      int b2SortIndex = getBundleSortIndex(b2);
      if (b1SortIndex == b2SortIndex) {
        return 0;
      }
      else if (b1SortIndex < b2SortIndex) {
        return -1;
      }
      else {
        return 1;
      }
    }

    private int getBundleSortIndex(Bundle bundle) {
      for (int i = 0; i < prefixList.length; i++) {
        String symbolicName = bundle.getSymbolicName();
        if (symbolicName.startsWith(prefixList[i])) {
          return i;
        }
      }
      return Integer.MAX_VALUE;
    }
  }

  public abstract static class BundleComparator implements Comparator<Bundle> {

    public BundleComparator() {
    }

    @Override
    public int compare(Bundle b1, Bundle b2) {
      int b1SortIndex = getBundleSortIndex(b1, b1.getSymbolicName());
      int b2SortIndex = getBundleSortIndex(b2, b2.getSymbolicName());
      if (b1SortIndex == b2SortIndex) {
        return 0;
      }
      else if (b1SortIndex < b2SortIndex) {
        return -1;
      }
      else {
        return 1;
      }
    }

    protected abstract int getBundleSortIndex(Bundle bundle, String symbolicName);
  }
}
