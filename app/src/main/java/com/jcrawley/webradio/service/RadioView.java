package com.jcrawley.webradio.service;


public interface RadioView {

   void selectPreviousStation();

   void  selectNextStation();
   void updateStatusViewOnStop();

   void  updateStatusViewOnConnecting();

    void updateStatusViewOnPlaying();

   void updateStatusViewOnError();

}
