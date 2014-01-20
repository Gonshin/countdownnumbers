package countdown.numbers.game;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

public class MainPage extends Activity {
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_page);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_page, menu);
        return true;
    }
    
    /** Called when the clear inputs button is clicked **/
    public void clearInputs(View view){
       	EditText editText;

       	editText = (EditText) findViewById(R.id.target);
       	editText.setText("");
       	
       	String inputIDStr;
       	int    inputIDInt;
        for(int i = 1 ; i <= 6; i++){
           	inputIDStr  = "input_" + Integer.toString(i);
           	inputIDInt  = getResources().getIdentifier(inputIDStr, "id", "countdown.numbers.game");
            
           	editText    = (EditText) findViewById(inputIDInt);
            editText.setText("");
        }
    }
    
    /** Called when the user clicks the Send button */
    public void solveCountdown(View view) {
    	
    	double[] inputs;
    	
    	Log.d("PK", "About to call getInputs()");

  	   	inputs = getInputs();
  	   	
       	EditText editText    = (EditText) findViewById(R.id.target);
       	double target;
       	if( editText.getText().toString().length() == 0)
       		target = 0; // an empty string is assumed to be zero
       	else
         	target = Double.parseDouble(editText.getText().toString());
       	
        Log.d("PK Msg", "The target we seek is " + Double.toString(target));
    	
        // we now run the solver in the back-ground, since it can be a tad slow
        SolverInBG solverInBG = PK_Utils.getNewSolverInBG(this);
        
    	String message = "\nJust started...\n\n\n\n";
        //message = solver.runSolver(inputs, target);
    	
    	// Close any pending pages that are currently open
        Intent intentFinishOldPendingPage = new Intent("finish_pending_page");
        LocalBroadcastManager.getInstance(view.getContext()).sendBroadcast(intentFinishOldPendingPage);

        Intent intentStartNewPendingPage = new Intent(this, PendingPage.class);
        intentStartNewPendingPage.putExtra(PendingPage.pendingStatusID, message);
        startActivity(intentStartNewPendingPage);

        solverInBG.execute(target, inputs[0], inputs[1], inputs[2], inputs[3], inputs[4], inputs[5]);    
    }    
    
    public double[] getInputs(){
        double[] inputs = new double[6];
        
        String    inputIDStr;
        int       inputIDInt;

        String    inputStr;
        EditText  editText;
        for(int i = 1 ; i <= 6; i++){
           	inputIDStr  = "input_" + Integer.toString(i);
           	inputIDInt  = getResources().getIdentifier(inputIDStr, "id", "countdown.numbers.game");
           	editText    = (EditText) findViewById(inputIDInt);
            inputStr    = editText.getText().toString();
            if (inputStr.length() == 0) 
            	inputs[i-1] = 0; // blank input strings are treated as zeros
            else
                inputs[i-1] = Double.parseDouble(inputStr);
        }        
        return inputs;
    }
}


