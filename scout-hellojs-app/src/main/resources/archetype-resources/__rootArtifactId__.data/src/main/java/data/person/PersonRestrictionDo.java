#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.data.person;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("${simpleArtifactName}.PersonRestriction")
public class PersonRestrictionDo extends DoEntity {

  public DoValue<String> firstName() {
    return doValue("firstName");
  }

  public DoValue<String> lastName() {
    return doValue("lastName");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public PersonRestrictionDo withFirstName(String firstName) {
    firstName().set(firstName);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getFirstName() {
    return firstName().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public PersonRestrictionDo withLastName(String lastName) {
    lastName().set(lastName);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getLastName() {
    return lastName().get();
  }
}
