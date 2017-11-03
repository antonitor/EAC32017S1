package cat.xtec.ioc.eac3_2017s1.Data;

import android.provider.BaseColumns;

/**
 * Created by Toni on 03/11/2017.
 */

public class MediaContract {
    public static final class MediaTable implements BaseColumns {
        public static final String TABLE_NAME = "media";
        public static final String COLUMN_FILE_NAME = "fileName";
        public static final String COLUMN_PATH = "filePath";
        public static final String COLUMN_IS_VIDEO = "isVideo";
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_LONGITUDE = "longitude";
    }
}