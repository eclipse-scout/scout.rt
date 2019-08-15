package org.eclipse.scout.migration.ecma6;

import org.eclipse.scout.rt.platform.util.CollectionUtility;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Predicate;

public final class PathFilters {
  private static Path SRC_MAIN_JS = Paths.get("src/main/js");

  private PathFilters(){
  }

  public static Predicate<PathInfo> and(Predicate<PathInfo>... predicates){
    if(predicates.length == 0){
      return p -> true;
    }
    return p ->  Arrays.stream(predicates).allMatch(predicate -> predicate.test(p));
  }

  public static Predicate<PathInfo> withExtension(String extension){
    return fileInfo -> FileUtility.hasExtension(fileInfo.getPath(), extension);
  }

  public static Predicate<PathInfo> inSrcMainJs(){
    return info -> info.getModuleRelativePath() != null && info.getModuleRelativePath().startsWith(SRC_MAIN_JS);
  }

  public static Predicate<PathInfo> notOneOf(Path... notAcceptedRelativeToModule){
    return notOneOf(CollectionUtility.hashSet(notAcceptedRelativeToModule));
  }

  public static Predicate<PathInfo> notOneOf(Set<Path> notAcceptedRelativeToModule){
     return info -> info.getModuleRelativePath() == null || !notAcceptedRelativeToModule.contains(info.getModuleRelativePath());
  }

  public static Predicate<PathInfo> oneOf(Path... acceptedRelativeToModule){
    return oneOf(CollectionUtility.hashSet(acceptedRelativeToModule));
  }

  public static Predicate<PathInfo> oneOf(Set<Path> acceptedRelativeToModule){
    return info -> info.getModuleRelativePath()!= null && acceptedRelativeToModule.contains(info.getModuleRelativePath());
  }

  public static Predicate<PathInfo> isClass(){
    return info -> info.getPath().getFileName().toString().matches("^[A-Z]{1}.*$");
  }

  public static Predicate<PathInfo> isUtility(){
    return info -> info.getPath().getFileName().toString().matches("^[a-z]{1}.*$");
  }

}
