//
// TB TODO - Does not work for postDelayed it seems... They do not make it to handleMessage...
// 
package com.bitcoinvideocasino.app;

import android.os.Handler;
import java.util.Stack;
import android.os.Message;
import android.util.Log;

public class PausableHandler extends Handler {
    private Stack<Message> mMessageStack = new Stack<Message>();
    private boolean mIsPaused = false;
    
    public synchronized void pause()
    {
        mIsPaused = true;
    }   

    public synchronized void resume()
    {
        mIsPaused = false;
        while (! mMessageStack.empty())
        {
            sendMessageAtFrontOfQueue(mMessageStack.pop());
        }   
    }   

    @Override
    public void handleMessage(Message msg)
    {
    	
        if (mIsPaused)
        {
        	Log.v("handleMessage", "Delaying message...");
            mMessageStack.push(Message.obtain(msg));
            return;
        }

        // otherwise handle message as normal
        // ...
    	Log.v("handleMessage", "Gonna handle this message!");
        super.handleMessage(msg);
    }

}
