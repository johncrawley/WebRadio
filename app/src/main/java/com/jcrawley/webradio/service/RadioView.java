package com.jcrawley.webradio.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public interface RadioView {

   void selectPreviousStation();

   void  selectNextStation();
   void updateStatusViewOnStop();

   void  updateStatusViewOnConnecting();

    void updateStatusViewOnPlaying();

   void updateStatusViewOnError();

}
