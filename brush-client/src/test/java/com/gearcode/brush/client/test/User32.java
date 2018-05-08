package com.gearcode.brush.client.test;

import com.sun.jna.Library;

/**
 * Created by jason on 2018/4/21.
 */
public interface User32 extends Library {
    boolean LockWorkStation();

    HANDLE GetProcessWindowStation();

    HANDLE OpenDesktopW(String lpszDesktop, int dwFlags, boolean fInherit, int dwDesiredAccess);

}
