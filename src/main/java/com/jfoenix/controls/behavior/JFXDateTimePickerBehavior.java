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

package com.jfoenix.controls.behavior;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.jfoenix.controls.JFXDateTimePicker;
import com.jfoenix.skins.JFXDateTimePickerSkin;
import com.sun.javafx.scene.control.behavior.ComboBoxBaseBehavior;
import com.sun.javafx.scene.control.behavior.KeyBinding;

/**
 * JFXDateTimePickerBehavior is the just copy of {@link JFXDatePickerBehavior}
 * with changed data type to {@link LocalDateTime} and variables names.
 * 
 * @author Roman Gorovoy
 * @version 1.0
 * @since 2019-04-01
 */
public class JFXDateTimePickerBehavior extends ComboBoxBaseBehavior<LocalDateTime> {

    /***************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/

    public JFXDateTimePickerBehavior(final JFXDateTimePicker dateTimePicker) {
        super(dateTimePicker, JFX_DATETIME_PICKER_BINDINGS);
    }

    /***************************************************************************
     *                                                                         *
     * Key event handling                                                      *
     *                                                                         *
     **************************************************************************/

    protected static final List<KeyBinding> JFX_DATETIME_PICKER_BINDINGS = new ArrayList<>();

    static {
        JFX_DATETIME_PICKER_BINDINGS.addAll(COMBO_BOX_BASE_BINDINGS);
    }

    /**************************************************************************
     *                                                                        *
     * Mouse Events handling (when losing focus)                              *
     *                                                                        *
     *************************************************************************/

    @Override
    public void onAutoHide() {
        JFXDateTimePicker dateTimePicker = (JFXDateTimePicker) getControl();
        JFXDateTimePickerSkin cpSkin = (JFXDateTimePickerSkin) dateTimePicker.getSkin();
        cpSkin.syncWithAutoUpdate();
        if (!dateTimePicker.isShowing()) {
            super.onAutoHide();
        }
    }
}
