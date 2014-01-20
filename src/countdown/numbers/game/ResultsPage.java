package countdown.numbers.game;

import android.app.Activity;
import android.content.Intent;

import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class ResultsPage extends Activity {
	public final static String resultStrID = "countdown.numbers.game.resultStr";

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    // Get the message from the intent
	    Intent intent  = getIntent();
	    String message = intent.getStringExtra(resultStrID);
	    
        setContentView(R.layout.results_page);
  	    TextView textView = (TextView) findViewById(R.id.result_text);
	    textView.setText(message);
 	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.results_page, menu);
        return true;
    }
		
    /** Called when the user clicks the Return to main button */
    public void returnToMain(View view) {
    	// The pending page should already be closed by now, but we send a message to it to close
    	// just in case! Previously did find that it was occasionally still open,
    	// particularly when the solution had been found very quickly.
        Intent intentFinishPendingPage = new Intent("finish_pending_page");
        LocalBroadcastManager.getInstance(view.getContext()).sendBroadcast(intentFinishPendingPage);

        // now we close the results page.
        finish();
        
        // When the pending page and results page are closed, 
        // the only page left open is the main page and so that is what the user will see.
    }    
}
    