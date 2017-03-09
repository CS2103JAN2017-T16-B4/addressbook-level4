package org.teamstbf.yats.logic.commands;

import java.util.HashSet;
import java.util.Set;

import org.teamstbf.yats.commons.exceptions.IllegalValueException;
import org.teamstbf.yats.logic.commands.exceptions.CommandException;
import org.teamstbf.yats.model.item.Description;
import org.teamstbf.yats.model.item.Event;
import org.teamstbf.yats.model.item.Location;
import org.teamstbf.yats.model.item.Periodic;
import org.teamstbf.yats.model.item.Timing;
import org.teamstbf.yats.model.item.Title;
import org.teamstbf.yats.model.item.UniqueItemList;
import org.teamstbf.yats.model.tag.Tag;
import org.teamstbf.yats.model.tag.UniqueTagList;

/**
 * Adds a task to the TaskManager.
 */
public class AddCommand extends Command {

    public static final String COMMAND_WORD = "add";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds an task or event to the task manager. "
            + "Parameters: task name l/location p/period(none/daily/weekly/monthly) s/START TIME  e/END TIME  d/ description [t/TAG]...\n"
            + "Example: " + COMMAND_WORD
            + " meeting with boss l/work p/daily s/7:00pm  e/9:00pm  "
            + "d/get scolded for being lazy t/kthxbye";

    public static final String MESSAGE_SUCCESS = "New event added: %1$s";
    public static final String MESSAGE_DUPLICATE_EVENT = "This event already exists in the address book";

    private final Event toAdd;

    /**
     * Creates an AddCommand using raw values.
     * @param string2
     * @param string
     *
     * @throws IllegalValueException if any of the raw values are invalid
     */
    public AddCommand(String name, String location, String period, String startTime,
    		String endTime, String description, Set<String> tags)
            throws IllegalValueException {
        final Set<Tag> tagSet = new HashSet<>();
        for (String tagName : tags) {
            tagSet.add(new Tag(tagName));
        }
        this.toAdd = new Event(
                new Title(name),
                new Location(location),
                new Periodic(period),
                new Timing(startTime),
                new Timing(endTime),
                new Description(description),
                new UniqueTagList(tagSet)
        );
    }

    @Override
    public CommandResult execute() throws CommandException {
        assert model != null;
        try {
            model.addEvent(toAdd);
            return new CommandResult(String.format(MESSAGE_SUCCESS, toAdd));
        } catch (UniqueItemList.DuplicatePersonException e) {
            throw new CommandException(MESSAGE_DUPLICATE_EVENT);
        }

    }

}