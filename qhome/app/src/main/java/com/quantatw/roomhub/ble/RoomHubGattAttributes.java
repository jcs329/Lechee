/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.quantatw.roomhub.ble;

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class RoomHubGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();

    public static String QCI_SERVICE = "01681590-BBA3-F393-E0A9-E50E24DC0168";
    public static String CHARACTERISTIC_WRITE = "01681591-BBA3-F393-E0A9-E50E24DC0168";
    public static String CHARACTERISTIC_NOTIFY = "01681592-BBA3-F393-E0A9-E50E24DC0168";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";


    public static String[] QCI_CHARACTERISTIC = {
            CHARACTERISTIC_WRITE,
            CHARACTERISTIC_NOTIFY
    };

    static {
        attributes.put(QCI_SERVICE, "QCI Service");

        attributes.put(CHARACTERISTIC_WRITE, "Write characteristic");
        attributes.put(CHARACTERISTIC_NOTIFY, "Notify characteristic");

    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
