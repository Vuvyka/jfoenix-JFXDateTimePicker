/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.jfoenix.skins;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.YearMonth;

import com.jfoenix.controls.JFXDateTimePicker;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialog.DialogTransition;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.behavior.JFXDateTimePickerBehavior;
import com.jfoenix.svg.SVGGlyph;
import com.sun.javafx.binding.ExpressionHelper;
import com.sun.javafx.scene.control.skin.ComboBoxPopupControl;

import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.StringConverter;

/**
 * <h1>Material Design DateTime Picker Skin</h1>
 * <p>JFXDateTimePickerSkin is the just copy of {@link JFXDatePickerSkin}
 * with changed data types to {@link LocalDateTime} and variable's names,
 * and modified SVG glyph.</p>
 * 
 * @author Roman Gorovoy
 * @version 1.0
 * @since 2019-04-01
 */
public class JFXDateTimePickerSkin extends ComboBoxPopupControl<LocalDateTime> {

    /**
     * TODO:
     * 1. Handle different Chronology
     */
    private JFXDateTimePicker dateTimePicker;

    // displayNode is the same as editorNode
    private TextField displayNode;
    private JFXDateTimePickerContent content;

    private JFXDialog dialog;

    public JFXDateTimePickerSkin(JFXDateTimePicker dateTimePicker) {
        super(dateTimePicker, new JFXDateTimePickerBehavior(dateTimePicker));
        this.dateTimePicker = dateTimePicker;
        try {
            Field helper = dateTimePicker.focusedProperty().getClass().getSuperclass()
                .getDeclaredField("helper");
            helper.setAccessible(true);
            ExpressionHelper value = (ExpressionHelper) helper.get(dateTimePicker.focusedProperty());
            Field changeListenersField = value.getClass().getDeclaredField("changeListeners");
            changeListenersField.setAccessible(true);
            ChangeListener[] changeListeners = (ChangeListener[]) changeListenersField.get(value);
            // remove parent focus listener to prevent editor class cast exception
            for (int i = changeListeners.length - 1; i > 0; i--) {
                if (changeListeners[i] != null && changeListeners[i].getClass().getName().contains("ComboBoxPopupControl")) {
                    dateTimePicker.focusedProperty().removeListener(changeListeners[i]);
                    break;
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        // add focus listener on editor node
        dateTimePicker.focusedProperty().addListener((obj, oldVal, newVal) -> {
            if (getEditor() != null && !newVal) {
                setTextFromTextFieldIntoComboBoxValue();
            }
        });

        // create calendar-o-clock button
        arrow = new SVGGlyph(0,
              "calendar-o-clock",
              "m832 575v256q0 8-5 13-5 5-13 5H632q-8 0-13-5-5-5-5-13v-36q0-8 5-13 5-5"
            + " 13-5H760V576q0-8 5-13 5-5 13-5h36q8 0 13 5 5 5 5 13z"
            + "m237 202Q1069 693 1028 621 987 549 915 508 843 467 759 467q-84 0-156 41"
            + "-72 41-113 113-40 72-40 156 0 84 40 156Q 531 1005 603 1046q 72 41 156 41"
            + " 84 0 156-41 72-41 113-112 41-72 41-156z"
            + "m128 0q 0 119-58 220Q 1081 1098 980 1156q-101 58-220 58-119 0-220-58-100"
            + "-58-158-158-58-101-58-220 0-119 58-220 58-101 158-159 101-58 220-58 119"
            + " 0 220 58 101 58 159 159 58 101 58 220z"
            + "M0 0v 1024h 325 c-11-20-21-42-30-64H 64V 255H 896v 38 c 21 5 43 13 63"
            + " 22V 0H 831V 64H 703V 0H 255V 64H 127V 0Z"
            + "m320 383v 127h 14 c 30-49 68-92 113-127z"
            + "M127 575V 703H 255V 575Z"
            + "m0 192V 896H 255V 768Z"
            , null);
        ((SVGGlyph) arrow).setFill(dateTimePicker.getDefaultColor());
        ((SVGGlyph) arrow).setSize(20, 20);
        arrowButton.getChildren().setAll(arrow);

        ((JFXTextField) getEditor()).setFocusColor(dateTimePicker.getDefaultColor());

        registerChangeListener(dateTimePicker.converterProperty(), "CONVERTER");
        registerChangeListener(dateTimePicker.dayCellFactoryProperty(), "DAY_CELL_FACTORY");
        registerChangeListener(dateTimePicker.showWeekNumbersProperty(), "SHOW_WEEK_NUMBERS");
        registerChangeListener(dateTimePicker.valueProperty(), "VALUE");
        registerChangeListener(dateTimePicker.defaultColorProperty(), "DEFAULT_COLOR");
    }

    @Override
    protected Node getPopupContent() {
        if (content == null) {
            // different chronologies are not supported yet
            content = new JFXDateTimePickerContent(dateTimePicker);
        }
        return content;
    }

    @Override
    public void show() {
        if (!dateTimePicker.isOverLay()) {
            super.show();
        }
        if (content != null) {
            content.init();
            content.clearFocus();
        }
        if (dialog == null && dateTimePicker.isOverLay()) {
            StackPane dialogParent = dateTimePicker.getDialogParent();
            if (dialogParent == null) {
                dialogParent = (StackPane) dateTimePicker.getScene().getRoot();
            }
            dialog = new JFXDialog(dialogParent, (Region) getPopupContent(), DialogTransition.CENTER, true);
            arrowButton.setOnMouseClicked((click) -> {
                if (dateTimePicker.isOverLay()) {
                    StackPane parent = dateTimePicker.getDialogParent();
                    if (parent == null) {
                        parent = (StackPane) dateTimePicker.getScene().getRoot();
                    }
                    dialog.show(parent);
                }
            });
        }
    }

    @Override
    protected void handleControlPropertyChanged(String p) {
        if ("DEFAULT_COLOR".equals(p)) {
            ((JFXTextField) getEditor()).setFocusColor(dateTimePicker.getDefaultColor());
        } else if ("DAY_CELL_FACTORY".equals(p)) {
            updateDisplayNode();
            content = null;
            popup = null;
        } else if ("CONVERTER".equals(p)) {
            updateDisplayNode();
        } else if ("EDITOR".equals(p)) {
            getEditableInputNode();
        } else if ("SHOWING".equals(p)) {
            if (dateTimePicker.isShowing()) {
                if (content != null) {
                    LocalDateTime dateTime = dateTimePicker.getValue();
                    // set the current date / now when showing the date picker content
                    content.displayedYearMonthProperty().set((dateTime != null) ?
                        YearMonth.from(dateTime) : YearMonth.now());
                    content.updateValues();
                }
                show();
            } else {
                hide();
            }
        } else if ("SHOW_WEEK_NUMBERS".equals(p)) {
            if (content != null) {
                // update the content grid to show week numbers
                content.updateContentGrid();
                content.updateWeekNumberDateCells();
            }
        } else if ("VALUE".equals(p)) {
            updateDisplayNode();
            if (content != null) {
                LocalDateTime dateTime = dateTimePicker.getValue();
                content.displayedYearMonthProperty().set((dateTime != null) ?
                    YearMonth.from(dateTime) : YearMonth.now());
                content.updateValues();
            }
            dateTimePicker.fireEvent(new ActionEvent());
        } else {
            super.handleControlPropertyChanged(p);
        }
    }

    @Override
    protected TextField getEditor() {
        return ((JFXDateTimePicker) getSkinnable()).getEditor();
    }

    @Override
    protected StringConverter<LocalDateTime> getConverter() {
        return ((JFXDateTimePicker) getSkinnable()).getConverter();
    }

    @Override
    public Node getDisplayNode() {
        if (displayNode == null) {
            displayNode = getEditableInputNode();
            displayNode.getStyleClass().add("time-picker-display-node");
            updateDisplayNode();
        }
        displayNode.setEditable(dateTimePicker.isEditable());
        return displayNode;
    }

    /*
     * this method is called from the behavior class to make sure
     * DatePicker button is in sync after the popup is being dismissed
     */
    public void syncWithAutoUpdate() {
        if (!getPopup().isShowing() && dateTimePicker.isShowing()) {
            dateTimePicker.hide();
        }
    }
}
