package pisi.unitedmeows.seriex.util.events;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import pisi.unitedmeows.eventapi.event.Event;
import pisi.unitedmeows.eventapi.event.listener.Listener;
import pisi.unitedmeows.eventapi.system.IEventSystem;

public class SeriexEventSystem implements IEventSystem {

	private final Map<Class<?>, Field[]> fieldCache = new HashMap<>();
	private final HashMap<Class<?>, ArrayList<Listener<?>>> listenerCache = new HashMap<>();

	private Field[] getFieldsFromClass(Object instance) {
		return fieldCache.computeIfAbsent(instance.getClass(), Class::getDeclaredFields);
	}

	@Override
	public void subscribeAll(final Object instance) {
		try {
			for (final Field field : getFieldsFromClass(instance)) {
				if (Listener.class.isAssignableFrom(field.getType())) {
					field.setAccessible(true);
					subscribe(instance, (Listener<?>) field.get(instance), field);
				}
			}
		}
		catch (final Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void subscribeAll(final Object instance, final Listener<?>... listeners) {
		try {
			for (final Field field : getFieldsFromClass(instance)) {
				if (Listener.class.isAssignableFrom(field.getType())) {
					field.setAccessible(true);
					final Listener<?> listener = (Listener<?>) field.get(instance);
					if (Arrays.stream(listeners).anyMatch(x -> x == listener)) {
						subscribe(instance, (Listener<?>) field.get(instance), field);
					}
				}
			}
		}
		catch (final Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void subscribe(final Listener<?> listener, final Object instance) {
		try {
			for (final Field field : getFieldsFromClass(instance)) {
				if (Listener.class.isAssignableFrom(field.getType())) {
					field.setAccessible(true);
					if ((Listener<?>) field.get(instance) == listener) {
						subscribe(instance, listener, field);
						break;
					}
				}
			}
		}
		catch (final Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void subscribe(final Object instance, final Listener<?> listener, final Field field) {
		try {
			listener.__setup(instance);
			field.setAccessible(true);
			for (final Class<?> listeningEvent : listener.listeningEvents()) {
				if (!listenerCache.containsKey(listeningEvent)) {
					final ArrayList<Listener<?>> listenerList = new ArrayList<>();
					listenerList.add(listener);
					listenerCache.put(listeningEvent, listenerList);
				} else {
					final ArrayList<Listener<?>> listeners = listenerCache.get(listeningEvent);
					if (listeners != null) {
						listeners.add(listener);
						listeners.sort((o1, o2) -> Integer.compare(o2.getWeight().value(), o1.getWeight().value()));
					}
				}
			}
		}
		catch (final Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void unsubscribe(final Listener<?> listener) {
		try {
			for (Class<?> clazz : listener.listeningEvents()) {
				final ArrayList<Listener<?>> listeners = listenerCache.getOrDefault(clazz, null);
				if (listeners == null) return;
				final Iterator<Listener<?>> iterator = listeners.iterator();
				while (iterator.hasNext()) {
					if (listener == iterator.next()) {
						iterator.remove();
						return;
					}
				}
			}
		}
		catch (final Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void unsubscribeAll(final Object listenerClass) {
		try {
			for (final ArrayList<Listener<?>> value : listenerCache.values()) {
				value.removeIf(x -> x.declared() == listenerClass);
			}
		}
		catch (final Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void fire(final Event event) {
		final ArrayList<Listener<?>> listeners = listenerCache.getOrDefault(event.getClass(), null);
		if (listeners == null) return;
		for (Listener<?> listener : listeners) {
			listener.call(event);
			if (event.stop()) {
				break;
			}
		}
	}
}
