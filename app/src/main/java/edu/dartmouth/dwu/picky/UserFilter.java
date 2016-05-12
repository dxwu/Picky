package edu.dartmouth.dwu.picky;

import java.util.ArrayList;
import android.provider.MediaStore;
import android.bluetooth.BluetoothAdapter;

/**
 * Created by dwu on 5/10/16.
 */
public class UserFilter {

    public static final int LEVEL_NO_BT_DEFAULT = 67;
    public static final int LEVEL_WITH_BT_DEFAULT = 69;

    public int levelNoBt;
    public int levelWithBt;

    public boolean blockCamera;
    public boolean blockBtStatus;
    public boolean blockBatteryLevel;
    public boolean blockRecordAudio;
}
