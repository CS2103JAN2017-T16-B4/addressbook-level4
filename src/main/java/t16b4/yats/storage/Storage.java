package t16b4.yats.storage;

import java.io.IOException;
import java.util.Optional;

import t16b4.yats.commons.events.model.TaskManagerChangedEvent;
import t16b4.yats.commons.events.storage.DataSavingExceptionEvent;
import t16b4.yats.commons.exceptions.DataConversionException;
import t16b4.yats.model.ReadOnlyTaskManager;
import t16b4.yats.model.UserPrefs;

/**
 * API of the Storage component
 */
public interface Storage extends TaskManagerStorage, UserPrefsStorage {

    @Override
    Optional<UserPrefs> readUserPrefs() throws DataConversionException, IOException;

    @Override
    void saveUserPrefs(UserPrefs userPrefs) throws IOException;

    @Override
    String getTaskManagerFilePath();

    @Override
    Optional<ReadOnlyTaskManager> readTaskManager() throws DataConversionException, IOException;

    @Override
    void saveTaskManager(ReadOnlyTaskManager taskManager) throws IOException;

    /**
     * Saves the current version of the YATS to the hard disk.
     *   Creates the data file if it is missing.
     * Raises {@link DataSavingExceptionEvent} if there was an error during saving.
     */
    void handleAddressBookChangedEvent(TaskManagerChangedEvent abce);
}
