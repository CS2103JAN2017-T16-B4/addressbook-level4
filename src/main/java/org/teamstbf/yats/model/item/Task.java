package org.teamstbf.yats.model.item;

import java.util.Objects;

import org.teamstbf.yats.commons.util.CollectionUtil;
import org.teamstbf.yats.model.tag.UniqueTagList;

/**
 * Represents a Person in the address book.
 * Guarantees: details are present and not null, field values are validated.
 */
public class Task implements ReadOnlyItem {

	private Title title;
	private Deadline deadline;
	private Timing timing;
	private Description description;
	private Periodic periodic;

	private UniqueTagList tags;

	/**
	 * Every field must be present and not null.
	 */
	public Task(Title title, Deadline deadline, Timing timing, Description description, UniqueTagList tags) {
		assert !CollectionUtil.isAnyNull(title, deadline, timing, description, tags);
		this.title = title;
		this.deadline = deadline;
		this.timing = timing;
		this.description = description;
		this.tags = new UniqueTagList(tags); // protect internal tags from changes in the arg list
	}

	/**
	 * Creates a copy of the given ReadOnlyPerson.
	 */
	public Task(ReadOnlyItem source) {
		this(source.getTitle(), source.getDeadline(), source.getTiming(), source.getDescription(), source.getTags());
	}

	public void setTitle(Title name) {
		assert name != null;
		this.title = name;
	}

	@Override
	public Title getTitle() {
		return title;
	}

	public void setDeadline(Deadline deadline) {
		assert deadline != null;
		this.deadline = deadline;
	}

	@Override
	public Deadline getDeadline() {
		return deadline;
	}

	public void setTiming(Timing timing) {
		assert timing != null;
		this.timing = timing;
	}

	@Override
	public Timing getTiming() {
		return timing;
	}

	public void setDescription(Description description) {
		assert description != null;
		this.description = description;
	}

	@Override
	public Description getDescription() {
		return description;
	}

	public void setPeriodic(Periodic periodic) {
		assert periodic != null;
		this.periodic = periodic;
	}

	@Override
	public Periodic getPeriodic() {
		return periodic;
	}

	@Override
	public UniqueTagList getTags() {
		return new UniqueTagList(tags);
	}

	/**
	 * Replaces this person's tags with the tags in the argument tag list.
	 */
	public void setTags(UniqueTagList replacement) {
		tags.setTags(replacement);
	}

	/**
	 * Updates this person with the details of {@code replacement}.
	 */
	public void resetData(ReadOnlyItem replacement) {
		assert replacement != null;

		this.setTitle(replacement.getTitle());
		this.setDeadline(replacement.getDeadline());
		this.setTiming(replacement.getTiming());
		this.setDescription(replacement.getDescription());
		this.setTags(replacement.getTags());
	}

	@Override
	public boolean equals(Object other) {
		return other == this // short circuit if same object
				|| (other instanceof ReadOnlyItem // instanceof handles nulls
						&& this.isSameStateAs((ReadOnlyItem) other));
	}

	@Override
	public int hashCode() {
		// use this method for custom fields hashing instead of implementing your own
		return Objects.hash(title, deadline, timing, description, tags);
	}

	@Override
	public String toString() {
		return getAsText();
	}


}
