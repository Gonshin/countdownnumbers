
package countdown.numbers.game;


import java.text.DecimalFormat;
import static java.lang.Math.*;
// import static java.lang.Math.*; 

import android.content.Context;
import android.util.Log;

public class PK_Utils {
	 
	  static private SolverInBG m_SolverInBG;
	
	  public static SolverInBG getSolverInBG(Context context) { 
		  m_SolverInBG.setContext(context);
		  return m_SolverInBG; };
	  
      public static SolverInBG getNewSolverInBG(Context context) {
    	  
    	  if( m_SolverInBG != null) {
    		  m_SolverInBG.cancel(false);
    		  Log.d("getNewSolverInBG", "Found old solver running and have killed it.");
    	  }
    	  m_SolverInBG = new SolverInBG();
    	  m_SolverInBG.setContext(context);
    	  return m_SolverInBG;     	  
      }
		  
	  public static void Assert( boolean allIsWell, String message) throws Exception
	  {
		 if(!allIsWell) {
			 
		   Log.d("PK Error", "Found a problem: " + message);
		   throw new Exception(message);
		 }
	  } 

	
    //for integers we remove the decimal places
    public static String doubleToString(double d)
    {
       if ( d == floor(d)) {// i.e. d is an integer
          DecimalFormat fmt = new DecimalFormat("#,###");
          return ( fmt.format(d));      
       } else
          return new String(""+d);
    }

     public static boolean isNumeric(String str)  {  
        try  {  
            Double.parseDouble(str);  
        } catch(NumberFormatException nfe) {  
            return false;  
        }  
        return true;  
     }  
	
	
} // end of class PK_Utils