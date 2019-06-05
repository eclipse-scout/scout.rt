#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.data.person;

import org.eclipse.scout.rt.platform.dataobject.DoEntity;
import org.eclipse.scout.rt.platform.dataobject.DoValue;
import org.eclipse.scout.rt.platform.dataobject.TypeName;

@TypeName("${simpleArtifactName}.PersonRestriction")
public class PersonRestrictionDo extends DoEntity {
  public DoValue<String> firstName() {
    return doValue("firstName");
  }

  public PersonRestrictionDo withFirstName(String firstName) {
    firstName().set(firstName);
    return this;
  }

  public String getFirstName() {
    return firstName().get();
  }

  public DoValue<String> lastName() {
    return doValue("lastName");
  }

  public PersonRestrictionDo withLastName(String lastName) {
    lastName().set(lastName);
    return this;
  }

  public String getLastName() {
    return lastName().get();
  }
}
