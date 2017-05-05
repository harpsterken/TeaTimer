package biz.bitinc.android.teatimer;

class Constants {
	
	// Define Application Constants
    static final String INTENT_ACTION_TEA_DETAIL = "biz.bitinc.android.teatimer.TEA_DETAIL";
    static final String PREFS_NAME = "biz.bitinc.android.teatimer.PREFERENCE_FILE_KEY";

    // Define Database and Table Constants
    static final String DATABASE_NAME = "teatimer.db";
    static final String DATABASE_TABLE = "teas";
    static final int DATABASE_VERSION = 1;
    
    // Define Table (Teas) Constants 
    static final String TEAS_ROWID = "_id";
    static final String TEAS_TYPE = "Type";
    static final String TEAS_DESCRIPTION = "Description";
    static final String TEAS_STEEPTEMP = "SteepTemp";
    static final String TEAS_STEEPTIME = "SteepTime";
    static final String TEAS_ALARMSOUND = "AlarmSound";
}
