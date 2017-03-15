package org.teamstbf.yats.logic.commands;

import java.util.List;
import java.util.Optional;

import org.teamstbf.yats.commons.core.Messages;
import org.teamstbf.yats.commons.util.CollectionUtil;
import org.teamstbf.yats.logic.commands.exceptions.CommandException;
import org.teamstbf.yats.model.item.Description;
import org.teamstbf.yats.model.item.Event;
import org.teamstbf.yats.model.item.Location;
import org.teamstbf.yats.model.item.Periodic;
import org.teamstbf.yats.model.item.ReadOnlyEvent;
import org.teamstbf.yats.model.item.Schedule;
import org.teamstbf.yats.model.item.Title;
import org.teamstbf.yats.model.item.UniqueEventList;
import org.teamstbf.yats.model.tag.UniqueTagList;

/**
 * Edits the details of an existing task in the task scheduler.
 */
public class EditCommand extends Command {

    public static final String COMMAND_WORD = "edit";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Edits the details of the task identified "
            + "by the index number used in the last task listing. "
            + "Existing values will be overwritten by the input values.\n"
            + "Parameters: INDEX (must be a positive integer) [s/START_TIME] [e/END_TIME] [d/DESCRIPTION] [t/TAGS]...\n"
            + "Example: " + COMMAND_WORD + " 1 s/02-02-2017 t/school";

    public static final String MESSAGE_EDIT_TASK_SUCCESS = "Edited Task: %1$s";
    public static final String MESSAGE_NOT_EDITED = "At least one field to edit must be provided.";
    public static final String MESSAGE_DUPLICATE_TASK = "This task already exists in the address book.";

    private final int filteredTaskListIndex;
    private final EditTaskDescriptor editTaskDescriptor;

    /**
     * @param filteredTaskListIndex the index of the task in the filtered task list to edit
     * @param editTaskDescriptor details to edit the task
     */
    public EditCommand(int filteredTaskListIndex, EditTaskDescriptor editTaskDescriptor) {
        assert filteredTaskListIndex > 0;
        assert editTaskDescriptor != null;

        // converts filteredTaskListIndex from one-based to zero-based.
        this.filteredTaskListIndex = filteredTaskListIndex - 1;

        this.editTaskDescriptor = new EditTaskDescriptor(editTaskDescriptor);
    }

    @Override
    public CommandResult execute() throws CommandException {
        List<ReadOnlyEvent> lastShownList = model.getFilteredTaskList();

        if (filteredTaskListIndex >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_TASK_DISPLAYED_INDEX);
        }

        ReadOnlyEvent taskToEdit = lastShownList.get(filteredTaskListIndex);
        Event editedTask = createEditedTask(taskToEdit, editTaskDescriptor);

        try {
            model.updateEvent(filteredTaskListIndex, editedTask);
        } catch (UniqueEventList.DuplicateEventException dpe) {
            throw new CommandException(MESSAGE_DUPLICATE_TASK);
        }
        model.updateFilteredListToShowAll();
        return new CommandResult(String.format(MESSAGE_EDIT_TASK_SUCCESS, taskToEdit));
    }

    /**
     * Creates and returns a {@code Task} with the details of {@code taskToEdit}
     * edited with {@code editTaskDescriptor}.
     */
    private static Event createEditedTask(ReadOnlyEvent taskToEdit,
                                             EditTaskDescriptor editTaskDescriptor) {
        assert taskToEdit != null;

        Title updatedName = editTaskDescriptor.getName().orElseGet(taskToEdit::getTitle);
        Location updatedLocation = editTaskDescriptor.getLocation().orElseGet(taskToEdit::getLocation);
        Schedule updatedStartTime = editTaskDescriptor.getStartTime().orElseGet(taskToEdit::getStartTime);
        Schedule updatedEndTime = editTaskDescriptor.getEndTime().orElseGet(taskToEdit::getEndTime);
        Description updatedDescription = editTaskDescriptor.getDescription().orElseGet(taskToEdit::getDescription);
        Periodic updatedPeriodic = editTaskDescriptor.getPeriodic().orElseGet(taskToEdit::getPeriod);
        UniqueTagList updatedTags = editTaskDescriptor.getTags().orElseGet(taskToEdit::getTags);

        return new Event(updatedName, updatedLocation, updatedPeriodic, updatedStartTime,
        		updatedEndTime, updatedDescription, updatedTags);
    }

    /**
     * Stores the details to edit the person with. Each non-empty field value will replace the
     * corresponding field value of the person.
     */
    public static class EditTaskDescriptor {
        private Optional<Title> name = Optional.empty();
        private Optional<Location> location = Optional.empty();
        private Optional<Schedule> startTime = Optional.empty();
        private Optional<Schedule> endTime = Optional.empty();
        private Optional<Description> description = Optional.empty();
        private Optional<Periodic> periodic = Optional.empty();
        private Optional<UniqueTagList> tags = Optional.empty();

        public EditTaskDescriptor() {}

        public EditTaskDescriptor(EditTaskDescriptor toCopy) {
            this.name = toCopy.getName();
            this.location = toCopy.getLocation();
            this.startTime = toCopy.getStartTime();
            this.endTime = toCopy.getStartTime();
            this.description = toCopy.getDescription();
            this.periodic = toCopy.getPeriodic();
            this.tags = toCopy.getTags();
        }

        /**
         * Returns true if at least one field is edited.
         */
        public boolean isAnyFieldEdited() {
            return CollectionUtil.isAnyPresent(this.name, this.location, this.startTime, this.description, this.periodic, this.tags);
        }

        public void setName(Optional<Title> name) {
            assert name != null;
            this.name = name;
        }

        public Optional<Title> getName() {
            return name;
        }
        
        public void setLocation(Optional<Location> location) {
        	assert location != null;
        	this.location = location;
        }

        public Optional<Location> getLocation() {
        	return location;
        }

        public void setStartTime(Optional<Schedule> schedule) {
            assert schedule != null;
            this.startTime = schedule;
        }

        public Optional<Schedule> getStartTime() {
            return startTime;
        }
        
        public void setEndTime(Optional<Schedule> schedule) {
        	assert schedule != null;
        	this.endTime = schedule;
        }
        
        public Optional<Schedule> getEndTime() {
        	return endTime;
        }

        public void setDescription(Optional<Description> description) {
            assert description != null;
            this.description = description;
        }

        public Optional<Description> getDescription() {
            return description;
        }

		public void setPeriodic(Optional<Periodic> periodic) {
			assert periodic != null;
        	this.periodic = periodic;		
		}
		
        public Optional<Periodic> getPeriodic() {
        	return periodic;
        }

        public void setTags(Optional<UniqueTagList> tags) {
            assert tags != null;
            this.tags = tags;
        }

        public Optional<UniqueTagList> getTags() {
            return tags;
        }

    }

}
