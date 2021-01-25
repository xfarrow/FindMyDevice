package de.nulide.findmydevice.utils;

import java.util.TimerTask;

import de.nulide.findmydevice.receiver.SMSReceiver;

public class ExpiredTempWhiteListedTask extends TimerTask {

    private String tempContact;
    private SMSReceiver smsrec;

    public ExpiredTempWhiteListedTask(String tempContact, SMSReceiver smsrec) {
        this.tempContact = tempContact;
        this.smsrec = smsrec;
    }


    @Override
    public void run() {
        SMS.sendMessage(tempContact, "FMD: Access expired");
        smsrec.removeTemp();
    }
}
