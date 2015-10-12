package it.paspiz85.contacts;

import java.io.FileReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Summary;

public class FacebookBirthdayCalendar implements Collection<Component> {

	private Map<String, Component> data = new HashMap<String, Component>();

	private Map<String, Component> nameIndex = new HashMap<String, Component>();

	@SuppressWarnings("unchecked")
	public FacebookBirthdayCalendar() throws Exception {
		String file = System.getProperty("facebook.calendar");
		CalendarBuilder calendarBuilder = new CalendarBuilder();
		Calendar calendar = calendarBuilder.build(new FileReader(file));
		addAll(calendar.getComponents());
	}

	public boolean add(Component obj) {
		Component comp = (Component) obj;
		Component old = data.put(getId(comp), comp);
		nameIndex.put(getName(comp), comp);
		return old != null;
	}

	public boolean addAll(Collection<? extends Component> collection) {
		boolean result = false;
		for (Component obj : collection) {
			result |= add(obj);
		}
		return result;
	}

	public void clear() {
		data.clear();
		nameIndex.clear();
	}

	public boolean contains(Object obj) {
		Component comp = (Component) obj;
		return data.containsKey(getId(comp));
	}

	public boolean containsAll(Collection<?> collection) {
		boolean result = true;
		for (Object obj : collection) {
			result &= contains(obj);
		}
		return result;
	}

	public Date getBirthday(Object obj) {
		Component comp = (Component) obj;
		DtStart date = (DtStart) comp.getProperty("DTSTART");
		return date.getDate();
	}

	public Object getById(String id) {
		return data.get(id);
	}

	public Object getByName(String name) {
		return nameIndex.get(name);
	}

	public String getId(Object obj) {
		Component comp = (Component) obj;
		Property uid = comp.getProperty("UID");
		String id = uid.getValue();
		return id.substring(1, id.length() - "@facebook.com".length());
	}

	public String getName(Object obj) {
		Component comp = (Component) obj;
		Summary summary = (Summary) comp.getProperty("SUMMARY");
		String name = summary.getValue();
		name = name.substring(0, name.indexOf("'s Birthday"));
		return name;
	}

	public boolean isEmpty() {
		return size() == 0;
	}

	public Iterator<Component> iterator() {
		final Iterator<Component> i = data.values().iterator();
		return new Iterator<Component>() {

			public boolean hasNext() {
				return i.hasNext();
			}

			public Component next() {
				return i.next();
			}

			public void remove() {
				i.remove();
			}
		};
	}

	public boolean remove(Object obj) {
		Component comp = (Component) obj;
		Component old = data.remove(getId(comp));
		nameIndex.remove(getName(comp));
		return old != null;
	}

	public boolean removeAll(Collection<?> collection) {
		boolean result = false;
		for (Object obj : collection) {
			result |= remove(obj);
		}
		return result;
	}

	public boolean retainAll(Collection<?> collection) {
		boolean result = false;
		for (String id : new HashSet<String>(data.keySet())) {
			boolean found = false;
			for (Object obj : collection) {
				Component comp = (Component) obj;
				if (id.equals(getId(comp))) {
					found = true;
				}
			}
			if (!found) {
				result |= remove(data.get(id));
			}
		}
		return result;
	}

	public int size() {
		return data.size();
	}

	public Object[] toArray() {
		return data.values().toArray();
	}

	public <T> T[] toArray(T[] a) {
		return data.values().toArray(a);
	}
}
