package org.teamstbf.yats.logic.commands;

import java.util.Set;

//@@author A0138952W
public class ListCommandLocation extends ListCommand {

    private final Set<String> keywords;

    public ListCommandLocation(Set<String> keywords) {
        this.keywords = keywords;
    }

    @Override
    public CommandResult execute() {
        model.updateFilteredListToShowLocation(keywords);
        return new CommandResult(getMessageForPersonListShownSummary(model.getFilteredTaskList().size()));
    }
}
