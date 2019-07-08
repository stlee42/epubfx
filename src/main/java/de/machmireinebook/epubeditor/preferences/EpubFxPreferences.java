package de.machmireinebook.epubeditor.preferences;

import javafx.event.EventHandler;
import javafx.event.EventType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dlsc.formsfx.model.util.TranslationService;
import com.dlsc.preferencesfx.PreferencesFxEvent;
import com.dlsc.preferencesfx.history.History;
import com.dlsc.preferencesfx.model.Category;
import com.dlsc.preferencesfx.model.PreferencesFxModel;
import com.dlsc.preferencesfx.util.SearchHandler;
import com.dlsc.preferencesfx.util.StorageHandler;
import com.dlsc.preferencesfx.util.StorageHandlerImpl;
import com.dlsc.preferencesfx.view.BreadCrumbPresenter;
import com.dlsc.preferencesfx.view.BreadCrumbView;
import com.dlsc.preferencesfx.view.CategoryController;
import com.dlsc.preferencesfx.view.CategoryPresenter;
import com.dlsc.preferencesfx.view.CategoryView;
import com.dlsc.preferencesfx.view.NavigationPresenter;
import com.dlsc.preferencesfx.view.NavigationView;
import com.dlsc.preferencesfx.view.PreferencesFxPresenter;
import com.dlsc.preferencesfx.view.PreferencesFxView;
import com.dlsc.preferencesfx.view.UndoRedoBox;

/**
 * Represents the main PreferencesFX class.
 *
 * @author François Martin
 * @author Marco Sanfratello
 */
public class EpubFxPreferences
{
    private static final Logger LOGGER =
            LogManager.getLogger(EpubFxPreferences.class.getName());

    private PreferencesFxModel preferencesFxModel;

    private NavigationView navigationView;

    private UndoRedoBox undoRedoBox;

    private BreadCrumbView breadCrumbView;
    private BreadCrumbPresenter breadCrumbPresenter;

    private CategoryController categoryController;

    private PreferencesFxView preferencesFxView;
    private EpubFxPreferencesDialog preferencesFxDialog;

    private EpubFxPreferences(Class<?> saveClass, Category... categories) {
        this(new StorageHandlerImpl(saveClass), categories);
    }

    private EpubFxPreferences(StorageHandler storageHandler, Category... categories) {
        preferencesFxModel = new PreferencesFxModel(storageHandler, new SearchHandler(), new History(), categories);
        init();
    }

    private void init() {
        // setting values are only loaded if they are present already
        preferencesFxModel.loadSettingValues();

        undoRedoBox = new UndoRedoBox(preferencesFxModel.getHistory());

        breadCrumbView = new BreadCrumbView(preferencesFxModel, undoRedoBox);
        breadCrumbPresenter = new BreadCrumbPresenter(preferencesFxModel, breadCrumbView);

        categoryController = new CategoryController();
        initializeCategoryViews();

        // display initial category
        categoryController.setView(preferencesFxModel.getDisplayedCategory());

        navigationView = new NavigationView(preferencesFxModel);
        new NavigationPresenter(preferencesFxModel, navigationView);

        preferencesFxView = new PreferencesFxView(
                preferencesFxModel, navigationView, breadCrumbView, categoryController
        );
        new PreferencesFxPresenter(preferencesFxModel, preferencesFxView);
    }

    /**
     * Creates the Preferences window.
     *
     * @param saveClass  the class which the preferences are saved as
     *                   Must be unique to the application using the preferences
     * @param categories the items to be displayed in the TreeSearchView
     * @return the preferences window
     */
    public static EpubFxPreferences of(Class<?> saveClass, Category... categories) {
        return new EpubFxPreferences(saveClass, categories);
    }

    /**
     * Creates the Preferences window.
     *
     * @param customStorageHandler Custom implementation of the {@link StorageHandler}
     * @param categories           the items to be displayed in the TreeSearchView
     * @return the preferences window
     */
    public static EpubFxPreferences of(StorageHandler customStorageHandler, Category... categories) {
        return new EpubFxPreferences(customStorageHandler, categories);
    }

    /**
     * Prepares the CategoryController by creating CategoryView / CategoryPresenter pairs from
     * all Categories and loading them into the CategoryController.
     */
    private void initializeCategoryViews() {
        preferencesFxModel.getFlatCategoriesLst().forEach(category -> {
            CategoryView categoryView = new CategoryView(preferencesFxModel, category);
            CategoryPresenter categoryPresenter = new CategoryPresenter(
                    preferencesFxModel, category, categoryView, breadCrumbPresenter
            );
            categoryController.addView(category, categoryView, categoryPresenter);
        });
    }

    /**
     * Shows the PreferencesFX dialog.
     */
    public void show() {
        // by default, modal is false for retro-compatibility
        show(false);
    }

    /**
     * Show the PreferencesFX dialog.
     *
     * @param modal window or not modal, that's the question.
     */
    public void show(boolean modal) {
        if (preferencesFxDialog == null) {
            preferencesFxDialog = new EpubFxPreferencesDialog(preferencesFxModel, preferencesFxView);
        }
        preferencesFxDialog.show(modal);
    }

    /**
     * Defines if the PreferencesAPI should save the applications states.
     * This includes the persistence of the dialog window, as well as each settings values.
     *
     * @param enable if true, the storing of the window state of the dialog window
     *               and the settings values are enabled.
     * @return this object for fluent API
     */
    public EpubFxPreferences persistApplicationState(boolean enable) {
        persistWindowState(enable);
        saveSettings(enable);
        return this;
    }

    /**
     * Defines whether the state of the dialog window should be persisted or not.
     *
     * @param persist if true, the size, position and last selected item in the TreeSearchView are
     *                being saved. When the dialog is showed again, it will be restored to
     *                the last saved state. Defaults to false.
     * @return this object for fluent API
     */
    // asciidoctor Documentation - tag::fluentApiMethod[]
    public EpubFxPreferences persistWindowState(boolean persist) {
        preferencesFxModel.setPersistWindowState(persist);
        return this;
    }
    // asciidoctor Documentation - end::fluentApiMethod[]

    /**
     * Defines whether the adjusted settings of the application should be saved or not.
     *
     * @param save if true, the values of all settings of the application are saved.
     *             When the application is started again, the settings values will be restored to
     *             the last saved state. Defaults to false.
     * @return this object for fluent API
     */
    public EpubFxPreferences saveSettings(boolean save) {
        preferencesFxModel.setSaveSettings(save);
        // if settings shouldn't be saved, clear them if there are any present
        if (!save) {
            preferencesFxModel.getStorageHandler().clearPreferences();
        }
        return this;
    }

    /**
     * Defines whether the table to debug the undo / redo history should be shown in a dialog
     * when pressing a key combination or not.
     * <\br>
     * Pressing Ctrl + Shift + H (Windows) or CMD + Shift + H (Mac) opens a dialog with the
     * undo / redo history, shown in a table.
     *
     * @param debugState if true, pressing the key combination will open the dialog
     * @return this object for fluent API
     */
    public EpubFxPreferences debugHistoryMode(boolean debugState) {
        preferencesFxModel.setHistoryDebugState(debugState);
        return this;
    }

    public EpubFxPreferences buttonsVisibility(boolean isVisible) {
        preferencesFxModel.setButtonsVisible(isVisible);
        return this;
    }

    /**
     * Sets the translation service property of the preferences dialog.
     *
     * @param newValue The new value for the translation service property.
     * @return PreferencesFx to allow for chaining.
     */
    public EpubFxPreferences i18n(TranslationService newValue) {
        preferencesFxModel.setTranslationService(newValue);
        return this;
    }

    /**
     * Registers an event handler with the model. The handler is called when the
     * model receives an {@code Event} of the specified type during the bubbling
     * phase of event delivery.
     *
     * @param eventType    the type of the events to receive by the handler
     * @param eventHandler the handler to register
     * @throws NullPointerException if either event type or handler are {@code null}.
     */
    public EpubFxPreferences addEventHandler(EventType<PreferencesFxEvent> eventType, EventHandler<? super PreferencesFxEvent> eventHandler) {
        preferencesFxModel.addEventHandler(eventType, eventHandler);
        return this;
    }

    /**
     * Unregisters a previously registered event handler from the model. One
     * handler might have been registered for different event types, so the
     * caller needs to specify the particular event type from which to
     * unregister the handler.
     *
     * @param eventType    the event type from which to unregister
     * @param eventHandler the handler to unregister
     * @throws NullPointerException if either event type or handler are {@code null}.
     */
    public EpubFxPreferences removeEventHandler(EventType<PreferencesFxEvent> eventType, EventHandler<? super PreferencesFxEvent> eventHandler) {
        preferencesFxModel.removeEventHandler(eventType, eventHandler);
        return this;
    }

    /**
     * Returns a PreferencesFxView, so that it can be used as a Node.
     *
     * @return a PreferencesFxView, so that it can be used as a Node.
     */
    public PreferencesFxView getView() {
        return preferencesFxView;
    }

    /**
     * Call this method to manually save the changed settings
     * when showing the preferences by using {@link #getView()}.
     */
    public void saveSettings() {
        preferencesFxModel.saveSettings();
    }

    /**
     * Call this method to undo all changes made in the settings
     * when showing the preferences by using {@link #getView()}.
     */
    public void discardChanges() {
        preferencesFxModel.discardChanges();
    }

}

