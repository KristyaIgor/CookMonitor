package edi.md.cookmonitor;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Tony on 2017/12/3.
 */

public class BaseEnum {
    public static final int CookMonitor = 202, OrderMonitor = 101, NONE_SELECTED_MODE = 0;

    @IntDef({CookMonitor, OrderMonitor, NONE_SELECTED_MODE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface fiscalCommands {
    }

}
