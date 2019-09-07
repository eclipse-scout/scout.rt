package org.eclipse.scout.migration.ecma6;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Predicate;

import org.eclipse.scout.migration.ecma6.model.old.Exemption;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

public final class PathFilters {
  private static Path SRC_MAIN_JS = Paths.get("src/main/js");

  private PathFilters() {
  }

  @SafeVarargs
  public static Predicate<PathInfo> and(Predicate<PathInfo>... predicates) {
    if (predicates.length == 0) {
      return p -> true;
    }
    return p -> Arrays.stream(predicates).allMatch(predicate -> predicate.test(p));
  }

  public static Predicate<PathInfo> withExtension(String extension) {
    return fileInfo -> FileUtility.hasExtension(fileInfo.getPath(), extension);
  }

  public static Predicate<PathInfo> inSrcMainJs() {
    return info -> info.getModuleRelativePath() != null && info.getModuleRelativePath().startsWith(SRC_MAIN_JS);
  }

  public static Predicate<PathInfo> notOneOf(Path... notAcceptedRelativeToModule) {
    return notOneOf(CollectionUtility.hashSet(notAcceptedRelativeToModule));
  }

  public static Predicate<PathInfo> notOneOf(Set<Path> notAcceptedRelativeToModule) {
    return info -> info.getModuleRelativePath() == null || !notAcceptedRelativeToModule.contains(info.getModuleRelativePath());
  }

  public static Predicate<PathInfo> oneOf(Path... acceptedRelativeToModule) {
    return oneOf(CollectionUtility.hashSet(acceptedRelativeToModule));
  }

  public static Predicate<PathInfo> oneOf(Set<Path> acceptedRelativeToModule) {
    return info -> info.getModuleRelativePath() != null && acceptedRelativeToModule.contains(info.getModuleRelativePath());
  }

  public static Predicate<PathInfo> isClass() {
    return info -> info.getPath().getFileName().toString().matches("^[A-Z].*$");
  }

  @Exemption
  public static Predicate<PathInfo> isUtility() {
    return info -> isUtility(info.getPath());
  }

  public static Predicate<PathInfo> isTopLevelEnum() {
    return info -> isTopLevelEnum(info.getPath());
  }

  @Exemption
  public static boolean isUtility(Path path) {
    if (isTopLevelEnum(path)) {
      return false;
    }
    return path.getFileName().toString().matches("^[a-z].*$");
  }

  @Exemption
  public static boolean isTopLevelEnum(Path path) {
    return path.endsWith("scout/form/fields/TreeVisitResult.js")
        || path.endsWith("scout/layout/LayoutConstants.js")
        || path.endsWith("scout/keystroke/keys.js")
        || path.endsWith("scout/menu/MenuDestinations.js")
        || path.endsWith("scout/focus/FocusRule.js")
        || path.endsWith("scout/util/HAlign.js")
        || path.endsWith("studio/util/enums.js");
  }
}
