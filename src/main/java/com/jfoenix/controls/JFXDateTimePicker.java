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

package com.jfoenix.controls;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.chrono.Chronology;
import java.time.chrono.IsoChronology;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.jfoenix.controls.base.IFXValidatableControl;
import com.jfoenix.skins.JFXDateTimePickerSkin;
import com.jfoenix.validation.base.ValidatorBase;
import com.sun.javafx.css.converters.BooleanConverter;
import com.sun.javafx.css.converters.PaintConverter;
import com.sun.javafx.scene.control.skin.resources.ControlResources;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.WritableValue;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.SimpleStyleableBooleanProperty;
import javafx.css.SimpleStyleableObjectProperty;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Cell;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.Control;
import javafx.scene.control.DateCell;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.LocalDateTimeStringConverter;

/**
 * JFXDateTimePicker is the combo of {@link JFXDatePicker}, {@link DatePicker} and {@link JFXTimePicker}.
 *
 * @author Roman Gorovoy
 * @version 1.0
 * @since 2019-04-01
 */
public class JFXDateTimePicker extends ComboBoxBase<LocalDateTime> implements IFXValidatableControl {

    /***************************************************************************
     *                                                                         *
     * DatePicker                                                              *
     * >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> *
     **************************************************************************/

    private LocalDateTime lastValidDateTime = null;
    private Chronology lastValidChronology = IsoChronology.INSTANCE;

    /***************************************************************************
     * <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< *
     * DatePicker                                                              *
     *                                                                         *
     **************************************************************************/

    /**
     * {@inheritDoc}
     */
    public JFXDateTimePicker() {
        initialize();

        /***************************************************************************
         *                                                                         *
         * DatePicker                                                              *
         * >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> *
         **************************************************************************/

        valueProperty().addListener(observable -> {
            LocalDateTime dateTime = getValue();
            Chronology chrono = getChronology();

            if (validateDateTime(chrono, dateTime)) {
                lastValidDateTime = dateTime;
            } else {
                System.err.println("Restoring value to " +
                            ((lastValidDateTime == null) ? "null" : getConverter().toString(lastValidDateTime)));
                setValue(lastValidDateTime);
            }
        });

        chronologyProperty().addListener(observable -> {
            LocalDateTime dateTime = getValue();
            Chronology chrono = getChronology();

            if (validateDateTime(chrono, dateTime)) {
                lastValidChronology = chrono;
                defaultConverter = new LocalDateTimeStringConverter(FormatStyle.SHORT, FormatStyle.SHORT, Locale.getDefault(), chrono);
            } else {
                System.err.println("Restoring value to " + lastValidChronology);
                setChronology(lastValidChronology);
            }
        });

        /***************************************************************************
         * <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< *
         * DatePicker                                                              *
         *                                                                         *
         **************************************************************************/
    }

    /***************************************************************************
     *                                                                         *
     * DatePicker                                                              *
     * >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> *
     **************************************************************************/

    private boolean validateDateTime(Chronology chrono, LocalDateTime dateTime) {
        try {
            if (dateTime != null) {
                chrono.date(dateTime);
            }
            return true;
        } catch (DateTimeException ex) {
            System.err.println(ex);
            return false;
        }
    }

    /***************************************************************************
     * <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< *
     * DatePicker                                                              *
     *                                                                         *
     **************************************************************************/

    /**
     * {@inheritDoc}
     */
    public JFXDateTimePicker(LocalDateTime localDateTime) {
        setValue(localDateTime);
        initialize();
    }

    private void initialize() {
        getStyleClass().add(DEFAULT_STYLE_CLASS);
        setAccessibleRole(AccessibleRole.DATE_PICKER);
        setEditable(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUserAgentStylesheet() {
        return JFXDateTimePicker.class.getResource("/css/controls/jfx-date-time-picker.css").toExternalForm();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Skin<?> createDefaultSkin() {
        return new JFXDateTimePickerSkin(this);
    }

    /***************************************************************************
     *                                                                         *
     * Properties                                                              *
     *                                                                         *
     **************************************************************************/

    /***************************************************************************
     *                                                                         *
     * DatePicker                                                              *
     * >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> *
     **************************************************************************/

    /**
     * A custom cell factory can be provided to customize individual
     * day cells in the DatePicker popup. Refer to {@link DateCell}
     * and {@link Cell} for more information on cell factories.
     * Example:
     *
     * <pre><code>
     * final Callback&lt;DatePicker, DateCell&gt; dayCellFactory = new Callback&lt;DatePicker, DateCell&gt;() {
     *     public DateCell call(final DatePicker datePicker) {
     *         return new DateCell() {
     *             &#064;Override public void updateItem(LocalDate item, boolean empty) {
     *                 super.updateItem(item, empty);
     *
     *                 if (MonthDay.from(item).equals(MonthDay.of(9, 25))) {
     *                     setTooltip(new Tooltip("Happy Birthday!"));
     *                     setStyle("-fx-background-color: #ff4444;");
     *                 }
     *                 if (item.equals(LocalDate.now().plusDays(1))) {
     *                     // Tomorrow is too soon.
     *                     setDisable(true);
     *                 }
     *             }
     *         };
     *     }
     * };
     * datePicker.setDayCellFactory(dayCellFactory);
     * </code></pre>
     *
     * @defaultValue null
     */
    private ObjectProperty<Callback<JFXDateTimePicker, DateCell>> dayCellFactory;
    public final void setDayCellFactory(Callback<JFXDateTimePicker, DateCell> value) {
        dayCellFactoryProperty().set(value);
    }
    public final Callback<JFXDateTimePicker, DateCell> getDayCellFactory() {
        return (dayCellFactory != null) ? dayCellFactory.get() : null;
    }
    public final ObjectProperty<Callback<JFXDateTimePicker, DateCell>> dayCellFactoryProperty() {
        if (dayCellFactory == null) {
            dayCellFactory = new SimpleObjectProperty<Callback<JFXDateTimePicker, DateCell>>(this, "dayCellFactory");
        }
        return dayCellFactory;
    }

    /**
     * The calendar system used for parsing, displaying, and choosing
     * dates in the DatePicker control.
     *
     * <p>The default value is returned from a call to
     * {@code Chronology.ofLocale(Locale.getDefault(Locale.Category.FORMAT))}.
     * The default is usually {@link java.time.chrono.IsoChronology} unless
     * provided explicitly in the {@link java.util.Locale} by use of a
     * Locale calendar extension.
     *
     * Setting the value to <code>null</code> will restore the default
     * chronology.
     */
    public final ObjectProperty<Chronology> chronologyProperty() {
        return chronology;
    }
    private ObjectProperty<Chronology> chronology =
        new SimpleObjectProperty<Chronology>(this, "chronology", null);
    public final Chronology getChronology() {
        Chronology chrono = chronology.get();
        if (chrono == null) {
            try {
                chrono = Chronology.ofLocale(Locale.getDefault(Locale.Category.FORMAT));
            } catch (Exception ex) {
                System.err.println(ex);
            }
            if (chrono == null) {
                chrono = IsoChronology.INSTANCE;
            }
            //System.err.println(chrono);
        }
        return chrono;
    }
    public final void setChronology(Chronology value) {
        chronology.setValue(value);
    }

    /**
     * Whether the DatePicker popup should display a column showing
     * week numbers.
     *
     * <p>The default value is specified in a resource bundle, and
     * depends on the country of the current locale.
     */
    public final BooleanProperty showWeekNumbersProperty() {
        if (showWeekNumbers == null) {
            String country = Locale.getDefault(Locale.Category.FORMAT).getCountry();
            boolean localizedDefault =
                (!country.isEmpty() &&
                 ControlResources.getNonTranslatableString("DatePicker.showWeekNumbers").contains(country));
            showWeekNumbers = new StyleableBooleanProperty(localizedDefault) {
                @Override public CssMetaData<JFXDateTimePicker,Boolean> getCssMetaData() {
                    return StyleableProperties.SHOW_WEEK_NUMBERS;
                }

                @Override public Object getBean() {
                    return JFXDateTimePicker.this;
                }

                @Override public String getName() {
                    return "showWeekNumbers";
                }
            };
        }
        return showWeekNumbers;
    }
    private BooleanProperty showWeekNumbers;
    public final void setShowWeekNumbers(boolean value) {
        showWeekNumbersProperty().setValue(value);
    }
    public final boolean isShowWeekNumbers() {
        return showWeekNumbersProperty().getValue();
    }

    /***************************************************************************
     * <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< *
     * DatePicker                                                              *
     *                                                                         *
     **************************************************************************/

    /**
     * the parent node used when showing the data picker content as an overlay,
     * intead of a popup
     */
    private ObjectProperty<StackPane> dialogParent = new SimpleObjectProperty<>(null);

    public final ObjectProperty<StackPane> dialogParentProperty() {
        return this.dialogParent;
    }

    public final StackPane getDialogParent() {
        return this.dialogParentProperty().get();
    }

    public final void setDialogParent(final StackPane dialogParent) {
        this.dialogParentProperty().set(dialogParent);
    }

    /**
     * Converts the input text to an object of type LocalDateTime and vice
     * versa.
     */
    public final ObjectProperty<StringConverter<LocalDateTime>> converterProperty() {
        return converter;
    }

    private ObjectProperty<StringConverter<LocalDateTime>> converter =
        new SimpleObjectProperty<>(this, "converter", null);

    public final void setConverter(StringConverter<LocalDateTime> value) {
        converterProperty().set(value);
    }

    public final StringConverter<LocalDateTime> getConverter() {
        StringConverter<LocalDateTime> converter = converterProperty().get();
        if (converter != null) {
            return converter;
        } else {
            return defaultConverter;
        }
    }

    private StringConverter<LocalDateTime> defaultConverter = new LocalDateTimeStringConverter(FormatStyle.SHORT, FormatStyle.SHORT,
        Locale.getDefault(), this.getChronology());

    private BooleanProperty _24HourView = new SimpleBooleanProperty(false);

    public final BooleanProperty _24HourViewProperty() {
        return this._24HourView;
    }

    public final boolean is24HourView() {
        return _24HourViewProperty().get();
    }

    public final void set24HourView(final boolean value) {
        _24HourViewProperty().setValue(value);
    }

    /**
     * The editor for the DateTimePicker.
     *
     * @see javafx.scene.control.ComboBox#editorProperty
     */
    private ReadOnlyObjectWrapper<TextField> editor;

    public final TextField getEditor() {
        return editorProperty().get();
    }

    public final ReadOnlyObjectProperty<TextField> editorProperty() {
        if (editor == null) {
            editor = new ReadOnlyObjectWrapper<>(this, "editor");
            final FakeFocusJFXTextField editorNode = new FakeFocusJFXTextField();
            this.focusedProperty().addListener((obj, oldVal, newVal) -> {
                if (getEditor() != null) {
                    editorNode.setFakeFocus(newVal);
                }
            });
            editorNode.activeValidatorWritableProperty().bind(activeValidatorProperty());
            editor.set(editorNode);
        }
        return editor.getReadOnlyProperty();
    }

    private ValidationControl validationControl = new ValidationControl(this);

    @Override
    public ValidatorBase getActiveValidator() {
        return validationControl.getActiveValidator();
    }

    @Override
    public ReadOnlyObjectProperty<ValidatorBase> activeValidatorProperty() {
        return validationControl.activeValidatorProperty();
    }

    @Override
    public ObservableList<ValidatorBase> getValidators() {
        return validationControl.getValidators();
    }

    @Override
    public void setValidators(ValidatorBase... validators) {
        validationControl.setValidators(validators);
    }

    @Override
    public boolean validate() {
        return validationControl.validate();
    }

    @Override
    public void resetValidation() {
        validationControl.resetValidation();
    }

    /***************************************************************************
     *                                                                         *
     * Stylesheet Handling                                                     *
     *                                                                         *
     **************************************************************************/

    /**
     * Initialize the style class to 'jfx-date-picker'.
     * <p>
     * This is the selector class from which CSS can be used to style
     * this control.
     */
    private static final String DEFAULT_STYLE_CLASS = "jfx-date-time-picker";

    /**
     * show the popup as an overlay using JFXDialog
     * NOTE: to show it properly the scene root must be StackPane, or the user must set
     * the dialog parent manually using the property {{@link #dialogParentProperty()}
     */
    private StyleableBooleanProperty overLay = new SimpleStyleableBooleanProperty(StyleableProperties.OVERLAY,
        JFXDateTimePicker.this,
        "overLay",
        false);

    public final StyleableBooleanProperty overLayProperty() {
        return this.overLay;
    }

    public final boolean isOverLay() {
        return overLay != null && this.overLayProperty().get();
    }

    public final void setOverLay(final boolean overLay) {
        this.overLayProperty().set(overLay);
    }

    /**
     * the default color used in the data picker content
     */
    private StyleableObjectProperty<Paint> defaultColor = new SimpleStyleableObjectProperty<>(StyleableProperties.DEFAULT_COLOR,
        JFXDateTimePicker.this,
        "defaultColor",
        Color.valueOf(
            "#009688"));

    public Paint getDefaultColor() {
        return defaultColor == null ? Color.valueOf("#009688") : defaultColor.get();
    }

    public StyleableObjectProperty<Paint> defaultColorProperty() {
        return this.defaultColor;
    }

    public void setDefaultColor(Paint color) {
        this.defaultColor.set(color);
    }

    private static class StyleableProperties {
        private static final CssMetaData<JFXDateTimePicker, Paint> DEFAULT_COLOR =
            new CssMetaData<JFXDateTimePicker, Paint>("-jfx-default-color",
                PaintConverter.getInstance(), Color.valueOf("#009688")) {
                @Override
                public boolean isSettable(JFXDateTimePicker control) {
                    return control.defaultColor == null || !control.defaultColor.isBound();
                }

                @Override
                public StyleableProperty<Paint> getStyleableProperty(JFXDateTimePicker control) {
                    return control.defaultColorProperty();
                }
            };

        private static final CssMetaData<JFXDateTimePicker, Boolean> OVERLAY =
            new CssMetaData<JFXDateTimePicker, Boolean>("-jfx-overlay",
                BooleanConverter.getInstance(), false) {
                @Override
                public boolean isSettable(JFXDateTimePicker control) {
                    return control.overLay == null || !control.overLay.isBound();
                }

                @Override
                public StyleableBooleanProperty getStyleableProperty(JFXDateTimePicker control) {
                    return control.overLayProperty();
                }
            };

        /***************************************************************************
         *                                                                         *
         * DatePicker                                                              *
         * >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> *
         **************************************************************************/

        private static final String country =
            Locale.getDefault(Locale.Category.FORMAT).getCountry();
        private static final CssMetaData<JFXDateTimePicker, Boolean> SHOW_WEEK_NUMBERS =
              new CssMetaData<JFXDateTimePicker, Boolean>("-fx-show-week-numbers",
                   BooleanConverter.getInstance(),
                   (!country.isEmpty() &&
                    ControlResources.getNonTranslatableString("DatePicker.showWeekNumbers").contains(country))) {
            @Override public boolean isSettable(JFXDateTimePicker n) {
                return n.showWeekNumbers == null || !n.showWeekNumbers.isBound();
            }

            @Override public StyleableProperty<Boolean> getStyleableProperty(JFXDateTimePicker n) {
                return (StyleableProperty<Boolean>)(WritableValue<Boolean>)n.showWeekNumbersProperty();
            }
        };

        /***************************************************************************
         * <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< *
         * DatePicker                                                              *
         *                                                                         *
         **************************************************************************/

        private static final List<CssMetaData<? extends Styleable, ?>> CHILD_STYLEABLES;

        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables =
                new ArrayList<CssMetaData<? extends Styleable, ?>>(Control.getClassCssMetaData());
            Collections.addAll(styleables,
                DEFAULT_COLOR,
                SHOW_WEEK_NUMBERS, // DatePicker's
                OVERLAY);
            CHILD_STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    // inherit the styleable properties from parent
    private List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        if (STYLEABLES == null) {
            final List<CssMetaData<? extends Styleable, ?>> styleables =
                new ArrayList<>(Control.getClassCssMetaData());
            styleables.addAll(getClassCssMetaData());
            styleables.addAll(Control.getClassCssMetaData());
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
        return STYLEABLES;
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.CHILD_STYLEABLES;
    }
}
