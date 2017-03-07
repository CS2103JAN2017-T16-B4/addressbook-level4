package seedu.address.logic.commands;


/**
 * Lists all persons in the address book to the user.
 */
public class ListCommand extends Command {

    public static final String COMMAND_WORD = "list";
    public static final String COMMAND_WORD_SUFFIX_TITLE = "list title";
    public static final String COMMAND_WORD_SUFFIX_DATE = "list date";
    public static final String COMMAND_WORD_SUFFIX_TAG = "list tag";

    public static final String MESSAGE_SUCCESS = "Listed all items";


    @Override
    public CommandResult execute() {
        model.updateFilteredListToShowAll();
        return new CommandResult(MESSAGE_SUCCESS);
    }
}
