package org.eclipse.scout.migration.ecma6.pathfilter;

import org.eclipse.scout.migration.ecma6.PathInfo;
import org.eclipse.scout.rt.platform.ApplicationScoped;

import java.nio.file.Path;
import java.util.function.Predicate;

@ApplicationScoped
public interface IMigrationPathFilter extends Predicate<PathInfo> {

}
