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

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MONTHS;
import static java.time.temporal.ChronoUnit.WEEKS;
import static java.time.temporal.ChronoUnit.YEARS;
import java.awt.geom.Point2D;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.Chronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DecimalStyle;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDateTimePicker;
import com.jfoenix.controls.JFXListCell;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.svg.SVGGlyph;
import com.jfoenix.transitions.CachedTransition;
import javafx.animation.Animation.Status;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.DateCell;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import javafx.util.converter.LocalTimeStringConverter;
import javafx.util.converter.NumberStringConverter;

/**
 * JFXDateTimePickerContent is the just copy of JFXDatePickerContent with partially
 * injected functional from JFXTimePickerContent and combo them
 * 
 * @author Roman Gorovoy
 */
public class JFXDateTimePickerContent extends VBox {

    private static final String SPINNER_LABEL = "spinner-label";
    private static final String ROBOTO = "Roboto";
    private static final Color DEFAULT_CELL_COLOR = Color.valueOf("#9C9C9C");
    private static final Color DEFAULT_COLOR = Color.valueOf("#313131");

    private static final PseudoClass selectedYear = PseudoClass.getPseudoClass("selected-year");

    /***************************************************************************
     *                                                                         *
     * Date and Time picker's content merge                                    *
     * >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> *
     **************************************************************************/

    private HBox headerPane = new HBox();
    private StackPane contentPlaceHolder = new StackPane();

    /**
     * the value represents a 'mode', which sub control currently visible:
     * false - calendar, true - clock
     */
    private boolean currentVisibleClock = false;

    /***************************************************************************
     * <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< *
     * Date and Time picker's content merge                                    *
     *                                                                         *
     **************************************************************************/

    protected JFXDateTimePicker dateTimePicker;
    private JFXButton backMonthButton;
    private JFXButton forwardMonthButton;
    private ObjectProperty<JFXListCell> selectedYearCell = new SimpleObjectProperty<>(null);
    private Label selectedDateLabel;
    private Label selectedYearLabel;
    private Label monthYearLabel;
    protected GridPane contentGrid;
    private StackPane calendarStackPane = new StackPane();

    // animation
    private CachedTransition showTransition;
    private CachedTransition hideTransition;
    private ParallelTransition tempImageTransition;

    private int daysPerWeek = 7;
    private List<DateCell> weekDaysCells = new ArrayList<>();
    private List<DateCell> weekNumberCells = new ArrayList<>();
    protected List<DateCell> dayCells = new ArrayList<>();
    private LocalDate[] dayCellDates;
    private DateCell currentFocusedDayCell = null;

    /***************************************************************************
     *                                                                         *
     * Time Picker Content                                                     *
     * >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> *
     **************************************************************************/

    private enum TimeUnit {HOURS, MINUTES}

    private Color fadedColor = Color.rgb(255, 255, 255, 0.67);
    private boolean is24HourView = false;
    private double contentCircleRadius = 100;
    private Label selectedHourLabel = new Label();
    private Label selectedMinLabel = new Label();
    private Label periodPMLabel, periodAMLabel;
    private StackPane clockPlaceHolder = new StackPane();
    private StackPane hoursContent;
    private StackPane minutesContent;
    private Rotate hoursPointerRotate, _24HourHoursPointerRotate;
    private Rotate minsPointerRotate;
    private ObjectProperty<TimeUnit> unit = new SimpleObjectProperty<>(TimeUnit.HOURS);
    private DoubleProperty angle = new SimpleDoubleProperty(Math.toDegrees(2 * Math.PI / 12));
    private StringProperty period = new SimpleStringProperty("AM");
    private ObjectProperty<Rotate> pointerRotate = new SimpleObjectProperty<>(),
        _24HourPointerRotate = new SimpleObjectProperty<>();
    private ObjectProperty<Label> timeLabel = new SimpleObjectProperty<>();
    private NumberStringConverter unitConverter = new NumberStringConverter("#00");
    private ObjectProperty<LocalTime> selectedTime = new SimpleObjectProperty<>(this, "selectedTime");

    /***************************************************************************
     * <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< *
     * Time Picker Content                                                     *
     *                                                                         *
     **************************************************************************/

    StackPane calendarPlaceHolder = new StackPane();

    private ListView<String> yearsListView = new JFXListView<String>() {
        {
            this.getStyleClass().setAll("date-picker-list-view");
            this.setCellFactory(listView -> new JFXListCell<String>() {
                boolean mousePressed = false;

                {
                    this.getStyleClass().setAll("date-picker-list-cell");
                    setOnMousePressed(click -> mousePressed = true);
                    setOnMouseReleased(release -> mousePressed = false);
                    setOnMouseClicked(click -> {
                        String selectedItem = yearsListView.getSelectionModel().getSelectedItem();
                        if (selectedItem != null && selectedItem.equals(getText())) {
                            int offset = Integer.parseInt(getText()) - Integer.parseInt(
                                selectedYearLabel.getText());
                            forward(offset, YEARS, false, false);
                            hideTransition.setOnFinished(finish -> {
                                selectedYearCell.set(this);
                                pseudoClassStateChanged(selectedYear, true);
                                setTextFill(dateTimePicker.getDefaultColor());
                                yearsListView.scrollTo(this.getIndex() - 2 >= 0 ? this.getIndex() - 2 : this.getIndex());
                                hideTransition.setOnFinished(null);
                            });
                            hideTransition.play();
                        }
                    });
                    selectedYearLabel.textProperty().addListener((o, oldVal, newVal) -> {
                        if (!yearsListView.isVisible() && newVal.equals(getText())) {
                            selectedYearCell.set(this);
                        }
                    });
                }

                @Override
                public void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (!empty) {
                        final boolean isSelectedYear = item.equals(selectedYearLabel.getText());
                        if (isSelectedYear) {
                            selectedYearCell.set(this);
                        }
                        pseudoClassStateChanged(selectedYear, isSelectedYear);
                        setTextFill(isSelectedYear ? dateTimePicker.getDefaultColor() : DEFAULT_COLOR);
                    } else {
                        pseudoClassStateChanged(selectedYear, false);
                        setTextFill(DEFAULT_COLOR);
                    }
                }
            });
        }
    };

    // Date formatters
    final DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM");
    final DateTimeFormatter yearFormatter = DateTimeFormatter.ofPattern("y");
    final DateTimeFormatter weekNumberFormatter = DateTimeFormatter.ofPattern("w");
    final DateTimeFormatter weekDayNameFormatter = DateTimeFormatter.ofPattern("ccc");
    final DateTimeFormatter dayCellFormatter = DateTimeFormatter.ofPattern("d");

    private ObjectProperty<YearMonth> selectedYearMonth = new SimpleObjectProperty<>(this, "selectedYearMonth");

    JFXDateTimePickerContent(final JFXDateTimePicker dateTimePicker) {
        this.dateTimePicker = dateTimePicker;
        getStyleClass().add("date-picker-popup");

        LocalDate date = dateTimePicker.getValue() == null ?
            LocalDate.now() : dateTimePicker.getValue().toLocalDate();
        selectedYearMonth.set(YearMonth.from(date));
        selectedYearMonth.addListener((observable, oldValue, newValue) -> updateValues());

        // add change listener to change the color of the selected year cell
        selectedYearCell.addListener((o, oldVal, newVal) -> {
            if (oldVal != null) {
                oldVal.pseudoClassStateChanged(selectedYear, false);
                oldVal.setTextFill(DEFAULT_COLOR);
            }
            if (newVal != null) {
                newVal.pseudoClassStateChanged(selectedYear, true);
                newVal.setTextFill(dateTimePicker.getDefaultColor());
            }
        });

        /***************************************************************************
         *                                                                         *
         * Time Picker Content                                                     *
         * >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> *
         **************************************************************************/

        LocalTime time = this.dateTimePicker.getValue() == null ?
            LocalTime.now() : this.dateTimePicker.getValue().toLocalTime();
        is24HourView = this.dateTimePicker.is24HourView();

        this.dateTimePicker.valueProperty().addListener((o, oldVal, newVal) -> goToTime(newVal));

        /***************************************************************************
         * <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< *
         * Time Picker Content                                                     *
         *                                                                         *
         **************************************************************************/

        /***************************************************************************
         *                                                                         *
         * Date and Time picker's content merge                                    *
         * >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> *
         **************************************************************************/

        // create the header pane
        this.headerPane.getStyleClass().add("date-time-picker-header");
        this.headerPane.setAlignment(Pos.CENTER);
        headerPane.setBackground(new Background(new BackgroundFill(this.dateTimePicker.getDefaultColor(),
            CornerRadii.EMPTY,
            Insets.EMPTY)));
        this.headerPane.getChildren().add(createHeaderPane());
        this.headerPane.getChildren().add(createHeaderPane(time, is24HourView));
        getChildren().add(this.headerPane);

        /***************************************************************************
         * <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< *
         * Date and Time picker's content merge                                    *
         *                                                                         *
         **************************************************************************/

        contentGrid = new GridPane() {
            @Override
            protected double computePrefWidth(double height) {
                final int nCols = daysPerWeek + (dateTimePicker.isShowWeekNumbers() ? 1 : 0);
                final double leftSpace = snapSpace(getInsets().getLeft());
                final double rightSpace = snapSpace(getInsets().getRight());
                final double hgaps = snapSpace(getHgap()) * (nCols - 1);
                // compute content width
                final double contentWidth = super.computePrefWidth(height) - leftSpace - rightSpace - hgaps;
                return ((snapSize(contentWidth / nCols)) * nCols) + leftSpace + rightSpace + hgaps;
            }

            @Override
            protected void layoutChildren() {
                if (getWidth() > 0 && getHeight() > 0) {
                    super.layoutChildren();
                }
            }
        };
        contentGrid.setFocusTraversable(true);
        contentGrid.getStyleClass().add("calendar-grid");
        contentGrid.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT,
            CornerRadii.EMPTY,
            Insets.EMPTY)));
        contentGrid.setPadding(new Insets(0, 12, 12, 12));
        contentGrid.setVgap(0);
        contentGrid.setHgap(0);

        // create week days cells
        createWeekDaysCells();
        // create month days cells
        createDayCells();

        VBox contentHolder = new VBox();
        // create content pane
        contentHolder.getChildren().setAll(createCalendarMonthLabelPane(), contentGrid);
        // add month arrows pane
        calendarStackPane.getChildren().setAll(contentHolder, createCalendarArrowsPane());

        // create years list view
        for (int i = 0; i <= 200; i++) {
            yearsListView.getItems().add(Integer.toString(1900 + i));
        }
        yearsListView.setVisible(false);
        yearsListView.setOpacity(0);
        yearsListView.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT,
            CornerRadii.EMPTY,
            Insets.EMPTY)));

        /***************************************************************************
         *                                                                         *
         * Time Picker Content                                                     *
         * >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> *
         **************************************************************************/

        // create clock content pane
        clockPlaceHolder.getChildren().add(createContentPane(time, is24HourView));

        // add listeners
        unit.addListener((o, oldVal, newVal) -> {
            if (newVal == TimeUnit.HOURS) {
                angle.set(Math.toDegrees(2 * Math.PI / 12));
                int tmp = Integer.parseInt(selectedHourLabel.getText());
                if (is24HourView) {
                    if (tmp == 0 || tmp > 12) {
                        hoursContent.getChildren().get(0).setVisible(false);
                        hoursContent.getChildren().get(1).setVisible(true);
                    } else {
                        hoursContent.getChildren().get(1).setVisible(false);
                        hoursContent.getChildren().get(0).setVisible(true);
                    }
                }
                pointerRotate.set(_24HourHoursPointerRotate);
                _24HourPointerRotate.set(_24HourHoursPointerRotate);
                timeLabel.set(selectedHourLabel);
            } else if (newVal == TimeUnit.MINUTES) {
                angle.set(Math.toDegrees(2 * Math.PI / 60));
                pointerRotate.set(minsPointerRotate);
                timeLabel.set(selectedMinLabel);
            }
            swapLabelsColor(selectedHourLabel, selectedMinLabel);
            switchTimeUnit(newVal);
        });

        if (!is24HourView) {
            period.addListener((o, oldVal, newVal) -> {
                swapLabelsColor(periodPMLabel, periodAMLabel);
                updateValue();
            });
        }

        /***************************************************************************
         * <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< *
         * Time Picker Content                                                     *
         *                                                                         *
         **************************************************************************/

        /***************************************************************************
         *                                                                         *
         * Date and Time picker's content merge                                    *
         * >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> *
         **************************************************************************/

        // clip one base pane that contains all content, instead of each content's pane
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(contentPlaceHolder.widthProperty());
        clip.heightProperty().bind(contentPlaceHolder.heightProperty());
        contentPlaceHolder.setClip(clip);

        StackPane stackPaneCalendarMerge = new StackPane();
        stackPaneCalendarMerge.getChildren().setAll(calendarStackPane, yearsListView);

        calendarPlaceHolder.getChildren().setAll(stackPaneCalendarMerge);
        yearsListView.maxWidthProperty().bind(contentPlaceHolder.widthProperty());
        yearsListView.maxHeightProperty().bind(contentPlaceHolder.heightProperty());
        contentPlaceHolder.getChildren().setAll(calendarPlaceHolder, clockPlaceHolder);
        getChildren().add(contentPlaceHolder);

        /***************************************************************************
         * <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< *
         * Date and Time picker's content merge                                    *
         *                                                                         *
         **************************************************************************/

        refresh();

        scrollToYear();

        addEventHandler(KeyEvent.ANY, event -> {
            Node node = getScene().getFocusOwner();
            if (node instanceof DateCell) {
                currentFocusedDayCell = (DateCell) node;
            }

            switch (event.getCode()) {
                case HOME:
                    // go to the current date
                    init();
                    goToDate(LocalDate.now(), true);
                    event.consume();
                    break;
                case PAGE_UP:
                    if (!backMonthButton.isDisabled()) {
                        forward(-1, MONTHS, true, true);
                    }
                    event.consume();
                    break;
                case PAGE_DOWN:
                    if (!forwardMonthButton.isDisabled()) {
                        forward(1, MONTHS, true, true);
                    }
                    event.consume();
                    break;
                case ESCAPE:
                    dateTimePicker.hide();
                    event.consume();
                    break;
                case F4:
                case F10:
                case UP:
                case DOWN:
                case LEFT:
                case RIGHT:
                case TAB:
                    break;
                default:
                    event.consume();
            }
        });

        // create animation
        showTransition = new CachedTransition(yearsListView,
            new Timeline(
                new KeyFrame(Duration.millis(0),
                    new KeyValue(yearsListView.opacityProperty(),
                        0,
                        Interpolator.EASE_BOTH),
                    new KeyValue(calendarStackPane.opacityProperty(),
                        1,
                        Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(500),
                    new KeyValue(yearsListView.opacityProperty(),
                        0,
                        Interpolator.EASE_BOTH),
                    new KeyValue(calendarStackPane.opacityProperty(),
                        0,
                        Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(1000),
                    new KeyValue(yearsListView.opacityProperty(),
                        1,
                        Interpolator.EASE_BOTH),
                    new KeyValue(calendarStackPane.opacityProperty(),
                        0,
                        Interpolator.EASE_BOTH),
                    new KeyValue(selectedYearLabel.textFillProperty(),
                        Color.WHITE,
                        Interpolator.EASE_BOTH),
                    new KeyValue(selectedDateLabel.textFillProperty(),
                        Color.rgb(255, 255, 255, 0.67),
                        Interpolator.EASE_BOTH)))) {
            {
                setCycleDuration(Duration.millis(320));
                setDelay(Duration.seconds(0));
            }

            @Override
            protected void starting() {
                super.starting();
                yearsListView.setVisible(true);
            }
        };

        hideTransition = new CachedTransition(yearsListView,
            new Timeline(
                new KeyFrame(Duration.millis(0),
                    new KeyValue(yearsListView.opacityProperty(),
                        1,
                        Interpolator.EASE_BOTH),
                    new KeyValue(calendarStackPane.opacityProperty(),
                        0,
                        Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(500),
                    new KeyValue(yearsListView.opacityProperty(),
                        0,
                        Interpolator.EASE_BOTH),
                    new KeyValue(calendarStackPane.opacityProperty(),
                        0,
                        Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(1000),
                    new KeyValue(yearsListView.opacityProperty(),
                        0,
                        Interpolator.EASE_BOTH),
                    new KeyValue(calendarStackPane.opacityProperty(),
                        1,
                        Interpolator.EASE_BOTH),
                    new KeyValue(selectedDateLabel.textFillProperty(),
                        Color.WHITE,
                        Interpolator.EASE_BOTH),
                    new KeyValue(selectedYearLabel.textFillProperty(),
                        Color.rgb(255, 255, 255, 0.67),
                        Interpolator.EASE_BOTH)))) {
            {
                setCycleDuration(Duration.millis(320));
                setDelay(Duration.seconds(0));
            }

            @Override
            protected void stopping() {
                super.stopping();
                yearsListView.setVisible(false);
            }
        };
    }

    private final void scrollToYear() {
        int yearIndex = Integer.parseInt(selectedYearLabel.getText()) - 1900 - 2;
        yearsListView.scrollTo(yearIndex >= 0 ? yearIndex : yearIndex + 2);
    }

    @Override
    public String getUserAgentStylesheet() {
        return JFXDateTimePickerContent.class.getResource("/css/controls/jfx-date-time-picker.css").toExternalForm();
    }

    ObjectProperty<YearMonth> displayedYearMonthProperty() {
        return selectedYearMonth;
    }

    private void createWeekDaysCells() {
        // create week days names
        for (int i = 0; i < daysPerWeek; i++) {
            DateCell cell = new DateCell();
            cell.getStyleClass().add("day-name-cell");
            cell.setTextFill(DEFAULT_CELL_COLOR);
            cell.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
            cell.setFont(Font.font(ROBOTO, FontWeight.BOLD, 12));
            cell.setAlignment(Pos.BASELINE_CENTER);
            weekDaysCells.add(cell);
        }
        // create week days numbers
        for (int i = 0; i < 6; i++) {
            DateCell cell = new DateCell();
            cell.getStyleClass().add("week-number-cell");
            cell.setTextFill(DEFAULT_CELL_COLOR);
            cell.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
            cell.setFont(Font.font(ROBOTO, FontWeight.BOLD, 12));
            weekNumberCells.add(cell);
        }
    }

    /*
     * header panel represents the selected Date
     * we keep javaFX original style classes
     */
    protected VBox createHeaderPane() {
        // Year label
        selectedYearLabel = new Label();
        selectedYearLabel.getStyleClass().add(SPINNER_LABEL);
        selectedYearLabel.setTextFill(Color.rgb(255, 255, 255, 0.67));
        selectedYearLabel.setFont(Font.font(ROBOTO, FontWeight.BOLD, 14));
        // Year label container
        HBox yearLabelContainer = new HBox();
        yearLabelContainer.getStyleClass().add("spinner");
        yearLabelContainer.getChildren().addAll(selectedYearLabel);
        yearLabelContainer.setAlignment(Pos.CENTER_LEFT);
        yearLabelContainer.setFillHeight(false);
        yearLabelContainer.setOnMouseClicked((click) -> {
            if (!yearsListView.isVisible()) {
                scrollToYear();
                hideTransition.stop();
                showTransition.play();
            }

            /***************************************************************************
             *                                                                         *
             * Date and Time picker's content merge                                    *
             * >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> *
             **************************************************************************/

            if (!calendarPlaceHolder.isVisible()) {
                this.switchDateTimeView(-1, clockPlaceHolder, calendarPlaceHolder);
                currentVisibleClock = false;
            }

            /***************************************************************************
             * <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< *
             * Date and Time picker's content merge                                    *
             *                                                                         *
             **************************************************************************/
        });

        // selected date label
        selectedDateLabel = new Label();
        selectedDateLabel.getStyleClass().add(SPINNER_LABEL);
        selectedDateLabel.setTextFill(Color.WHITE);
        selectedDateLabel.setFont(Font.font(ROBOTO, FontWeight.BOLD, 24)); // 32
        // selected date label container
        HBox selectedDateContainer = new HBox(selectedDateLabel);
        selectedDateContainer.getStyleClass().add("spinner");
        selectedDateContainer.setAlignment(Pos.CENTER_LEFT);
        selectedDateContainer.setOnMouseClicked((click) -> {
            if (yearsListView.isVisible()) {
                showTransition.stop();
                hideTransition.play();
            }

            /***************************************************************************
             *                                                                         *
             * Date and Time picker's content merge                                    *
             * >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> *
             **************************************************************************/

            if (!calendarPlaceHolder.isVisible()) {
                this.switchDateTimeView(-1, clockPlaceHolder, calendarPlaceHolder);
                currentVisibleClock = false;
            }

            /***************************************************************************
             * <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< *
             * Date and Time picker's content merge                                    *
             *                                                                         *
             **************************************************************************/
        });

        VBox headerPanel = new VBox();
        headerPanel.getStyleClass().add("month-year-pane");
        headerPanel.setBackground(new Background(new BackgroundFill(this.dateTimePicker.getDefaultColor(),
            CornerRadii.EMPTY,
            Insets.EMPTY)));
        headerPanel.setPadding(new Insets(12, 24, 12, 24));
        headerPanel.getChildren().add(yearLabelContainer);
        headerPanel.getChildren().add(selectedDateContainer);
        return headerPanel;
    }

    /*
     * methods to create the content of the date picker
     */
    protected BorderPane createCalendarArrowsPane() {

        SVGGlyph leftChevron = new SVGGlyph(0,
            "CHEVRON_LEFT",
            "M 742,-37 90,614 Q 53,651 53,704.5 53,758 90,795 l 652,651 q 37,37 90.5,37 53.5,0 90.5,-37 l 75,-75 q 37,-37 37,-90.5 0,-53.5 -37,-90.5 L 512,704 998,219 q 37,-38 37,-91 0,-53 -37,-90 L 923,-37 Q 886,-74 832.5,-74 779,-74 742,-37 z",
            Color.GRAY);
        SVGGlyph rightChevron = new SVGGlyph(0,
            "CHEVRON_RIGHT",
            "m 1099,704 q 0,-52 -37,-91 L 410,-38 q -37,-37 -90,-37 -53,0 -90,37 l -76,75 q -37,39 -37,91 0,53 37,90 l 486,486 -486,485 q -37,39 -37,91 0,53 37,90 l 76,75 q 36,38 90,38 54,0 90,-38 l 652,-651 q 37,-37 37,-90 z",
            Color.GRAY);
        leftChevron.setFill(DEFAULT_COLOR);
        leftChevron.setSize(6, 11);
        rightChevron.setFill(DEFAULT_COLOR);
        rightChevron.setSize(6, 11);

        backMonthButton = new JFXButton();
        backMonthButton.setMinSize(40, 40);
        backMonthButton.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT,
            new CornerRadii(40),
            Insets.EMPTY)));
        backMonthButton.getStyleClass().add("left-button");
        backMonthButton.setGraphic(leftChevron);
        backMonthButton.setRipplerFill(this.dateTimePicker.getDefaultColor());
        backMonthButton.setOnAction(t -> forward(-1, MONTHS, false, true));

        forwardMonthButton = new JFXButton();
        forwardMonthButton.setMinSize(40, 40);
        forwardMonthButton.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT,
            new CornerRadii(40),
            Insets.EMPTY)));
        forwardMonthButton.getStyleClass().add("right-button");
        forwardMonthButton.setGraphic(rightChevron);
        forwardMonthButton.setRipplerFill(this.dateTimePicker.getDefaultColor());
        forwardMonthButton.setOnAction(t -> forward(1, MONTHS, false, true));

        BorderPane arrowsContainer = new BorderPane();
        arrowsContainer.setLeft(backMonthButton);
        arrowsContainer.setRight(forwardMonthButton);
        arrowsContainer.setPadding(new Insets(4, 12, 2, 12));
        arrowsContainer.setPickOnBounds(false);
        return arrowsContainer;
    }

    protected BorderPane createCalendarMonthLabelPane() {
        monthYearLabel = new Label();
        monthYearLabel.getStyleClass().add(SPINNER_LABEL);
        monthYearLabel.setFont(Font.font(ROBOTO, FontWeight.BOLD, 13));
        monthYearLabel.setTextFill(DEFAULT_COLOR);

        BorderPane monthContainer = new BorderPane();
        monthContainer.setMinHeight(50);
        monthContainer.setCenter(monthYearLabel);
        monthContainer.setPadding(new Insets(2, 12, 2, 12));
        return monthContainer;
    }

    void updateContentGrid() {
        contentGrid.getColumnConstraints().clear();
        contentGrid.getChildren().clear();
        int colsNumber = daysPerWeek + (dateTimePicker.isShowWeekNumbers() ? 1 : 0);
        ColumnConstraints columnConstraints = new ColumnConstraints();
        columnConstraints.setPercentWidth(100);
        for (int i = 0; i < colsNumber; i++) {
            contentGrid.getColumnConstraints().add(columnConstraints);
        }

        // Week days cells
        for (int i = 0; i < daysPerWeek; i++) {
            contentGrid.add(weekDaysCells.get(i), i + colsNumber - daysPerWeek, 1);
        }

        // Week number cells
        if (dateTimePicker.isShowWeekNumbers()) {
            for (int i = 0; i < 6; i++) {
                contentGrid.add(weekNumberCells.get(i), 0, i + 2);
            }
        }

        // Month days cells
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < daysPerWeek; col++) {
                contentGrid.add(dayCells.get(row * daysPerWeek + col), col + colsNumber - daysPerWeek, row + 2);
            }
        }
    }

    private void refresh() {
        updateDayNameCells();
        updateValues();
    }

    private void updateDayNameCells() {
        int weekFirstDay = WeekFields.of(getLocale()).getFirstDayOfWeek().getValue();
        LocalDate date = LocalDate.of(2009, 7, 12 + weekFirstDay);
        for (int i = 0; i < daysPerWeek; i++) {
            String name = weekDayNameFormatter.withLocale(getLocale()).format(date.plus(i, DAYS));
            // Fix Chinese environment week display incorrectly
            if (weekDayNameFormatter.getLocale() == java.util.Locale.CHINA) {
                name = name.substring(2, 3).toUpperCase();
            } else {
                name = name.substring(0, 1).toUpperCase();
            }
            weekDaysCells.get(i).setText(name);
        }
    }

    void updateValues() {
        updateWeekNumberDateCells();
        updateDayCells();
        updateMonthYearPane();
    }

    void updateWeekNumberDateCells() {
        if (dateTimePicker.isShowWeekNumbers()) {
            final Locale locale = getLocale();
            LocalDate firstDayOfMonth = selectedYearMonth.get().atDay(1);
            for (int i = 0; i < 6; i++) {
                LocalDate date = firstDayOfMonth.plus(i, WEEKS);
                String weekNumber = weekNumberFormatter.withLocale(locale)
                    .withDecimalStyle(DecimalStyle.of(locale))
                    .format(date);
                weekNumberCells.get(i).setText(weekNumber);
            }
        }
    }

    private void updateDayCells() {
        Locale locale = getLocale();
        Chronology chrono = getPrimaryChronology();
        // get the index of the first day of the month
        int firstDayOfWeek = WeekFields.of(getLocale()).getFirstDayOfWeek().getValue();
        int firstOfMonthIndex = selectedYearMonth.get().atDay(1).getDayOfWeek().getValue() - firstDayOfWeek;
        firstOfMonthIndex += firstOfMonthIndex < 0 ? daysPerWeek : 0;
        YearMonth currentYearMonth = selectedYearMonth.get();

        int daysInCurMonth = -1;

        for (int i = 0; i < 6 * daysPerWeek; i++) {
            DateCell dayCell = dayCells.get(i);
            dayCell.getStyleClass().setAll("cell", "date-cell", "day-cell");
            dayCell.setPrefSize(40, 42);
            dayCell.setDisable(false);
            dayCell.setStyle(null);
            dayCell.setGraphic(null);
            dayCell.setTooltip(null);
            dayCell.setTextFill(DEFAULT_COLOR);
            dayCell.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT,
                CornerRadii.EMPTY,
                Insets.EMPTY)));

            try {
                if (daysInCurMonth == -1) {
                    daysInCurMonth = currentYearMonth.lengthOfMonth();
                }

                int dayIndex = i - firstOfMonthIndex + 1;

                LocalDate date = currentYearMonth.atDay(dayIndex);
                dayCellDates[i] = date;

                // if it's today
                if (date.equals(LocalDate.now())) {
                    dayCell.setTextFill(this.dateTimePicker.getDefaultColor());
                    dayCell.getStyleClass().add("today");
                }
                // if it's the current selected value
                LocalDateTime dateTime = dateTimePicker.getValue();
                if (dateTime != null && date.equals(dateTime.toLocalDate())) {
                    dayCell.getStyleClass().add("selected");
                    dayCell.setTextFill(Color.WHITE);
                    dayCell.setBackground(
                        new Background(new BackgroundFill(this.dateTimePicker.getDefaultColor(),
                            new CornerRadii(40),
                            Insets.EMPTY)));
                }

                ChronoLocalDate cDate = chrono.date(date);
                String cellText = dayCellFormatter.withLocale(locale)
                    .withChronology(chrono)
                    .withDecimalStyle(DecimalStyle.of(locale))
                    .format(cDate);
                dayCell.setText(cellText);
                if (i < firstOfMonthIndex) {
                    dayCell.getStyleClass().add("previous-month");
                    dayCell.setText("");
                } else if (i >= firstOfMonthIndex + daysInCurMonth) {
                    dayCell.getStyleClass().add("next-month");
                    dayCell.setText("");
                }
                // update cell item
                dayCell.updateItem(date, false);
            } catch (DateTimeException ex) {
                // Disable day cell if its date is out of range
                dayCell.setText("");
                dayCell.setDisable(true);
            }
        }
    }

    protected void updateMonthYearPane() {
        // update date labels
        YearMonth yearMonth = selectedYearMonth.get();
        LocalDate value = dateTimePicker.getValue() == null ?
            LocalDate.now() : dateTimePicker.getValue().toLocalDate();
        selectedDateLabel.setText(DateTimeFormatter.ofPattern("EEE, MMM dd").format(value));

        selectedYearLabel.setText(formatYear(yearMonth));
        monthYearLabel.setText(formatMonth(yearMonth) + " " + formatYear(yearMonth));

        Chronology chrono = dateTimePicker.getChronology();
        LocalDate firstDayOfMonth = yearMonth.atDay(1);
        backMonthButton.setDisable(!isValidDate(chrono, firstDayOfMonth, -1, DAYS));
        forwardMonthButton.setDisable(!isValidDate(chrono, firstDayOfMonth, +1, MONTHS));
    }

    private String formatMonth(YearMonth yearMonth) {
        try {
            Chronology chrono = getPrimaryChronology();
            ChronoLocalDate cDate = chrono.date(yearMonth.atDay(1));
            return monthFormatter.withLocale(getLocale()).withChronology(chrono).format(cDate);
        } catch (DateTimeException ex) {
            // Date is out of range.
            return "";
        }
    }

    private String formatYear(YearMonth yearMonth) {
        try {
            Chronology chrono = getPrimaryChronology();
            ChronoLocalDate cDate = chrono.date(yearMonth.atDay(1));
            return yearFormatter.withLocale(getLocale())
                .withChronology(chrono)
                .withDecimalStyle(DecimalStyle.of(getLocale()))
                .format(cDate);
        } catch (DateTimeException ex) {
            // Date is out of range.
            return "";
        }
    }

    protected LocalDate dayCellDate(DateCell dateCell) {
        assert dayCellDates != null;
        return dayCellDates[dayCells.indexOf(dateCell)];
    }

    protected void forward(int offset, ChronoUnit unit, boolean focusDayCell, boolean withAnimation) {
        if (withAnimation) {
            if (tempImageTransition == null || tempImageTransition.getStatus() == Status.STOPPED) {
                Pane monthContent = (Pane) calendarStackPane.getChildren().get(0);
                this.getParent().setManaged(false);
                SnapshotParameters snapShotparams = new SnapshotParameters();
                snapShotparams.setFill(Color.TRANSPARENT);
                WritableImage temp = monthContent.snapshot(snapShotparams,
                    new WritableImage((int) monthContent.getWidth(),
                        (int) monthContent.getHeight()));
                ImageView tempImage = new ImageView(temp);
                calendarStackPane.getChildren().add(0, tempImage);
                TranslateTransition imageTransition = new TranslateTransition(Duration.millis(160), tempImage);
                imageTransition.setToX(-offset * calendarStackPane.getWidth());
                imageTransition.setOnFinished((finish) -> calendarStackPane.getChildren().remove(tempImage));
                monthContent.setTranslateX(offset * calendarStackPane.getWidth());
                TranslateTransition contentTransition = new TranslateTransition(Duration.millis(160), monthContent);
                contentTransition.setToX(0);

                tempImageTransition = new ParallelTransition(imageTransition, contentTransition);
                tempImageTransition.setOnFinished((finish) -> {
                    calendarStackPane.getChildren().remove(tempImage);
                    this.getParent().setManaged(true);
                });
                tempImageTransition.play();
            }
        }
        YearMonth yearMonth = selectedYearMonth.get();
        DateCell dateCell = currentFocusedDayCell;
        if (dateCell == null || !(dayCellDate(dateCell).getMonth() == yearMonth.getMonth())) {
            dateCell = findDayCellOfDate(yearMonth.atDay(1));
        }
        goToDayCell(dateCell, offset, unit, focusDayCell);
    }

    /***************************************************************************
     *                                                                         *
     * Date and Time picker's content merge                                    *
     * >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> *
     **************************************************************************/

    /**
     * Animated transition from one panel to another
     * <p>Each panel should be contained to another one overabundant panel</p>
     * @param offset moving animation direction
     * @param from replaceable panel
     * @param to replacing panel
     */
    private void switchDateTimeView(int offset, Pane from, Pane to) {
        // side moving animation, like forward() method
        if (tempImageTransition == null || tempImageTransition.getStatus() == Status.STOPPED) {

            // get target panel from overabundant one
            Pane innerContent = (Pane) to.getChildren().get(0);
            this.getParent().setManaged(false);

            // take rendered image from replaceable panel
            SnapshotParameters snapShotparams = new SnapshotParameters();
            snapShotparams.setFill(Color.TRANSPARENT);
            WritableImage temp = from.snapshot(snapShotparams,
                new WritableImage((int) from.getWidth(),
                    (int) from.getHeight()));
            ImageView tempImage = new ImageView(temp);

            // make replaceable invisible and replacing visible
            from.setOpacity(0);
            from.setVisible(false);
            to.setOpacity(1);
            to.setVisible(true);

            // add render to replacing container
            to.getChildren().add(0, tempImage);

            // animate
            TranslateTransition imageTransition = new TranslateTransition(Duration.millis(160), tempImage);
            imageTransition.setToX(-offset * from.getWidth());
            imageTransition.setOnFinished((finish) -> to.getChildren().remove(tempImage));

            innerContent.setTranslateX(offset * from.getWidth());
            TranslateTransition contentTransition = new TranslateTransition(Duration.millis(160), innerContent);
            contentTransition.setToX(0);

            tempImageTransition = new ParallelTransition(imageTransition, contentTransition);
            tempImageTransition.setOnFinished((finish) -> {
                this.getParent().setManaged(true);
            });
            tempImageTransition.play();
        }
        // transparent transition animation
//        if (from.isVisible()) {
//            Timeline fadeout = new Timeline(new KeyFrame(Duration.millis(320),
//                new KeyValue(from.opacityProperty(),
//                    0,
//                    Interpolator.EASE_BOTH)));
//            Timeline fadein = new Timeline(new KeyFrame(Duration.millis(320),
//                new KeyValue(to.opacityProperty(),
//                    1,
//                    Interpolator.EASE_BOTH)));
//            new ParallelTransition(fadeout, fadein).play();
//            from.setVisible(false);
//            to.setVisible(true);
//        }
    }
    
    /***************************************************************************
     * <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< *
     * Date and Time picker's content merge                                    *
     *                                                                         *
     **************************************************************************/

    private void goToDayCell(DateCell dateCell, int offset, ChronoUnit unit, boolean focusDayCell) {
        goToDate(dayCellDate(dateCell).plus(offset, unit), focusDayCell);
    }

    private void goToDate(LocalDate date, boolean focusDayCell) {
        if (isValidDate(dateTimePicker.getChronology(), date)) {
            selectedYearMonth.set(YearMonth.from(date));
            if (focusDayCell) {
                findDayCellOfDate(date).requestFocus();
            }
        }
    }

    private void selectDayCell(DateCell dateCell) {
        LocalTime time = dateTimePicker.getValue() == null ?
            LocalTime.now() : dateTimePicker.getValue().toLocalTime();
        dateTimePicker.setValue(LocalDateTime.of(dayCellDate(dateCell), time));
        dateTimePicker.hide();
    }

    private DateCell findDayCellOfDate(LocalDate date) {
        for (int i = 0; i < dayCellDates.length; i++) {
            if (date.equals(dayCellDates[i])) {
                return dayCells.get(i);
            }
        }
        return dayCells.get(dayCells.size() / 2 + 1);
    }

    void init() {
        calendarStackPane.setOpacity(!currentVisibleClock ? 1 : 0);
        calendarStackPane.setVisible(!currentVisibleClock);
        selectedDateLabel.setTextFill(Color.WHITE);
        selectedYearLabel.setTextFill(Color.rgb(255, 255, 255, 0.67));
        yearsListView.setOpacity(0);
        yearsListView.setVisible(false);

        /***************************************************************************
         *                                                                         *
         * Time Picker Content                                                     *
         * >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> *
         **************************************************************************/

        clockPlaceHolder.setOpacity(currentVisibleClock ? 1 : 0);
        clockPlaceHolder.setVisible(currentVisibleClock);
        if(unit.get() == TimeUnit.HOURS){
            selectedHourLabel.setTextFill(Color.rgb(255, 255, 255, 0.87));
        }else{
            selectedMinLabel.setTextFill(Color.rgb(255, 255, 255, 0.87));
        }

        /***************************************************************************
         * <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< *
         * Time Picker Content                                                     *
         *                                                                         *
         **************************************************************************/
    }

    void clearFocus() {
        LocalDate focusDate = dateTimePicker.getValue() == null ?
            LocalDate.now() : dateTimePicker.getValue().toLocalDate();
        if (YearMonth.from(focusDate).equals(selectedYearMonth.get())) {
            goToDate(focusDate, true);
        }

        /***************************************************************************
         *                                                                         *
         * Time Picker Content                                                     *
         * >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> *
         **************************************************************************/

        LocalDateTime focusTime = dateTimePicker.getValue();
        if (focusTime == null) {
            focusTime = LocalDateTime.now();
        }
        goToTime(focusTime);

        /***************************************************************************
         * <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< *
         * Time Picker Content                                                     *
         *                                                                         *
         **************************************************************************/
    }

    protected void createDayCells() {
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < daysPerWeek; col++) {
                DateCell dayCell = createDayCell();
                dayCell.addEventHandler(MouseEvent.MOUSE_CLICKED, click -> {
                    // allow date selection on mouse primary button click
                    if (click.getButton() != MouseButton.PRIMARY) {
                        return;
                    }
                    DateCell selectedDayCell = (DateCell) click.getSource();
                    selectDayCell(selectedDayCell);
                    currentFocusedDayCell = selectedDayCell;
                });
                // add mouse hover listener
                dayCell.setOnMouseEntered((event) -> {
                    if (!dayCell.getStyleClass().contains("selected")) {
                        dayCell.setBackground(new Background(new BackgroundFill(Color.valueOf("#EDEDED"),
                            new CornerRadii(40),
                            Insets.EMPTY)));
                    }
                });
                dayCell.setOnMouseExited((event) -> {
                    if (!dayCell.getStyleClass().contains("selected")) {
                        dayCell.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT,
                            CornerRadii.EMPTY,
                            Insets.EMPTY)));
                    }
                });
                dayCell.setAlignment(Pos.BASELINE_CENTER);
                dayCell.setBorder(
                    new Border(new BorderStroke(Color.TRANSPARENT,
                        BorderStrokeStyle.SOLID,
                        CornerRadii.EMPTY,
                        new BorderWidths(5))));
                dayCell.setFont(Font.font(ROBOTO, FontWeight.BOLD, 12));
                dayCells.add(dayCell);
            }
        }
        dayCellDates = new LocalDate[6 * daysPerWeek];
        // position the cells into the grid
        updateContentGrid();
    }

    private DateCell createDayCell() {
        DateCell dayCell = null;
        // call cell factory if set by the user
        if (dateTimePicker.getDayCellFactory() != null) {
            dayCell = dateTimePicker.getDayCellFactory().call(dateTimePicker);
        }
        // else create the defaul day cell
        if (dayCell == null) {
            dayCell = new DateCell();
        }
        return dayCell;
    }

    /**
     * this method must be overriden when implementing other Chronolgy
     */
    protected Chronology getPrimaryChronology() {
        return dateTimePicker.getChronology();
    }

    protected Locale getLocale() {
        // for android compatibility
        return Locale.getDefault(/*Locale.Category.FORMAT*/);
    }

    protected boolean isValidDate(Chronology chrono, LocalDate date, int offset, ChronoUnit unit) {
        return date != null && isValidDate(chrono, date.plus(offset, unit));
    }

    protected boolean isValidDate(Chronology chrono, LocalDate date) {
        try {
            if (date != null) {
                chrono.date(date);
            }
            return true;
        } catch (DateTimeException ex) {
            return false;
        }
    }
    
    /***************************************************************************
     *                                                                         *
     * Time Picker Content                                                     *
     * >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> *
     **************************************************************************/

    protected BorderPane createContentPane(LocalTime time, boolean _24HourView) {
        Circle circle = new Circle(contentCircleRadius),
            selectionCircle = new Circle(contentCircleRadius / 6);
        circle.setFill(Color.rgb(224, 224, 224, 0.67));

        EventHandler<? super MouseEvent> mouseActionHandler = (event) -> {
            double dx = event.getX();
            double dy = event.getY();
            double shift = 9;
            double theta = Math.atan2(dy, dx);
            int index = (int) Math.round((180 + Math.toDegrees(theta)) / angle.get()),
                timeValue;
            if (_24HourView) {
                if (Point2D.distance(0, 0, dx, dy) >= (contentCircleRadius - shift - (2 * selectionCircle.getRadius()))) {
                    hoursContent.getChildren().get(1).setVisible(false);
                    hoursContent.getChildren().get(0).setVisible(true);
                    pointerRotate.get().setAngle(index * angle.get());
                    timeValue = (index + 9) % 12 == 0 ? 12 : (index + 9) % 12;
                } else {
                    hoursContent.getChildren().get(0).setVisible(false);
                    hoursContent.getChildren().get(1).setVisible(true);
                    _24HourPointerRotate.get().setAngle(index * angle.get());
                    int tmp = ((index + 21) % 24 <= 13 ? (index + 21) % 24 + 12 : (index + 21) % 24);
                    timeValue = tmp == 12 ? 0 : tmp;
                }
            } else {
                pointerRotate.get().setAngle(index * angle.get());
                timeValue = (index + 9) % 12 == 0 ? 12 : (index + 9) % 12;
            }
            if (unit.get() == TimeUnit.MINUTES) {
                timeValue = (index + 45) % 60;
            }
            timeLabel.get().setText(unit.get() == TimeUnit.MINUTES ? unitConverter.toString(timeValue) : Integer.toString(timeValue));
            updateValue();
        };

        circle.setOnMousePressed(mouseActionHandler);
        circle.setOnMouseDragged(mouseActionHandler);

        hoursContent = createHoursContent(time, _24HourView);
        hoursContent.setMouseTransparent(true);
        minutesContent = createMinutesContent(time);
        minutesContent.setOpacity(0);
        minutesContent.setMouseTransparent(true);

        StackPane contentPane = new StackPane();
        contentPane.getChildren().addAll(circle, hoursContent, minutesContent);
        contentPane.setPadding(new Insets(12));

        BorderPane contentContainer = new BorderPane();
        contentContainer.setCenter(contentPane);
        contentContainer.setMinHeight(50);
        contentContainer.setPadding(new Insets(2, 12, 2, 12));
        return contentContainer;
    }

    /*
     * header panel represents the selected Time
     * we keep javaFX original style classes
     */
    protected StackPane createHeaderPane(LocalTime time, boolean _24HourView) {
        int hour = time.getHour();

        selectedHourLabel.setText(String.valueOf(hour % (_24HourView ? 24 : 12) == 0 ? (_24HourView ? 0 : 12) : hour % (_24HourView ? 24 : 12)));
        selectedHourLabel.getStyleClass().add(SPINNER_LABEL);
        selectedHourLabel.setTextFill(Color.WHITE);
        selectedHourLabel.setFont(Font.font(ROBOTO, FontWeight.BOLD, 32)); // 42
        selectedHourLabel.setOnMouseClicked((click) -> {
            unit.set(TimeUnit.HOURS);

            /***************************************************************************
             *                                                                         *
             * Date and Time picker's content merge                                    *
             * >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> *
             **************************************************************************/

            if (!clockPlaceHolder.isVisible()) {
                this.switchDateTimeView(1, calendarPlaceHolder, clockPlaceHolder);
                currentVisibleClock = true;
            }

            /***************************************************************************
             * <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< *
             * Date and Time picker's content merge                                    *
             *                                                                         *
             **************************************************************************/
        });
        selectedHourLabel.setMinWidth(49);
        selectedHourLabel.setAlignment(Pos.CENTER_RIGHT);
        timeLabel.set(selectedHourLabel);

        selectedMinLabel.setText(String.valueOf(unitConverter.toString(time.getMinute())));
        selectedMinLabel.getStyleClass().add(SPINNER_LABEL);
        selectedMinLabel.setTextFill(fadedColor);
        selectedMinLabel.setFont(Font.font(ROBOTO, FontWeight.BOLD, 32)); // 42
        selectedMinLabel.setOnMouseClicked((click) -> {
            unit.set(TimeUnit.MINUTES);

            /***************************************************************************
             *                                                                         *
             * Date and Time picker's content merge                                    *
             * >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> *
             **************************************************************************/

            if (!clockPlaceHolder.isVisible()) {
                this.switchDateTimeView(1, calendarPlaceHolder, clockPlaceHolder);
                currentVisibleClock = true;
            }

            /***************************************************************************
             * <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< *
             * Date and Time picker's content merge                                    *
             *                                                                         *
             **************************************************************************/
        });

        Label separatorLabel = new Label(":");
        separatorLabel.setPadding(new Insets(0, 0, 4, 0));
        separatorLabel.setTextFill(fadedColor);
        separatorLabel.setFont(Font.font(ROBOTO, FontWeight.BOLD, 32)); // 42

        periodPMLabel = new Label("PM");
        periodPMLabel.getStyleClass().add(SPINNER_LABEL);
        periodPMLabel.setTextFill(fadedColor);
        periodPMLabel.setFont(Font.font(ROBOTO, FontWeight.BOLD, 14));
        periodPMLabel.setOnMouseClicked((click) -> period.set("PM"));

        periodAMLabel = new Label("AM");
        periodAMLabel.getStyleClass().add(SPINNER_LABEL);
        periodAMLabel.setTextFill(fadedColor);
        periodAMLabel.setFont(Font.font(ROBOTO, FontWeight.BOLD, 14));
        periodAMLabel.setOnMouseClicked((click) -> period.set("AM"));

        // init period value
        if (hour < 12) {
            periodAMLabel.setTextFill(Color.WHITE);
        } else {
            periodPMLabel.setTextFill(Color.WHITE);
        }
        period.set(hour < 12 ? "AM" : "PM");

        VBox periodContainer = new VBox();
        periodContainer.setPadding(new Insets(0, 0, 0, 4));
        periodContainer.getChildren().addAll(periodAMLabel, periodPMLabel);

        // Year label container
        HBox selectedTimeContainer = new HBox();
        selectedTimeContainer.getStyleClass().add("spinner");
        selectedTimeContainer.getChildren()
            .addAll(selectedHourLabel, separatorLabel, selectedMinLabel);
        if (!_24HourView) {
            selectedTimeContainer.getChildren().add(periodContainer);
        }
        selectedTimeContainer.setAlignment(Pos.CENTER);
        selectedTimeContainer.setFillHeight(false);

        StackPane headerPanel = new StackPane();
        headerPanel.getStyleClass().add("time-pane");
        headerPanel.setBackground(new Background(new BackgroundFill(this.dateTimePicker.getDefaultColor(),
            CornerRadii.EMPTY,
            Insets.EMPTY)));
        headerPanel.setPadding(new Insets(8, 24, 8, 24));
        headerPanel.getChildren().add(selectedTimeContainer);
        return headerPanel;
    }

    private StackPane createHoursContent(LocalTime time, boolean _24HourView) {
        // create hours content
        StackPane hoursPointer = new StackPane(), _24HoursPointer = new StackPane();
        Circle selectionCircle = new Circle(contentCircleRadius / 6),
            _24HourSelectionCircle = new Circle(contentCircleRadius / 6);
        selectionCircle.fillProperty().bind(dateTimePicker.defaultColorProperty());
        _24HourSelectionCircle.fillProperty().bind(dateTimePicker.defaultColorProperty());

        double shift = 9, _24HourShift = 27.5;
        Line line = new Line(shift, 0, contentCircleRadius, 0);
        line.fillProperty().bind(dateTimePicker.defaultColorProperty());
        line.strokeProperty().bind(line.fillProperty());
        line.setStrokeWidth(1.5);
        hoursPointer.getChildren().addAll(line, selectionCircle);
        StackPane.setAlignment(selectionCircle, Pos.CENTER_LEFT);

        Group pointerGroup = new Group();
        pointerGroup.getChildren().add(hoursPointer);
        pointerGroup.setTranslateX((-contentCircleRadius + shift) / 2);
        hoursPointerRotate = new Rotate(0, contentCircleRadius - shift, selectionCircle.getRadius());
        pointerRotate.set(hoursPointerRotate);
        pointerGroup.getTransforms().add(hoursPointerRotate);
        pointerGroup.setVisible(!is24HourView || (time.getHour() > 0 && time.getHour() < 13));

        Line _24HourLine = new Line(shift + _24HourShift, 0, contentCircleRadius, 0);
        _24HourLine.fillProperty().bind(dateTimePicker.defaultColorProperty());
        _24HourLine.strokeProperty().bind(_24HourLine.fillProperty());
        _24HourLine.setStrokeWidth(1.5);
        _24HoursPointer.getChildren().addAll(_24HourLine, _24HourSelectionCircle);
        StackPane.setAlignment(_24HourSelectionCircle, Pos.CENTER_LEFT);

        Group pointer24HourGroup = new Group();
        pointer24HourGroup.getChildren().add(_24HoursPointer);
        pointer24HourGroup.setTranslateX(((-contentCircleRadius + shift) / 2) + (_24HourShift / 2));
        _24HourHoursPointerRotate = new Rotate(0, contentCircleRadius - shift - _24HourShift, selectionCircle.getRadius());
        _24HourPointerRotate.set(_24HourHoursPointerRotate);
        pointer24HourGroup.getTransforms().add(_24HourHoursPointerRotate);
        pointer24HourGroup.setVisible(is24HourView && (time.getHour() == 0 || time.getHour() > 12));

        // changed container to Group that is able to place content at center
        Group clockLabelsContainer = new Group();
        // inner circle radius
        double radius = contentCircleRadius - shift - selectionCircle.getRadius();
        for (int i = 0; i < 12; i++) {
            // create the label and its container
            int val = (i + 3) % 12 == 0 ? 12 : (i + 3) % 12;
            Label label = new Label(Integer.toString(val));
            label.setFont(Font.font(ROBOTO, FontWeight.BOLD, 12));

            // init color
            label.setTextFill(((val == time.getHour() % 12 || (val == 12 && time.getHour() % 12 == 0)) && !is24HourView) ?
                Color.rgb(255, 255, 255, 0.87) : Color.rgb(0, 0, 0, 0.87));
            selectedHourLabel.textProperty().addListener((o, oldVal, newVal) -> {
                if (Integer.parseInt(newVal) == Integer.parseInt(label.getText())) {
                    label.setTextFill(Color.rgb(255, 255, 255, 0.87));
                } else {
                    label.setTextFill(Color.rgb(0, 0, 0, 0.87));
                }
            });

            // create label container
            StackPane labelContainer = new StackPane();
            labelContainer.getChildren().add(label);
            double labelSize = (selectionCircle.getRadius() / Math.sqrt(2)) * 2;
            labelContainer.setMinSize(labelSize, labelSize);

            // position the label on the circle
            double angle = 2 * i * Math.PI / 12;
            double xOffset = radius * Math.cos(angle);
            double yOffset = radius * Math.sin(angle);
            final double startx = contentCircleRadius + xOffset;
            final double starty = contentCircleRadius + yOffset;
            labelContainer.setLayoutX(startx - labelContainer.getMinWidth() / 2);
            labelContainer.setLayoutY(starty - labelContainer.getMinHeight() / 2);

            // add label to the parent node
            clockLabelsContainer.getChildren().add(labelContainer);

            // init pointer angle
            if (!is24HourView && (val == time.getHour() % 12 || (val == 12 && time.getHour() % 12 == 0))) {
                hoursPointerRotate.setAngle(180 + Math.toDegrees(angle));
            }
        }

        if (_24HourView) {
            radius /= 1.6;
            for (int i = 0; i < 12; i++) {
                // create the label and its container
                int val = (i + 3) % 12 == 0 ? 12 : (i + 3) % 12;
                val += (val == 12 ? -12 : 12);
                Label label = new Label(Integer.toString(val) + (val == 0 ? "0" : ""));
                label.setFont(Font.font(ROBOTO, FontWeight.NORMAL, 10));

                // init color
                label.setTextFill((val == time.getHour() % 24 || (val == 0 && time.getHour() % 24 == 0) && is24HourView) ?
                    Color.rgb(255, 255, 255, 0.54) : Color.rgb(0, 0, 0, 0.54));
                selectedHourLabel.textProperty().addListener((o, oldVal, newVal) -> {
                    if (Integer.parseInt(newVal) == Integer.parseInt(label.getText())) {
                        label.setTextFill(Color.rgb(255, 255, 255, 0.54));
                    } else {
                        label.setTextFill(Color.rgb(0, 0, 0, 0.54));
                    }
                });

                // create label container
                StackPane labelContainer = new StackPane();
                labelContainer.getChildren().add(label);
                double labelSize = (selectionCircle.getRadius() / Math.sqrt(2)) * 2;
                labelContainer.setMinSize(labelSize, labelSize);

                // position the label on the circle
                double angle = 2 * i * Math.PI / 12;
                double xOffset = radius * Math.cos(angle);
                double yOffset = radius * Math.sin(angle);
                final double startx = contentCircleRadius + xOffset;
                final double starty = contentCircleRadius + yOffset;
                labelContainer.setLayoutX(startx - labelContainer.getMinWidth() / 2);
                labelContainer.setLayoutY(starty - labelContainer.getMinHeight() / 2);

                // add label to the parent node
                clockLabelsContainer.getChildren().add(labelContainer);

                // init pointer angle
                if (val == time.getHour() % 24 || (val == 24 && time.getHour() % 24 == 0)) {
                    _24HourHoursPointerRotate.setAngle(180 + Math.toDegrees(angle));
                }
            }
        }

        if (_24HourView) {
            return new StackPane(pointerGroup, pointer24HourGroup, clockLabelsContainer);
        } else {
            return new StackPane(pointerGroup, clockLabelsContainer);
        }
    }

    private StackPane createMinutesContent(LocalTime time) {
        // create minutes content
        StackPane minsPointer = new StackPane();
        Circle selectionCircle = new Circle(contentCircleRadius / 6);
        selectionCircle.fillProperty().bind(dateTimePicker.defaultColorProperty());

        Circle minCircle = new Circle(selectionCircle.getRadius() / 8);
        minCircle.setFill(Color.rgb(255, 255, 255, 0.87));
        minCircle.setTranslateX(selectionCircle.getRadius() - minCircle.getRadius());
        minCircle.setVisible(time.getMinute() % 5 != 0);
        selectedMinLabel.textProperty().addListener((o, oldVal, newVal) -> {
            if (Integer.parseInt(newVal) % 5 == 0) {
                minCircle.setVisible(false);
            } else {
                minCircle.setVisible(true);
            }
        });

        double shift = 9;
        Line line = new Line(shift, 0, contentCircleRadius, 0);
        line.fillProperty().bind(dateTimePicker.defaultColorProperty());
        line.strokeProperty().bind(line.fillProperty());
        line.setStrokeWidth(1.5);
        minsPointer.getChildren().addAll(line, selectionCircle, minCircle);
        StackPane.setAlignment(selectionCircle, Pos.CENTER_LEFT);
        StackPane.setAlignment(minCircle, Pos.CENTER_LEFT);

        Group pointerGroup = new Group();
        pointerGroup.getChildren().add(minsPointer);
        pointerGroup.setTranslateX((-contentCircleRadius + shift) / 2);
        minsPointerRotate = new Rotate(0, contentCircleRadius - shift, selectionCircle.getRadius());
        pointerGroup.getTransforms().add(minsPointerRotate);

        // changed container to Group that is able to place content at center
        Group clockLabelsContainer = new Group();
        // inner circle radius
        double radius = contentCircleRadius - shift - selectionCircle.getRadius();
        for (int i = 0; i < 12; i++) {
            StackPane labelContainer = new StackPane();
            int val = ((i + 3) * 5) % 60;
            Label label = new Label(String.valueOf(unitConverter.toString(val)));
            label.setFont(Font.font(ROBOTO, FontWeight.BOLD, 12));
            // init label color
            label.setTextFill(val == time.getMinute() ?
                Color.rgb(255, 255, 255, 0.87) : Color.rgb(0, 0, 0, 0.87));
            selectedMinLabel.textProperty().addListener((o, oldVal, newVal) -> {
                if (Integer.parseInt(newVal) == Integer.parseInt(label.getText())) {
                    label.setTextFill(Color.rgb(255, 255, 255, 0.87));
                } else {
                    label.setTextFill(Color.rgb(0, 0, 0, 0.87));
                }
            });

            labelContainer.getChildren().add(label);
            double labelSize = (selectionCircle.getRadius() / Math.sqrt(2)) * 2;
            labelContainer.setMinSize(labelSize, labelSize);

            double angle = 2 * i * Math.PI / 12;
            double xOffset = radius * Math.cos(angle);
            double yOffset = radius * Math.sin(angle);
            final double startx = contentCircleRadius + xOffset;
            final double starty = contentCircleRadius + yOffset;
            labelContainer.setLayoutX(startx - labelContainer.getMinWidth() / 2);
            labelContainer.setLayoutY(starty - labelContainer.getMinHeight() / 2);

            // add label to the parent node
            clockLabelsContainer.getChildren().add(labelContainer);
        }

        minsPointerRotate.setAngle(180 + (time.getMinute() + 45) % 60 * Math.toDegrees(2 * Math.PI / 60));

        return new StackPane(pointerGroup, clockLabelsContainer);
    }

    ObjectProperty<LocalTime> displayedTimeProperty() {
        return selectedTime;
    }

    private void swapLabelsColor(Label lbl1, Label lbl2) {
        Paint color = lbl1.getTextFill();
        lbl1.setTextFill(lbl2.getTextFill());
        lbl2.setTextFill(color);
    }

    private void switchTimeUnit(TimeUnit newVal) {
        if (newVal == TimeUnit.HOURS) {
            Timeline fadeout = new Timeline(new KeyFrame(Duration.millis(320),
                new KeyValue(minutesContent.opacityProperty(),
                    0,
                    Interpolator.EASE_BOTH)));
            Timeline fadein = new Timeline(new KeyFrame(Duration.millis(320),
                new KeyValue(hoursContent.opacityProperty(),
                    1,
                    Interpolator.EASE_BOTH)));
            new ParallelTransition(fadeout, fadein).play();
        } else {
            Timeline fadeout = new Timeline(new KeyFrame(Duration.millis(320),
                new KeyValue(hoursContent.opacityProperty(),
                    0,
                    Interpolator.EASE_BOTH)));
            Timeline fadein = new Timeline(new KeyFrame(Duration.millis(320),
                new KeyValue(minutesContent.opacityProperty(),
                    1,
                    Interpolator.EASE_BOTH)));
            new ParallelTransition(fadeout, fadein).play();
        }
    }

    void updateValue() {
        LocalTime localTime;
        LocalDate localDate = this.dateTimePicker.getValue() == null ?
            LocalDate.now() : this.dateTimePicker.getValue().toLocalDate();
        
        if (is24HourView) {
            LocalTimeStringConverter localTimeStringConverter =
                new LocalTimeStringConverter(FormatStyle.SHORT, Locale.GERMAN);
            localTime = localTimeStringConverter.fromString(selectedHourLabel.getText() + ":" + selectedMinLabel.getText());
        } else {
            localTime = LocalTime.parse(selectedHourLabel.getText() + ":" + selectedMinLabel.getText() + " " + period.get(), DateTimeFormatter.ofPattern("h:mm a").withLocale(Locale.ENGLISH));
        }
        dateTimePicker.setValue(LocalDateTime.of(localDate, localTime));
    }

    private void goToTime(LocalDateTime date) {
        if (date != null) {
            LocalTime time = date.toLocalTime();
            int hour = time.getHour();
            selectedHourLabel.setText(Integer.toString(hour % (is24HourView ? 24 : 12) == 0 ?
                (is24HourView ? 0 : 12) : hour % (is24HourView ? 24 : 12)));
            selectedMinLabel.setText(unitConverter.toString(time.getMinute()));
            if (!is24HourView) {
                period.set(hour < 12 ? "AM" : "PM");
            }
            minsPointerRotate.setAngle(180 + (time.getMinute() + 45) % 60 * Math.toDegrees(2 * Math.PI / 60));
            hoursPointerRotate.setAngle(180 + Math.toDegrees(2 * (hour - 3) * Math.PI / 12));
            _24HourHoursPointerRotate.setAngle(180 + Math.toDegrees(2 * (hour - 3) * Math.PI / 12));
        }
    }
    
    /***************************************************************************
     * <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< *
     * Time Picker Content                                                     *
     *                                                                         *
     **************************************************************************/
}
