package org.teamstbf.yats.model;

import java.util.Set;
import java.util.logging.Logger;

import org.teamstbf.yats.commons.core.ComponentManager;
import org.teamstbf.yats.commons.core.LogsCenter;
import org.teamstbf.yats.commons.core.UnmodifiableObservableList;
import org.teamstbf.yats.commons.events.model.TaskManagerChangedEvent;
import org.teamstbf.yats.commons.util.CollectionUtil;
import org.teamstbf.yats.commons.util.StringUtil;
import org.teamstbf.yats.model.item.Event;
import org.teamstbf.yats.model.item.ReadOnlyEvent;
import org.teamstbf.yats.model.item.UniqueEventList;
import org.teamstbf.yats.model.item.UniqueEventList.DuplicateEventException;
import org.teamstbf.yats.model.item.UniqueEventList.EventNotFoundException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;

/**
 * Represents the in-memory model of the task manager data.
 * All changes to any model should be synchronized.
 */
public class ModelManager extends ComponentManager implements Model {
	private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

	private final TaskManager taskManager;
	private final FilteredList<ReadOnlyEvent> filteredEvents;

	private ObservableList<ReadOnlyEvent> observedEvents;
	private SortedList<ReadOnlyEvent> sortedEvents;
	private boolean toSort = false;
	
	/**
	 * Initializes a ModelManager with the given taskManager and userPrefs.
	 */
	public ModelManager(ReadOnlyTaskManager taskManager, UserPrefs userPrefs) {
		super();
		assert !CollectionUtil.isAnyNull(taskManager, userPrefs);

		logger.fine("Initializing with task manager: " + taskManager + " and user prefs " + userPrefs);

		this.taskManager = new TaskManager(taskManager);
		observedEvents = FXCollections.observableList(this.taskManager.getTaskList());
		filteredEvents = observedEvents.filtered(null);
		sortedEvents = observedEvents.sorted();
	}

	public ModelManager() {
		this(new TaskManager(), new UserPrefs());
	}
	
	@Override
	public void resetData(ReadOnlyTaskManager newData) {
		taskManager.resetData(newData);
		indicateTaskManagerChanged();
	}

	@Override
	public ReadOnlyTaskManager getTaskManager() {
		return taskManager;
	}

	/** Raises an event to indicate the model has changed */
	private void indicateTaskManagerChanged() {
		raise(new TaskManagerChangedEvent(taskManager));
	}

	@Override
	public synchronized void deleteEvent(ReadOnlyEvent target) throws EventNotFoundException {
		taskManager.removeEvent(target);
		indicateTaskManagerChanged();
	}

	@Override
	public synchronized void addEvent(Event event) throws UniqueEventList.DuplicateEventException {
		taskManager.addEvent(event);
		updateFilteredListToShowAll();
		indicateTaskManagerChanged();
	}

	@Override
	public void updateEvent(int filteredEventListIndex, ReadOnlyEvent editedEvent)
			throws UniqueEventList.DuplicateEventException {
		assert editedEvent != null;

		int taskManagerIndex = filteredEvents.getSourceIndex(filteredEventListIndex);
		taskManager.updateEvent(taskManagerIndex, editedEvent);
		indicateTaskManagerChanged();
	}

	@Override
	public void updateEvent(int filteredEventListIndex, Event editedEvent) throws DuplicateEventException {

	}
	
	@Override
	public void setToSortListSwitch() {
		this.toSort = true;
	}
	
	@Override
	public void unSetToSortListSwitch() {
		this.toSort =  false;
	}
	
	@Override
	public boolean getSortListSwitch() {
		return toSort;
	}

	//=========== Filtered Event List Accessors =============================================================
	
	@Override
	public UnmodifiableObservableList<ReadOnlyEvent> getFilteredTaskList() {
		return new UnmodifiableObservableList<>(filteredEvents);
	}
	
	@Override
	public UnmodifiableObservableList<ReadOnlyEvent> getSortedTaskList() {
		return new UnmodifiableObservableList<ReadOnlyEvent>(sortedEvents);
	}

	@Override
	public void updateFilteredListToShowAll() {
		filteredEvents.setPredicate(null);
	}

	@Override
	public void updateFilteredEventList(Set<String> keywords) {
		updateFilteredEventList(new PredicateExpression(new NameQualifier(keywords)));
	}
	
	@Override
	public void updateSortedEventList() {
		updateSortedEventListByTitle();
	}
	
	private void updateSortedEventListByTitle() {
		sortedEvents.sorted();
	}

	private void updateFilteredEventList(Expression expression) {
		filteredEvents.setPredicate(expression::satisfies);
	}

	//========== Inner classes/interfaces used for filtering =================================================

	interface Expression {
		boolean satisfies(ReadOnlyEvent event);
		String toString();
	}

	private class PredicateExpression implements Expression {

		private final Qualifier qualifier;

		PredicateExpression(Qualifier qualifier) {
			this.qualifier = qualifier;
		}

		@Override
		public boolean satisfies(ReadOnlyEvent event) {
			return qualifier.run(event);
		}

		@Override
		public String toString() {
			return qualifier.toString();
		}
	}

	interface Qualifier {
		boolean run(ReadOnlyEvent event);
		String toString();
	}

	private class NameQualifier implements Qualifier {
		private Set<String> nameKeyWords;

		NameQualifier(Set<String> nameKeyWords) {
			this.nameKeyWords = nameKeyWords;
		}

		@Override
		public boolean run(ReadOnlyEvent event) {
			return nameKeyWords.stream()
					.filter(keyword -> StringUtil.containsWordIgnoreCase(event.getTitle().fullName, keyword))
					.findAny()
					.isPresent();
		}

		@Override
		public String toString() {
			return "title=" + String.join(", ", nameKeyWords);
		}
	}

}
