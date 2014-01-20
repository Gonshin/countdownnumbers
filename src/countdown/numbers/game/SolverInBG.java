// For running the solver in the back-ground, don't want it run in the main thread

package countdown.numbers.game;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

 public class SolverInBG extends AsyncTask<Double, String, String> {
  
	 private Context m_context;
	 	 
	 public void setContext(Context context) {
		 m_context = context;
	 }
     protected String doInBackground(Double... d) {
    	 
    	 double target   = d[0];
    	 double inputs[] = new double[6];
    	 
    	 for(int i = 0; i < 6; i++){
    	     inputs[i] = d[i+1];	 
    	 }

    	 String message = "About to call solver";
         CountdownSolver solver = new CountdownSolver();
         try { 
             message = solver.runSolver(inputs, target, this);
         	     		
      	 } catch (Exception e){
     		Log.d("Exception", "tried, threw and caught.");
     		Log.d("Exception", e.getMessage());
     	 }    	    	
    	 return (message);   
     }

     public void reportProgress(String progress){
    	 publishProgress(progress); // this will call onProgressUpdate(..)
     }
     
     protected void onProgressUpdate(String... progressStr) {
         Log.d("onProgressUpdate", "made some progress" + progressStr[0]);
         
         Intent intent = new Intent("progress_update");
         intent.putExtra(PendingPage.pendingStatusID, progressStr[0]);
         
         LocalBroadcastManager.getInstance(m_context).sendBroadcast(intent);
     }

     protected void onPostExecute(String result) {
         Log.d("onPostEx", result);
         Intent intentStartResultPage = new Intent(m_context, ResultsPage.class);
         intentStartResultPage.putExtra(ResultsPage.resultStrID, result);
         m_context.startActivity(intentStartResultPage);
         
         Intent intentFinishPendingPage = new Intent("finish_pending_page");
         LocalBroadcastManager.getInstance(m_context).sendBroadcast(intentFinishPendingPage);
     }
 }