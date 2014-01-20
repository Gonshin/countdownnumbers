package countdown.numbers.game;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class PendingPage extends Activity {
	public final static String pendingStatusID = "countdown.numbers.game.pendingStatus";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
        setContentView(R.layout.pending_page);

        updateStatus(getIntent().getStringExtra(pendingStatusID));
        
        LocalBroadcastManager.getInstance(this).registerReceiver(updateReceiver,
        	      new IntentFilter("progress_update"));
        
        LocalBroadcastManager.getInstance(this).registerReceiver(finishMessageReceiver,
      	      new IntentFilter("finish_pending_page"));
    }
	
	private BroadcastReceiver finishMessageReceiver = new BroadcastReceiver() {
		  @Override
		  public void onReceive(Context context, Intent intent) {
            finish();			
		    Log.d("received", "finish pending page");
		  }
		  
		};

	
	private BroadcastReceiver updateReceiver = new BroadcastReceiver() {
	  @Override
	  public void onReceive(Context context, Intent intent) {
		updateStatus(intent.getStringExtra(pendingStatusID));  
		
	    Log.d("received", intent.getStringExtra(pendingStatusID));
	  }
	  
	};

	
	public void updateStatus(String status){
  	    TextView textView = (TextView) findViewById(R.id.pending_status_box);
	    textView.setText(status);
	}
	
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.pending_page, menu);
        return true;
    }
		
    /** Called when the user clicks the Return to main button */
    public void cancelSearch(View view) {
    	// I wonder are there any circumstances in which 
    	// going back won't return the user to the main page?
    	
    	PK_Utils.getSolverInBG(this).cancel(true);
    	super.onBackPressed();
    }   
    
    
}
    