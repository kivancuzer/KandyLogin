package com.example.kandylogin;

import com.rbbn.cpaas.mobile.utilities.Configuration;
import com.rbbn.cpaas.mobile.utilities.logging.LogLevel;
import com.rbbn.cpaas.mobile.utilities.logging.Logger;

class CustomizedLogger implements Logger {
    @Override
    public void log(LogLevel loglevel, String tag, String message) {
        // a customized implementation
    }

    @Override
    public void log(LogLevel loglevel, String tag, String message, Exception ex) {
        // a customized implementation
    }
}

