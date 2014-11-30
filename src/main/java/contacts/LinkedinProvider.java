package contacts;

import java.util.Collection;

public interface LinkedinProvider {
	
	public static class Person {
		
		private String lastname;

		public String getLastname() {
			return lastname;
		}

		public void setLastname(String lastname) {
			this.lastname = lastname;
		}
		
	}

	Collection<Person> getPersons() throws Exception;

}
