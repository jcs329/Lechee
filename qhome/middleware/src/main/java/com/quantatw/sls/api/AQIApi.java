package com.quantatw.sls.api;

import java.util.ArrayList;

/**
 * Created by 95011613 on 2016/06/06.
 */
public class AQIApi {

    //reference url : http://taqm.epa.gov.tw/taqm/tw/fpmi-2.aspx
    public enum AQI_CATEGORY {
        GOOD, NORMAL, DANGER
    }

    public enum PM25_LEVEL {
        LEVEL_1(0,11,AQI_CATEGORY.GOOD),
        LEVEL_2(12,23,AQI_CATEGORY.GOOD),
        LEVEL_3(24,35,AQI_CATEGORY.GOOD),
        LEVEL_4(36,41,AQI_CATEGORY.NORMAL),
        LEVEL_5(42,47,AQI_CATEGORY.NORMAL),
        LEVEL_6(48,53,AQI_CATEGORY.NORMAL),
        LEVEL_7(54,58,AQI_CATEGORY.DANGER),
        LEVEL_8(59,64,AQI_CATEGORY.DANGER),
        LEVEL_9(65,70,AQI_CATEGORY.DANGER),
        LEVEL_10(71,500,AQI_CATEGORY.DANGER);

        private int low_value;
        private int high_value;
        private AQI_CATEGORY category;

        PM25_LEVEL(int low_value, int high_value,AQI_CATEGORY category) {
            this.low_value = low_value;
            this.high_value = high_value;
            this.category = category;
        }

        public int getLowValue() { return this.low_value; }
        public int getHighValue() { return this.high_value; }
        public AQI_CATEGORY getCategory() { return this.category; }

        protected static ArrayList<PM25_LEVEL> getLevelByCategory(AQI_CATEGORY category){
            ArrayList<PM25_LEVEL> pm25_level_list = new ArrayList<PM25_LEVEL>();
            for (PM25_LEVEL level: PM25_LEVEL.values()){
                if(level.getCategory() == category)
                    pm25_level_list.add(level);
            }
            return pm25_level_list;
        }
    }

    public static AQI_CATEGORY getAQICategoryByPM25Value(int value){
        for (PM25_LEVEL level: PM25_LEVEL.values()){
            if((value >= level.getLowValue()) && (value <= level.getHighValue()))
                return level.getCategory();
        }
        return AQI_CATEGORY.DANGER;
    }

    public static int[] getDangerRange(){
        ArrayList<PM25_LEVEL> pm25_level_list=PM25_LEVEL.getLevelByCategory(AQI_CATEGORY.DANGER);

        return new int[]{pm25_level_list.get(0).getLowValue(),pm25_level_list.get(pm25_level_list.size()-1).getHighValue()};
    }
}
