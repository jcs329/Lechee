package com.quantatw.roomhub.manager;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by 95011613 on 2015/9/25.
 */
public class RoomHubDB {
    public static final String AUTHORITY = "roomhub";

    public static final class Account implements BaseColumns {

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI =
                Uri.parse("content://" + AUTHORITY + "/accounts");

        /**
         * The row ID.
         * <p>Type: INTEGER</p>
         */
        public static final String ID = "_id";

        /**
         * <p/>
         * Type: TEXT
         * </P>
         */
        public static final String ACCOUNT = "account";

        /**
         * <p/>
         * Type: TEXT
         * </P>
         */
        public static final String NAME = "name";

        public static final String DEFAULT_ACCOUNT="guest";
    }

    public static final class Profile implements BaseColumns {

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI =
                Uri.parse("content://" + AUTHORITY + "/profiles");

        /**
         * The row ID.
         * <p>Type: INTEGER</p>
         */
        public static final String ID = "_id";

        /**
         * <p/>
         * Type: INTEGER
         * </P>
         */
        public static final String ACCOUNT_ID = "account_id";

        /**
         * <p/>
         * Type: TEXT
         * </P>
         */
        public static final String UUID = "uuid";

        /**
         * <p/>
         * Type: INTEGER
         * </P>
         */
        public static final String FUN_MODE = "function_mode";

        /**
         * <p/>
         * Type: INTEGER
         * </P>
         */
        public static final String TEMP = "temperature";

        /**
         * <p/>
         * Type: INTEGER
         * </P>
         */
        public static final String SWING = "swing";

        /**
         * <p/>
         * Type: INTEGER
         * </P>
         */
        public static final String FAN = "fan";

        public static final String SELECTED = "selected";
    }

    public static final class Notice_Setting implements BaseColumns {


        /**
         * The row ID.
         * <p>Type: INTEGER</p>
         */
        public static final String ID = "_id";

        /**
         * <p/>
         * Type: TEXT
         * </P>
         */
        public static final String UUID = "uuid";

        /**
         * <p/>
         * Type: INTEGER
         * </P>
         */
        public static final String SWITCH_ON_OFF = "switch_on_off";

        /**
         * <p/>
         * Type: INTEGER
         * </P>
         */
        public static final String TIME = "time";

        /**
         * <p/>
         * Type: INTEGER
         * </P>
         */
        public static final String DELTA = "delta";
        /**
         * <p/>
         * Type: INTEGER
         * </P>
         */
        public static final String IS_DEFAULT_TIME = "isDeaultTime";
        /**
         * <p/>
         * Type: INTEGER
         * </P>
         */
        public static final String IS_DEFAULT_DELTA = "isDeaultDelta";
    }

    public static final class Blood_Pressure implements BaseColumns {


        /**
         * The row ID.
         * <p>Type: INTEGER</p>
         */
        public static final String ID = "_id";

        /**
         * <p/>
         * Type: TEXT
         * </P>
         */
        public static final String UUID = "uuid";

        /**
         * <p/>
         * Type: INTEGER
         * </P>
         */
        public static final String MEASURE_TIME = "measure_time";

        /**
         * <p/>
         * Type: INTEGER
         * </P>
         */
        public static final String MAX_BLOOD_PRESSURE = "max_blood_pressure";

        /**
         * <p/>
         * Type: INTEGER
         * </P>
         */
        public static final String MIN_BLOOD_PRESSURE = "min_blood_pressure";

        /**
         * <p/>
         * Type: INTEGER
         * </P>
         */
        public static final String HEART_RATE = "heart_rate";
    }

    public static final class ACToggle implements BaseColumns {


        /**
         * The row ID.
         * <p>Type: INTEGER</p>
         */
        public static final String ID = "_id";

        /**
         * <p/>
         * Type: TEXT
         * </P>
         */
        public static final String UUID = "uuid";

        /**
         * <p/>
         * Type: INTEGER
         * </P>
         */
        public static final String BRAND_ID = "brand_id";

        /**
         * <p/>
         * Type: TEXT
         * </P>
         */
        public static final String MODEL_ID = "model_id";
    }
}
