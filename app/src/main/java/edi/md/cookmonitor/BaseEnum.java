package edi.md.cookmonitor;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Tony on 2017/12/3.
 */

public class BaseEnum {
    public static final int CookMonitor = 202, OrderMonitor = 101, NONE_SELECTED_MODE = 0;
    public static final int OneMode = 45, DoubleMode = 46, NoneMode = 44;

    @IntDef({CookMonitor, OrderMonitor, NONE_SELECTED_MODE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface fiscalCommands {
    }

    @IntDef({OneMode, DoubleMode, NoneMode})
    @Retention(RetentionPolicy.SOURCE)
    public @interface modes {
    }

}
