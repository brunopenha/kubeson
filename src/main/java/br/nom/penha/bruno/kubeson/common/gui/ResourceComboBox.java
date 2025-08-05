package br.nom.penha.bruno.kubeson.common.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.nom.penha.bruno.kubeson.Main;
import br.nom.penha.bruno.kubeson.common.model.ItemType;
import br.nom.penha.bruno.kubeson.common.model.SelectedItem;
import br.nom.penha.bruno.kubeson.common.model.SelectorItem;
import br.nom.penha.bruno.kubeson.common.model.TabType;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.Skin;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ResourceComboBox extends ComboBox<SelectorItem> {

    private static Logger LOGGER = LogManager.getLogger(ResourceComboBox.class);

    private static int groupNumber;

    static {
        groupNumber = 1;
    }

    private Image iconDelete;

    private Image iconMetrics;

    private String valorClicado;

    public ResourceComboBox() {
        super.getStyleClass().add("selector-pod-name");
        super.setFocusTraversable(false);
        super.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        super.setVisibleRowCount(30);

        super.setCellFactory(listView -> {
            listView.setMinWidth(500);
            listView.setPrefWidth(500);
            CheckBoxListCell result = new CheckBoxListCell();
            result.setOnMouseClicked(e -> {
                        LOGGER.info("Event: " + e.toString());
                        LOGGER.info("e.getTarget(): " + e.getTarget());
                        LOGGER.info("e.getEventType().getName(): " + e.getEventType().getName());
                        //super.hide();
                    }
            );

            return result;
        });

        super.setButtonCell(new ListCell<SelectorItem>() {

            @Override
            protected void updateItem(SelectorItem item, boolean empty) {
                //System.out.println("setButtonCell - updateItem");
            }
        });


        super.showingProperty().addListener((observable, oldValue, newValue) -> {


            if (!newValue) {
                ObservableList<SelectedItem> selected = FXCollections.observableArrayList();
                for (SelectorItem item : getItems()) {

                    if (item.isChecked()) {
                        //System.out.println("Clicked: " + item.isChecked());
                        selected.add(new SelectedItem(item));
                        item.setChecked(false);
                    }
                }

                if (selected.size() > 1) {

                    MainTabPane.createLogTab(selected, new TabLabel(TabType.LOG, "Group " + groupNumber));
                    groupNumber++;
                } else if (getSelectionModel().getSelectedItem() != null
                        && getSelectionModel().getSelectedItem().getType() != ItemType.TEXT) {
                    SelectedItem selectedItem = new SelectedItem(getSelectionModel().getSelectedItem());
                    if (selectedItem.getType() == ItemType.CONFIG_MAP) {
                        MainTabPane.createConfigMapTab(selectedItem.getConfigMap(), new TabLabel(TabType.CONFIG_MAP, selectedItem.getText()));
                    } else {
                        MainTabPane.createLogTab(Collections.singletonList(selectedItem), new TabLabel(TabType.LOG, selectedItem.getText()));
                    }
                } else if (selected.size() == 1) {
                    MainTabPane.createLogTab(selected, new TabLabel(TabType.LOG, selected.get(0).getText()));
                }
                getSelectionModel().clearSelection();
            }
        });

        this.iconDelete = Main.getImage("/icons/delete_13x13.png");
        this.iconMetrics = Main.getImage("/icons/pie_13x13.png");
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new ComboBoxListViewSkin<SelectorItem>(this) {

            // overridden to prevent the popup from disappearing
//            @Override
            protected boolean isHideOnClickEnabled() {
                return false;
            }
        };
    }

    private class CheckBoxListCell extends ListCell<SelectorItem> {

        private final CheckBox checkBox = new CheckBox();

        private Property<Boolean> selectedProperty;

        private Property<Boolean> disabledProperty;

        public CheckBoxListCell() {
            checkBox.setPrefHeight(Region.USE_COMPUTED_SIZE);
            checkBox.setStyle("-fx-padding: 2 2 2 1;");
        }

        @Override
        public void updateItem(SelectorItem selectorItem, boolean empty) {
            super.updateItem(selectorItem, empty);

            if (selectorItem == null || empty) {
                setGraphic(null);
                return;
            }

            Text text = new Text(selectorItem.getText());
            text.setStyle(selectorItem.getStyle());

            if (selectorItem.getType() == ItemType.POD || selectorItem.getType() == ItemType.CONTAINER || selectorItem.getType() == ItemType.LABEL) {
                HBox hBox = new HBox(checkBox, text);
                hBox.setAlignment(Pos.CENTER_LEFT);
                setGraphic(hBox);

                if (selectorItem.getType() == ItemType.POD
                        || selectorItem.getType() == ItemType.LABEL
                        || selectorItem.getType() == ItemType.CONTAINER) {
                    HBox buttons = new HBox();
                    buttons.setAlignment(Pos.CENTER_RIGHT);
                    buttons.setSpacing(10);
                    buttons.setPadding(new Insets(0, 7, 0, 0));
                    HBox.setHgrow(buttons, Priority.ALWAYS);
                    hBox.getChildren().add(buttons);

                    if (selectorItem.getPod().hasMetrics()) {
                        Button metricsButton = createButton(iconMetrics, "METRICS");
                        metricsButton.setOnAction((event -> {
                            SelectedItem selectedItem = new SelectedItem(selectorItem);
                            String tabText;
                            if (selectedItem.getPod().getAppLabel() != null) {
                                tabText = selectedItem.getPod().getAppLabel();
                            } else {
                                tabText = selectedItem.getPod().getPodName();
                            }
                            MainTabPane.createMetricTab(selectedItem, new TabLabel(TabType.METRICS, tabText));
                            ResourceComboBox.this.hide();
                        }));
                        buttons.getChildren().add(metricsButton);
                    }

                    Button deleteButton = createButton(iconDelete, "DELETE POD");
                    deleteButton.setOnAction(event -> selectorItem.getPod().delete());
                    buttons.getChildren().add(deleteButton);
                }

                if (selectedProperty != null) {
                    checkBox.selectedProperty().unbindBidirectional(selectedProperty);
                }
                selectedProperty = selectorItem.getCheckedProperty();
                if (selectedProperty == null) {
                    selectedProperty = new SimpleBooleanProperty();
                    selectorItem.setCheckedProperty(selectedProperty);
                    selectedProperty.addListener((observable, oldValue, newValue) -> checkForDuplicatedPod(selectorItem, newValue));
                }
                checkBox.selectedProperty().bindBidirectional(selectedProperty);

                if (disabledProperty != null) {
                    checkBox.disableProperty().unbindBidirectional(disabledProperty);
                }
                disabledProperty = selectorItem.getDisabledProperty();
                checkBox.disableProperty().bindBidirectional(disabledProperty);
            } else {
                setGraphic(text);
            }
        }

        private void checkForDuplicatedPod(SelectorItem selectorItem, Boolean newValue) {
            if (selectorItem.getContainer() == null) {
                for (SelectorItem item : getListView().getItems()) {
                    if (!item.equals(selectorItem) && item.getPod() != null && item.getContainer() == null && item.getPod()
                            .getPodName()
                            .equals(selectorItem.getPod().getPodName())) {
                        item.setDisabledProperty(newValue);
                    }
                }
            }
        }

        private Button createButton(Image icon, String tooltip) {
            Button button = new Button();
            button.setGraphic(new ImageView(icon));
            button.setPrefHeight(21);
            button.setMinHeight(21);
            button.setPrefWidth(21);
            button.setMinWidth(21);
            button.setFocusTraversable(false);
            button.setTooltip(new Tooltip(tooltip));

            return button;
        }
    }
}
