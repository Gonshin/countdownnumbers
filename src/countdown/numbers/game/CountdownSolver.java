
package countdown.numbers.game;

import static java.lang.Math.*; 
import android.util.Log;

////////////////////////////////////////////////////////////////
public class CountdownSolver{
     public enum Operator{ addition, division, multiplication, subtraction, no_operator, compound_op };
     
     public static final double  m_LARGE_NUMBER           = 9999999.99;
     public static final double  m_STEPS_BETWEEN_REPORTS  = 1000.0; // number of trial between reports
     public        SolverStatus  m_currentBest;  
     public              double  m_counter;
     public              double  m_target;
     public          SolverInBG  m_solverInBG;

     public String runSolver(double[] inputs, double target, SolverInBG solverInBG) throws Exception {
       m_solverInBG                       = solverInBG;
       m_counter                          = 0;
       m_target                           = target;
       SolverStatus        initialStatus  = new SolverStatus( inputs, target);
       
       if ( checkIfTargetTooBig(initialStatus, target))
    	   return "With the given inputs, the target (" + PK_Utils.doubleToString(target) + ") is too big.";
       else {
           m_currentBest                      = initialStatus;
           SolverStatus res                   = Solve(initialStatus);
           return res.getResultText();
       }
     }
  
     private boolean checkIfTargetTooBig(SolverStatus status, double target){
    	  double product = 1;
    	  for( int i = 0; i < status.getNumNodes(); i++) {
    		  // suppose we had inputs {1, 2, 10, 6, 5, 7}
    		  // then the biggest target we could reach  would be 
    		  // ( 1 + 2 ) * 10 * 6 * 5 * 7 = 1.5 * 2 * 10 * 6 * 5 * 7 
    		  // that gives a hint as to where the 1.5 in the following line comes from.
    		  product *= max( 1.5, abs(status.getArrayOfNodes()[i].getValue()));
    	  }
    	  return ( abs(target) > product);
     }
     
     public boolean isCancelled(){
    	 if( m_solverInBG.isCancelled() ){
    		 Log.d("CountdownSolver", "has been cancelled");
    	 }
    	 return (m_solverInBG.isCancelled());
     }
     
     public SolverStatus Solve(SolverStatus startingStatus) throws Exception
     {	  
      int numNodes = startingStatus.getNumNodes();
      for  ( int i = 0; i < (numNodes -1); i++) {
        for( int j = 1; j < numNodes;      j++) {        
           if ( !alreadyChecked(i, j, startingStatus)) {
        	
	          //Log.d("SolverStatus Solve", "have i:" + i + ", j: " + j);
	          if( i < j ) // addition and multiplication are commutative so we don't want repeats 
	          { 
	              // note that checkSolution will call the Solve(..) method
	              // so together Solve(..) and checkSolution(..) are recursive
	              // Apologies for the slightly convoluted logic.
	              
	              // check solution will set the value of m_currentBest
	              if (checkSolution(i, j, Operator.addition,       startingStatus)) 
	            	  return m_currentBest;
	              
	              // we only want to check multiplication if both i and j are not 1
	              if (     (startingStatus.getArrayOfNodes()[i].getValue() != 1.0) 
	            		&& (startingStatus.getArrayOfNodes()[j].getValue() != 1.0)
	                    && (checkSolution(i, j, Operator.multiplication, startingStatus))) 
	                    
	                	 return m_currentBest;
			   } 
	
	          if( i != j ) { // subtraction and division are non-commutative and so we need to try all pairs.
	              if (checkSolution(i, j, Operator.subtraction,    startingStatus)) 
	            	  return m_currentBest;
	
	              // we don't want to waste time dividing by 1
	              if (     (startingStatus.getArrayOfNodes()[j].getValue() != 1.0) 
	                    && (checkSolution(i, j, Operator.division, startingStatus))) 
	              
	                     return m_currentBest;
	          }   
           }   // if(!alreadyChecked(i, j, startingStatus)
        }      // next j
      }        // next i
      
      return m_currentBest; 
     }              

     private boolean alreadyChecked(int i, int j, SolverStatus status) {
    	 double val = status.getArrayOfNodes()[j].getValue();
    	 for( int k = 0; k < j; k++) {
    		  if (( k != i) && ( status.getArrayOfNodes()[k].getValue() == val))
    			  return true;
    	 }
    	 return false;
     }
          
     private boolean checkSolution(int leftIndex, int rightIndex, Operator op, SolverStatus previousStatus) throws Exception {
		 
    	 if(isCancelled()) return true;
    	 
    	 SolverStatus currentStatus = new SolverStatus( previousStatus, leftIndex, rightIndex, op);
		  if( currentStatus.getClosestDistance() < m_currentBest.getClosestDistance()) 
			 m_currentBest = currentStatus;
		     	  		  
		  if( currentStatus.getClosestDistance() == 0.0) // found a solution, so can stop
			 return true;
		     
          // when a solution is not found, we call Solve ( recursively )
	      SolverStatus nextStatus = Solve(currentStatus);   
		  
  	      return ( nextStatus.getClosestDistance() == 0.0);  
	  }
  
  /////////////////////////////////////////////////////////////
  private class SolverStatus  {
     private CalcNode[]  m_nodes;
     private int         m_iClosest;
     private double      m_closestDistance;
          
     public SolverStatus( double[] inputs, double target) throws Exception {
        CalcNode[] nodes = new CalcNode[inputs.length];
        for(int i = 0; i < inputs.length; i++)
           nodes[i] = new CalcNode(inputs[i]);
        initialize(nodes);
     }
     
     public SolverStatus(  SolverStatus parent, int leftIndex, int rightIndex, Operator op) throws Exception
     {
         PK_Utils.Assert ( leftIndex != rightIndex, "Can't have left and right indices the same.");
         
         CalcNode[] parentNodes = parent.getArrayOfNodes();
         
         CalcNode trialNode = new CalcNode( parentNodes[leftIndex], parentNodes[rightIndex], op);
         
         if ( !trialNode.isValid() ) {
        	 m_nodes =  new CalcNode[parent.getNumNodes()-2];
             m_iClosest                      = 0;
             m_closestDistance               = m_LARGE_NUMBER;       	 
         } else {
           	 m_nodes                         = new CalcNode[parent.getNumNodes()-1];
             m_nodes[parent.getNumNodes()-2] = trialNode;
             m_iClosest                      = parent.getNumNodes()-2;
             m_closestDistance               = m_nodes[parent.getNumNodes()-2].getDistance();       	 
         }
        	 
         int childIndex = 0;
         for( int parentIndex = 0; parentIndex < parent.getNumNodes(); parentIndex++)
         {
             if(( parentIndex != leftIndex ) && ( parentIndex != rightIndex)) 
             {
                 m_nodes[childIndex] = parentNodes[parentIndex];
                 
                 if ( parentNodes[parentIndex].getDistance() < m_closestDistance) {
                	 m_iClosest        = childIndex;
                	 m_closestDistance = parentNodes[parentIndex].getDistance();
                 }

                 childIndex++;
             }
         }
     }
     
     private void initialize(CalcNode[] nodes) throws Exception
     {
        m_nodes   = nodes;
        PK_Utils.Assert( m_nodes.length > 0, "nodes array can't be empty.");
        
        m_iClosest = 0;
        m_closestDistance = abs(m_target - m_nodes[0].getValue());
                
        for( int i = 1; i < m_nodes.length; i++) {
           double currentDistance = abs(m_target - m_nodes[i].getValue());
           if(  currentDistance < m_closestDistance ) {
               m_iClosest = i;
               m_closestDistance = currentDistance;
           }
        }      
     }
       
     public double       getClosestDistance() { return m_closestDistance;         }  
     public CalcNode[]   getArrayOfNodes()    { return m_nodes;                   }
     public int          getNumNodes()        { return m_nodes.length;            }
   
     public String getIntermediateReport(){    	 
    	 String msg =  "\nAfter " + PK_Utils.doubleToString(m_counter) + " trials,\n"
    			     + "the closest we have to the target (" + PK_Utils.doubleToString(m_target) 
    			     + ") is " + PK_Utils.doubleToString(m_closestDistance) + " away.\n\n"
    			     + PK_Utils.doubleToString(m_nodes[m_iClosest].getValue()) 
         		     + " = " + m_nodes[m_iClosest].getRepresentation() + "\n";
    	return msg;
     }
     
     public String getResultText(){
    	 
         String result = new String("Final Result.\nHad target: " + PK_Utils.doubleToString(m_target));
         
         if( m_closestDistance == 0)
             result += ",\nfound solution";
         else
             result += ", got to within: " + PK_Utils.doubleToString(m_closestDistance);
             
         result += " using:\n\n" + PK_Utils.doubleToString(m_nodes[m_iClosest].getValue()) 
        		    + " = " + m_nodes[m_iClosest].getRepresentation(); 
         
         result += "\n\nThe total number of trials used was: " + PK_Utils.doubleToString(m_counter);
             	 
         return result;
     }     
  } // end of class SolverStatus
  
  /////////////////////////////////////////////////////////////////////////////
  public class CalcNode
  {
    private String   m_representation;
    private double   m_value;
    private boolean  m_isValid;     // can be invalid either when uninitialized or when there's been a division by zero error
    private Operator m_op;          // the principal operator of the node
    private double   m_distance;    // distance from target
    
    CalcNode( double  val)
    {        
        m_isValid        = (val != 0.0) || ( m_target == 0.0);  // we only accept a zero value if the target is zero.

        if( m_isValid) {
        	m_value           = val;
        	m_representation  = PK_Utils.doubleToString(m_value);
        	m_distance        = abs( val - m_target);
        } else {
        	m_value           = m_LARGE_NUMBER;
            m_representation  = "<INVALID>";
            m_distance        = m_LARGE_NUMBER;
        }

        m_op             = Operator.no_operator;
            
        m_counter++;
    }

    CalcNode( CalcNode leftNode, CalcNode rightNode, Operator op ) throws Exception
    {
        initialize( leftNode, rightNode, op);
        m_counter++;
        
        if( isInteger(m_counter / m_STEPS_BETWEEN_REPORTS)) {
        	Log.d("CalcNode", "Counter is: " + m_counter);
        	String report = m_currentBest.getIntermediateReport();
            Log.d("Report", report);
            m_solverInBG.reportProgress(report);
        }
    }
          
    public void initialize(CalcNode leftNode, CalcNode rightNode, Operator op ) throws Exception
    {
        String opString;
        m_isValid = leftNode.isValid() && rightNode.isValid();
            
        switch (op) {
          case addition:
            m_value = leftNode.getValue() + rightNode.getValue();
            opString = " + ";
            break;
          case division:
            if( rightNode.getValue() == 0.0) // have a division by zero error
               m_isValid = false;      
            else if ( ! isInteger(leftNode.getValue() / rightNode.getValue()))
            	m_isValid = false;
            else
               m_value = leftNode.getValue() / rightNode.getValue();
            opString = " / ";
            break;
          case multiplication:
            m_value = leftNode.getValue() * rightNode.getValue();
            opString = " x ";
            break;
          case subtraction:
            m_value = leftNode.getValue() - rightNode.getValue();
            opString = " - ";
            break;
          default:
            throw new Exception("Unrecognised operator");
            
        }

        m_distance = abs( m_target - getValue());
        
        String leftRep;
        if ( bracketsRequired( leftNode.getOperator(), op))
            leftRep = "(" + leftNode.getRepresentation() + ")";
        else 
        	leftRep = leftNode.getRepresentation();
        
        String rightRep;
        if ( bracketsRequired( rightNode.getOperator(), op))
            rightRep = "(" + rightNode.getRepresentation() + ")";
        else 
        	rightRep = rightNode.getRepresentation();
       
        m_representation = leftRep + opString + rightRep;
     
        m_op = op;
    }  
          
    public boolean isInteger( double d) {
    	return ( d == Math.floor(d));
    }
    
     public CalcNode() {
          m_isValid     = false;
          m_op = Operator.no_operator; 
     }
     
     public double getDistance() { 
    	 //Log.d("getDistance()", "v:" + m_value + " d:" + m_distance + " r:" + m_representation      
    	 //                       + " t:"  + m_target);
    	 if (isValid())   return m_distance; 
    	 else             return m_LARGE_NUMBER;
     }
     
     public String getRepresentation()   
     { 
        if( m_isValid ) return m_representation; 
        else            return new String("<invalid>"); 
     }
    
     public double getValue()
     { 
       if( isValid() )    return m_value;
       else               return m_LARGE_NUMBER; // we deliberately don't throw here since it will be quite 
     }                                          // common to have a division by zero and hence an invalid value.

     public boolean isValid() { 
    	 // we want to exclude zeros, we don't want to waste time
    	 if ( (m_value == 0.0) && (m_target != 0.0))
    		 return false;
    	 else
    	    return m_isValid; 
     }
     
     public Operator getOperator() { return m_op; }
     
     private boolean bracketsRequired(Operator opA, Operator opB){
         if ( opA == Operator.no_operator)
            return false; 
         else if (opA == Operator.compound_op)
        	 return true;
         else if (opB == Operator.subtraction)
        	 return true;
         else if (opB == Operator.division)
        	 return true;
         else 
        	 return opA != opB;              	 
     }
     
    }  // end of class CalcNode          /////////////////////////////////////////////////////////////
}    // end of class CountdownSolver   /////////////////////////////////////////////////////////////


/*

Steps:


make it beautiful.

save back-up

investigate slowness, if slowness persists, perhaps could have an 
intermediate message such as "calculating"

test it on a real device
test a range of different screen sizes

could possibly format the output neater

what happens when focus is lost.

to improve speed, could perhaps just work out the representation of 
a node when it is actually needed, compound nodes could hold references to their parents.


***************************************
Could do:


add progress report, 
could calc in the back-ground.
allow the user to cancel the calc
could add: are you sure ( you want to clear the inputs)
look for a different solution ( jumble the inputs )
tidy code, removing old junk and use sensible naming.

update the screen shots so that the color gradient is used.
update the icons, perhaps to use a color gradient.

could check that when we have an invalid node we don't keep reusing it.

on the pending ( and possibly results page ) could show all the inputs
( though would need to be ensure zeros are zero! 

when it is closed and restarted, it does not remember it's previous state,
should that be changed?

On a samsung phone the text input box background seems to be white,
unlike in the emulator, which uses blue-green

when viewed horizontally it seems the text input box takes over the whole screen.

could possibly make the screens expandable ( and shrinkable)

Suppose we get the solution
   208  = 4 - ( 6  - ( 7 * 3 * 2 * 5 ) )
it may be neater to display it:
   208  = ( 7 * 3 * 2 * 5 ) + 4 - 6 


*/


// Countdown numbers game solver by Philip Kinlen Mar 2011

// One trial problem, which currently takes  192k trials is target = 221
// inputs: { 1000, 1, 1, 3, 51, 26 }

//////////////////////////////////////////////////////////////



