package GrooveServer.core.components;

import java.util.Timer;
import java.util.TimerTask;

import GrooveServer.core.CoreServer;
 
class CallResetAllKeys extends TimerTask {

    public static int i = 1;

    private CoreServer _coreServer;

    public CallResetAllKeys(CoreServer coreServer){
        _coreServer = coreServer;
    }
     
    // TimerTask.run() method will be used to perform the action of the task
     
    public void run() {
        _coreServer.ResetAllTemporaryKeys();
    }
}
 
public class SubscriptionTimer {

    public static void StartTimer(int secondsToResetKeys, CoreServer coreServer){
        
        Timer timer = new Timer();
        
        
        // Helper class extends TimerTask
        TimerTask task = new CallResetAllKeys(coreServer);
        
        /*
        *  Schedule() method calls for timer class.
        *  void schedule(TimerTask task, Date firstTime, long period)
        */
        
        timer.schedule(task, 0, secondsToResetKeys * 1000);
        
    } 
}