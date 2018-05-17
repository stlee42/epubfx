package de.machmireinebook.epubeditor.preferences;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;

import com.dlsc.formsfx.model.structure.SingleSelectionField;
import com.dlsc.preferencesfx.formsfx.view.controls.SimpleControl;

/**
 * Created by Michail Jungierek, Acando GmbH on 17.05.2018
 */
public class RadioButtonControl<V> extends SimpleControl<SingleSelectionField<V>, HBox>
{
    /**
     * - The fieldLabel is the container that displays the label property of
     * the field.
     * - The radioButtons is the list of radio buttons to display.
     * - The toggleGroup defines the group for the radio buttons.
     * - The node is a VBox holding all radio buttons.
     */
    private final List<RadioButton> radioButtons = new ArrayList<>();
    private ToggleGroup toggleGroup;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initializeParts() {
        super.initializeParts();

        node = new HBox();
        node.getStyleClass().add("simple-radio-control");

        toggleGroup = new ToggleGroup();

        createRadioButtons();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void layoutParts() {
        node.setSpacing(5);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setupBindings() {
        super.setupBindings();
        setupRadioButtonBindings();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setupValueChangedListeners() {
        super.setupValueChangedListeners();

        field.itemsProperty().addListener((observable, oldValue, newValue) -> {
            createRadioButtons();
            setupRadioButtonBindings();
            setupRadioButtonEventHandlers();
        });

        field.selectionProperty().addListener((observable, oldValue, newValue) -> {
            if (field.getSelection() != null) {
                radioButtons.get(field.getItems().indexOf(field.getSelection())).setSelected(true);
            } else {
                toggleGroup.getSelectedToggle().setSelected(false);
            }
        });

        field.errorMessagesProperty().addListener((observable, oldValue, newValue) ->
                toggleTooltip(node, radioButtons.get(radioButtons.size() - 1))
        );
        field.tooltipProperty().addListener((observable, oldValue, newValue) ->
                toggleTooltip(node, radioButtons.get(radioButtons.size() - 1))
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setupEventHandlers() {
        node.setOnMouseEntered(event -> toggleTooltip(node, radioButtons.get(radioButtons.size() - 1)));
        node.setOnMouseExited(event -> toggleTooltip(node, radioButtons.get(radioButtons.size() - 1)));
        setupRadioButtonEventHandlers();
    }

    /**
     * This method creates radio buttons and adds them to radioButtons
     * and is used when the itemsProperty on the field changes.
     */
    private void createRadioButtons() {
        node.getChildren().clear();
        radioButtons.clear();

        for (int i = 0; i < field.getItems().size(); i++) {
            RadioButton rb = new RadioButton();

            rb.setText(field.getItems().get(i).toString());
            rb.setToggleGroup(toggleGroup);

            radioButtons.add(rb);
        }

        if (field.getSelection() != null) {
            radioButtons.get(field.getItems().indexOf(field.getSelection())).setSelected(true);
        }

        node.getChildren().addAll(radioButtons);
    }

    /**
     * Sets up bindings for all radio buttons.
     */
    private void setupRadioButtonBindings() {
        for (RadioButton radio : radioButtons) {
            radio.disableProperty().bind(field.editableProperty().not());
        }
    }

    /**
     * Sets up bindings for all radio buttons.
     */
    private void setupRadioButtonEventHandlers() {
        for (int i = 0; i < radioButtons.size(); i++) {
            final int j = i;
            radioButtons.get(j).setOnAction(event -> field.select(j));
        }
    }
}
