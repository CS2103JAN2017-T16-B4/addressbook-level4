package t16b4.yats.logic.commands;

import java.util.List;
import java.util.Optional;

import t16b4.yats.commons.core.Messages;
import t16b4.yats.commons.util.CollectionUtil;
import t16b4.yats.logic.commands.exceptions.CommandException;
import t16b4.yats.model.item.Description;
import t16b4.yats.model.item.Timing;
import t16b4.yats.model.item.Title;
import t16b4.yats.model.item.Task;
import t16b4.yats.model.item.Deadline;
import t16b4.yats.model.item.ReadOnlyItem;
import t16b4.yats.model.item.UniqueItemList;
import t16b4.yats.model.tag.UniqueTagList;

/**
 * Edits the details of an existing person in the address book.
 */
public class EditCommand extends Command {

    public static final String COMMAND_WORD = "edit";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Edits the details of the person identified "
            + "by the index number used in the last person listing. "
            + "Existing values will be overwritten by the input values.\n"
            + "Parameters: INDEX (must be a positive integer) [NAME] [p/PHONE] [e/EMAIL] [a/ADDRESS ] [t/TAG]...\n"
            + "Example: " + COMMAND_WORD + " 1 p/91234567 e/johndoe@yahoo.com";

    public static final String MESSAGE_EDIT_PERSON_SUCCESS = "Edited Person: %1$s";
    public static final String MESSAGE_NOT_EDITED = "At least one field to edit must be provided.";
    public static final String MESSAGE_DUPLICATE_PERSON = "This person already exists in the address book.";

    private final int filteredPersonListIndex;
    private final EditPersonDescriptor editPersonDescriptor;

    /**
     * @param filteredPersonListIndex the index of the person in the filtered person list to edit
     * @param editPersonDescriptor details to edit the person with
     */
    public EditCommand(int filteredPersonListIndex, EditPersonDescriptor editPersonDescriptor) {
        assert filteredPersonListIndex > 0;
        assert editPersonDescriptor != null;

        // converts filteredPersonListIndex from one-based to zero-based.
        this.filteredPersonListIndex = filteredPersonListIndex - 1;

        this.editPersonDescriptor = new EditPersonDescriptor(editPersonDescriptor);
    }

    @Override
    public CommandResult execute() throws CommandException {
        List<ReadOnlyItem> lastShownList = model.getFilteredPersonList();

        if (filteredPersonListIndex >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        ReadOnlyItem personToEdit = lastShownList.get(filteredPersonListIndex);
        Task editedPerson = createEditedPerson(personToEdit, editPersonDescriptor);

        try {
            model.updatePerson(filteredPersonListIndex, editedPerson);
        } catch (UniqueItemList.DuplicatePersonException dpe) {
            throw new CommandException(MESSAGE_DUPLICATE_PERSON);
        }
        model.updateFilteredListToShowAll();
        return new CommandResult(String.format(MESSAGE_EDIT_PERSON_SUCCESS, personToEdit));
    }

    /**
     * Creates and returns a {@code Person} with the details of {@code personToEdit}
     * edited with {@code editPersonDescriptor}.
     */
    private static Task createEditedPerson(ReadOnlyItem personToEdit,
                                             EditPersonDescriptor editPersonDescriptor) {
        assert personToEdit != null;

        Title updatedName = editPersonDescriptor.getName().orElseGet(personToEdit::getTitle);
        Deadline updatedPhone = editPersonDescriptor.getPhone().orElseGet(personToEdit::getDeadline);
        Timing updatedEmail = editPersonDescriptor.getEmail().orElseGet(personToEdit::getTiming);
        Description updatedAddress = editPersonDescriptor.getAddress().orElseGet(personToEdit::getDescription);
        UniqueTagList updatedTags = editPersonDescriptor.getTags().orElseGet(personToEdit::getTags);

        return new Task(updatedName, updatedPhone, updatedEmail, updatedAddress, updatedTags);
    }

    /**
     * Stores the details to edit the person with. Each non-empty field value will replace the
     * corresponding field value of the person.
     */
    public static class EditPersonDescriptor {
        private Optional<Title> name = Optional.empty();
        private Optional<Deadline> phone = Optional.empty();
        private Optional<Timing> email = Optional.empty();
        private Optional<Description> address = Optional.empty();
        private Optional<UniqueTagList> tags = Optional.empty();

        public EditPersonDescriptor() {}

        public EditPersonDescriptor(EditPersonDescriptor toCopy) {
            this.name = toCopy.getName();
            this.phone = toCopy.getPhone();
            this.email = toCopy.getEmail();
            this.address = toCopy.getAddress();
            this.tags = toCopy.getTags();
        }

        /**
         * Returns true if at least one field is edited.
         */
        public boolean isAnyFieldEdited() {
            return CollectionUtil.isAnyPresent(this.name, this.phone, this.email, this.address, this.tags);
        }

        public void setName(Optional<Title> name) {
            assert name != null;
            this.name = name;
        }

        public Optional<Title> getName() {
            return name;
        }

        public void setPhone(Optional<Deadline> phone) {
            assert phone != null;
            this.phone = phone;
        }

        public Optional<Deadline> getPhone() {
            return phone;
        }

        public void setEmail(Optional<Timing> email) {
            assert email != null;
            this.email = email;
        }

        public Optional<Timing> getEmail() {
            return email;
        }

        public void setAddress(Optional<Description> address) {
            assert address != null;
            this.address = address;
        }

        public Optional<Description> getAddress() {
            return address;
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
