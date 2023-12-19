#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.data.person;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("${simpleArtifactName}.PersonResponse")
public class PersonResponse extends DoEntity {

  public DoList<PersonDo> items() {
    return doList("items");
  }

  public PersonResponse withItem(PersonDo person) {
    items().get().add(person);
    return this;
  }

  public PersonResponse withItems(Stream<PersonDo> persons) {
    items().clear();
    List<PersonDo> personDos = items().get();
    persons.forEachOrdered(personDos::add);
    return this;
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public PersonResponse withItems(Collection<? extends PersonDo> items) {
    items().updateAll(items);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public PersonResponse withItems(PersonDo... items) {
    items().updateAll(items);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<PersonDo> getItems() {
    return items().get();
  }
}
