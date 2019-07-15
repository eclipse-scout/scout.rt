package org.eclipse.scout.migration.ecma6;

import org.eclipse.scout.rt.platform.util.CollectionUtility;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Predicate;

public final class PathFilters {

  private PathFilters(){
  }

  public static Predicate<Path> and(Predicate<Path>... predicates){
    if(predicates.length == 0){
      return p -> true;
    }
    return p ->  Arrays.stream(predicates).allMatch(predicate -> predicate.test(p));
  }

  public static Predicate<Path> withExtension(String extension){
    return p -> FileUtility.hasExtension(p, extension);
  }

  public static Predicate<Path> inSrcMainJs(){
    return p -> p.toString().contains(Paths.get("src/main/js").toString());
  }

  public static Predicate<Path> notOneOf(Path... notAccepted){
    return notOneOf(CollectionUtility.hashSet(notAccepted));
  }

  public static Predicate<Path> notOneOf(Set<Path> notAccepted){
    return p -> !notAccepted.contains(p);
  }

  public static Predicate<Path> oneOf(Path... accepted){
    return oneOf(CollectionUtility.hashSet(accepted));
  }

  public static Predicate<Path> oneOf(Set<Path> accepted){
    return p -> accepted.contains(p);
  }

}
