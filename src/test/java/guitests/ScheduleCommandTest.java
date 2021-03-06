package guitests;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.teamstbf.yats.commons.core.Messages;
import org.teamstbf.yats.testutil.TestEvent;
import org.teamstbf.yats.testutil.TestUtil;

import guitests.guihandles.EventCardHandle;

//@@author A0102778B

public class ScheduleCommandTest extends TaskManagerGuiTest {

    @Test
    public void schedule() {
        // schedule an event on the same day and check that scheduler can choose
        // correct date/time
        TestEvent[] currentList = td.getTypicalTasks();
        currentList = td.getTypicalTasks();
        assertScheduleTimingSuccess(td.sameDayScheduleChecker, currentList);
        currentList = TestUtil.addEventsToList(currentList, td.sameDayScheduleChecker);

        // schedule an event on a different day and check that scheduler can
        // choose correct date/time
        assertScheduleTimingSuccess(td.nextDayScheduleChecker, currentList);
        currentList = TestUtil.addEventsToList(currentList, td.nextDayScheduleChecker);

        // schedule a floating event
        TestEvent eventToAdd = td.fish;
        assertScheduleSuccess(eventToAdd, currentList);
        currentList = TestUtil.addEventsToList(currentList, eventToAdd);

        // schedule another event
        eventToAdd = td.goon;
        assertScheduleSuccess(eventToAdd, currentList);
        currentList = TestUtil.addEventsToList(currentList, eventToAdd);

        // schedule to empty list
        commandBox.runCommand("reset");
        assertScheduleSuccess(td.fish);

        // invalid command
        commandBox.runCommand("scheduled you");
        assertResultMessage(Messages.MESSAGE_UNKNOWN_COMMAND);
    }

    /*
     * This test checks that schedule is able to add tasls into the list
     */
    private void assertScheduleSuccess(TestEvent eventToAdd, TestEvent... currentList) {
        commandBox.runCommand(eventToAdd.getScheduleCommand());

        // confirm the new card contains the right data
        EventCardHandle addedCard = taskListPanel.navigateToEvent(eventToAdd.getTitle().fullName);
        assertMatching(eventToAdd, addedCard);
        // confirm the list now contains all previous tasks plus the new
        // task
        TestEvent[] expectedList = TestUtil.addEventsToList(currentList, eventToAdd);
        assertTrue(taskListPanel.isListMatchingWithoutOrder(expectedList));
    }

    /*
     * This test checks that the timing scheduled by the scheduling command is
     * correct
     */
    private void assertScheduleTimingSuccess(TestEvent eventToAdd, TestEvent... currentList) {
        commandBox.runCommand(eventToAdd.getScheduleCommand());
        // confirm the new card contains the right data
        EventCardHandle addedCard = taskListPanel.navigateToEvent(eventToAdd.getTitle().toString());
        assertMatching(eventToAdd, addedCard);
        // confirm the list now contains all previous tasks plus the new
        // task with the correct timing
        TestEvent[] expectedList = TestUtil.addEventsToList(currentList, eventToAdd);
        assertTrue(taskListPanel.isListMatchingWithoutOrder(expectedList));
    }
}
