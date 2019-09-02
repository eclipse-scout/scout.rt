package org.eclipse.scout.migration.ecma6.pathfilter;

import java.util.function.Predicate;

import org.eclipse.scout.migration.ecma6.PathInfo;
import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * This filter is used for debug reasons. If an implementation is found only files matching this filter are migrated. If
 * no implementation is found all files are migrated.
 */
@ApplicationScoped
public interface IMigrationIncludePathFilter extends Predicate<PathInfo> {

}
