package org.teamstbf.yats.logic.parser;

import static org.teamstbf.yats.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.Optional;

import org.teamstbf.yats.logic.commands.Command;
import org.teamstbf.yats.logic.commands.IncorrectCommand;
import org.teamstbf.yats.logic.commands.MarkUndoneCommand;

/**
 * Parses input arguments and creates a new MarkUndoneCommand object
 */
// @@author A0139448U
public class MarkUndoneCommandParser {
	/**
	 * Parses the given {@code String} of arguments in the context of the MarkDoneCommand
	 * and returns an MarkDoneCommand object for execution.
	 */
	public Command parse(String args) {
		Optional<Integer> index = ParserUtil.parseIndex(args);
		if (!index.isPresent()) {
			return new IncorrectCommand(
					String.format(MESSAGE_INVALID_COMMAND_FORMAT, MarkUndoneCommand.MESSAGE_USAGE));
		}

		return new MarkUndoneCommand(index.get());
	}

}