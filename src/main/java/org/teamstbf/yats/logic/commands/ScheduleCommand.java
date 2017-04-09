package org.teamstbf.yats.logic.commands;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.teamstbf.yats.commons.core.EventsCenter;
import org.teamstbf.yats.commons.events.ui.JumpToListRequestEvent;
import org.teamstbf.yats.commons.exceptions.IllegalValueException;
import org.teamstbf.yats.logic.commands.exceptions.CommandException;
import org.teamstbf.yats.model.item.Event;
import org.teamstbf.yats.model.item.ReadOnlyEvent;
import org.teamstbf.yats.model.item.ReadOnlyEventComparatorByStartDate;
import org.teamstbf.yats.model.item.Schedule;
import org.teamstbf.yats.model.item.UniqueEventList;
import org.teamstbf.yats.model.tag.Tag;
import org.teamstbf.yats.model.tag.UniqueTagList;

//@@ author A0102778B

/**
 * Adds a task to the TaskManager.
 */
public class ScheduleCommand extends Command {

	private static final int INVALID_START_VALUE = -1;

	private static final int SCHEDULE_ENDING_HOUR = 18;

	private static final int SCHEDULE_STARTING_HOUR = 8;

	private static final int INDEX_OF_POSITION_LONG = 1;

	private static final int INDEX_OF_START_LONG = 0;

	private static final int MILLISECONDS_PER_MINUTE = 60000;

	private static final int MILLISECONDS_PER_HOUR = 3600000;

	private static final long LAST_TIME_POSSIBLE = 1577846300000L;

	private static final int INITIAL_START_VALUE = INVALID_START_VALUE;

	public static final String COMMAND_WORD = "schedule";

	public static final String MESSAGE_USAGE = COMMAND_WORD + ": Schedule an task or event to the task manager. "
			+ "Parameters: task name [l/location s/START TIME  e/END TIME  d/ description t/TAG] = HOURS\n"
			+ "Example: " + COMMAND_WORD
			+ " meeting with boss l/work p/daily s/7:00pm,18/03/2017  e/9:00pm,18/03/2017  "
			+ "d/get scolded for being lazy t/kthxbye";

	public static final String MESSAGE_SUCCESS = "New event scheduled: %1$s";
	public static final String MESSAGE_SUCCESS_WITH_WARNING = "Warning! Event lasts more than 50 years, please check if valid";
	public static final String MESSAGE_HOURS_INVALID = "The format of hours is invalid - must be a valid long";
	public static final String MESSAGE_TIME_TOO_LONG = "Schedule can only take a timing of at most 10 hours, and it should not be negative - use add for long events";
	private static final long MAXIMUM_EVENT_LENGTH = 36000000L;
	private static final long MINIMUM_EVENT_LENGTH = 0L;
	private final Event toSchedule;
	private String hours;
	private String minutes;

	/**
	 * Creates an addCommand using a map of parameters
	 *
	 * @param addParam
	 * @throws IllegalValueException
	 *             if any of the parameters are invalid
	 */
	public ScheduleCommand(HashMap<String, Object> parameters) throws IllegalValueException {
		final Set<Tag> tagSet = new HashSet<>();
		for (String tagName : (Set<String>) parameters.get("tag")) {
			tagSet.add(new Tag(tagName));
		}
		this.toSchedule = new Event(parameters, new UniqueTagList(tagSet));
		try {
			this.hours = parameters.get("hours").toString();
		} catch (NullPointerException e) {
			this.hours = "0";
		}
		try {
			this.minutes = parameters.get("minutes").toString();
		} catch (NullPointerException e) {
			this.minutes = "0";
		}
		if (this.hours.equals("0") && this.minutes.equals("0")) {
			this.hours = "1";
		}
	}

	@Override
	public CommandResult execute() throws CommandException {
		assert model != null;
		try {
			model.saveImageOfCurrentTaskManager();
			long checkedHours = (long) (Double.parseDouble(this.hours) * MILLISECONDS_PER_HOUR);
			checkedHours = checkedHours + (long) (Double.parseDouble(this.minutes) * MILLISECONDS_PER_MINUTE);
			System.out.println(checkedHours);
			if (checkedHours < MINIMUM_EVENT_LENGTH || checkedHours > MAXIMUM_EVENT_LENGTH) {
				throw new IllegalArgumentException();
			}
			List<ReadOnlyEvent> filteredTaskLists = filterOnlyEventsWithStartEndTime();
			int count = setStartEndIntervalsForNewTask(checkedHours, filteredTaskLists);
			Collections.sort(filteredTaskLists, new ReadOnlyEventComparatorByStartDate());
			model.addEvent(toSchedule);
			model.updateFilteredListToShowSortedStart();
			System.out.println(count);
			EventsCenter.getInstance().post(new JumpToListRequestEvent((int)count));
			return new CommandResult(String.format(MESSAGE_SUCCESS, toSchedule));
		} catch (NumberFormatException e) {
			throw new CommandException(MESSAGE_HOURS_INVALID);
		} catch (IllegalArgumentException e) {
			throw new CommandException(MESSAGE_TIME_TOO_LONG);
		}
	}

	private int setStartEndIntervalsForNewTask(long checkedHours, List<ReadOnlyEvent> filteredTaskLists)
			throws IllegalArgumentException {
		ArrayList<Long> myList = getStartInterval(checkedHours, filteredTaskLists);
		long end = getEndInterval(checkedHours, myList.get(INDEX_OF_START_LONG));
		Schedule startTime = new Schedule(new Date(myList.get(INDEX_OF_START_LONG)));
		Schedule endTime = new Schedule(new Date(end));
		this.toSchedule.setStartTime(startTime);
		this.toSchedule.setEndTime(endTime);
		return myList.get(INDEX_OF_POSITION_LONG).intValue();
	}

	private long getEndInterval(long checkedHours, long start) {
		long end = start + checkedHours;
		return end;
	}
	private ArrayList<Long> getStartInterval(long checkedHours, List<ReadOnlyEvent> filteredTaskLists)

			throws IllegalArgumentException {
		long max = new Date().getTime();
		long curr;
		long start = INITIAL_START_VALUE;
		int startBound = SCHEDULE_STARTING_HOUR;
		int endBound = SCHEDULE_ENDING_HOUR;
		long getStartTime;
		int count = 0;
		for (ReadOnlyEvent event : filteredTaskLists) {
			curr = event.getStartTime().getDate().getTime();
			if (curr > max) {
				if ((curr - max) >= checkedHours) {
					getStartTime = findStartTime(max, checkedHours, curr, startBound, endBound);
					if (getStartTime != INVALID_START_VALUE) {
						start = getStartTime;
						break;
					}
				}
			}
			max = Math.max(max, event.getEndTime().getDate().getTime());
			count++;
		}
		if (start == INITIAL_START_VALUE) {
			start = findFirstStartTime(max, checkedHours, LAST_TIME_POSSIBLE, startBound, endBound);
		}
		ArrayList<Long> myList = new ArrayList<Long>();
		myList.add(start);
		myList.add((long)count);
		return myList;
	}

	private long findStartTime(long start, long checkedHours, long end, int startBound, int endBound) {
		int hoursMin = (int) (checkedHours / 60000L);
		Calendar dayOne = Calendar.getInstance();
		int startTime = convertToHoursMinutes(start, dayOne);
		Calendar dayTwo = Calendar.getInstance();
		int endTime = convertToHoursMinutes(end, dayTwo);
		startBound = startBound * 60;
		endBound = endBound * 60;
		System.out.println(end - start);
		if (end - start <= 28800000L) {
			startTime = Math.max(startTime, startBound);
			if (endTime >= startTime) {
				endTime = Math.min(endTime, endBound);
			} else {
				endTime = Math.max(endTime, endBound);
			}
			System.out.println("less than 8 hours case");
			System.out.println("startTime is" + startTime);
			System.out.println("endTime is" + endTime);
			System.out.println("hoursTime is" + hoursMin);
			if ((endTime - startTime) >= hoursMin) {
				dayOne.set(Calendar.HOUR_OF_DAY, startTime / 60);
				dayOne.set(Calendar.MINUTE, startTime % 60);
				System.out.println("match found");
				return dayOne.getTimeInMillis();
			}
			System.out.println("match not found");
		} else if (end - start >= 122400000L) {
			startTime = Math.max(startTime, startBound);
			if ((endBound - startTime) >= hoursMin) {
				dayOne.set(Calendar.HOUR_OF_DAY, startTime / 60);
				dayOne.set(Calendar.MINUTE, startTime % 60);
				return dayOne.getTimeInMillis();
			} else {
				dayOne.set(Calendar.HOUR_OF_DAY, 8);
				dayOne.set(Calendar.MINUTE, 0);
				dayOne.add(Calendar.DATE, 1);
				return dayOne.getTimeInMillis();
			}
		} else {
			if (startTime < endBound) {
				if (((int) (end - start / 60000L)) > (endBound - startTime)) {
					if ((endBound - startTime) >= hoursMin) {
						dayOne.set(Calendar.HOUR_OF_DAY, startTime / 60);
						dayOne.set(Calendar.MINUTE, startTime % 60);
						return dayOne.getTimeInMillis();
					}
				} else {
					if ((endTime - startTime) >= hoursMin) {
						dayOne.set(Calendar.HOUR_OF_DAY, startTime / 60);
						dayOne.set(Calendar.MINUTE, startTime % 60);
						return dayOne.getTimeInMillis();
					}
				}
			}
			if (endTime > startBound) {
				if ((endTime - startBound) >= hoursMin) {
					dayOne.set(Calendar.HOUR_OF_DAY, 8);
					dayOne.set(Calendar.MINUTE, 0);
					dayOne.add(Calendar.DATE, 1);
					return dayOne.getTimeInMillis();
				}
			}
		}
		return INVALID_START_VALUE;
	}

	private long findFirstStartTime(long start, long checkedHours, long end, int startBound, int endBound) {
		startBound = startBound * 60;
		endBound = endBound * 60;
		Calendar dayOne = Calendar.getInstance();
		int startTime = convertToHoursMinutes(start, dayOne);
		int hoursMin = (int) (checkedHours / 60000L);
		startTime = Math.max(startTime, startBound);
		int finishedTime = startTime + hoursMin;
		if (finishedTime <= endBound && startTime >= startBound) {
			dayOne.set(Calendar.HOUR_OF_DAY, startTime / 60);
			dayOne.set(Calendar.MINUTE, startTime % 60);
			return dayOne.getTimeInMillis();
		} else {
			dayOne.set(Calendar.HOUR_OF_DAY, 8);
			dayOne.set(Calendar.MINUTE, 0);
			dayOne.add(Calendar.DATE, 1);
			return dayOne.getTimeInMillis();
		}
	}

	private int convertToHoursMinutes(long longTiming, Calendar calendar) {
		calendar.setTimeInMillis(longTiming);
		int hourMinuteRep = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
		return hourMinuteRep;
	}

	private List<ReadOnlyEvent> filterOnlyEventsWithStartEndTime() {
		List<ReadOnlyEvent> taskLists = model.getFilteredTaskList();
		List<ReadOnlyEvent> filterTaskLists = new ArrayList<ReadOnlyEvent>();
		for (ReadOnlyEvent event : taskLists) {
			if (event.hasStartAndEndTime()) {
				filterTaskLists.add(event);
			}
		}
		return filterTaskLists;
	}
}
