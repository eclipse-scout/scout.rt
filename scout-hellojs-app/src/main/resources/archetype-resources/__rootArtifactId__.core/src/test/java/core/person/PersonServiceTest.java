package ${package}.core.person;

import ${package}.data.person.PersonDo;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertThrows;

@RunWith(PlatformTestRunner.class)
public class PersonServiceTest {
  @Test
  public void testLastNameMandatory() {
    PersonService personService = BEANS.get(PersonService.class);

    personService.assertPersonDo(new PersonDo().withLastName("LastName"));

    assertThrows(AssertionException.class, () -> personService.assertPersonDo(null));
    assertThrows(AssertionException.class, () -> personService.assertPersonDo(new PersonDo().withLastName(null)));
    assertThrows(AssertionException.class, () -> personService.assertPersonDo(new PersonDo().withLastName("")));
    assertThrows(AssertionException.class, () -> personService.assertPersonDo(new PersonDo().withLastName("  ")));
  }
}
