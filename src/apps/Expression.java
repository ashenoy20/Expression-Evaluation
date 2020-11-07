package apps;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import structures.Stack;

public class Expression {

	/**
	 * Expression to be evaluated
	 */
	String expr;                
    
	/**
	 * Scalar symbols in the expression 
	 */
	ArrayList<ScalarSymbol> scalars;   
	
	/**
	 * Array symbols in the expression
	 */
	ArrayList<ArraySymbol> arrays;
    
    /**
     * String containing all delimiters (characters other than variables and constants), 
     * to be used with StringTokenizer
     */
    public static final String delims = " \t*+-/()[]";
    
    /**
     * Initializes this Expression object with an input expression. Sets all other
     * fields to null.
     * 
     * @param expr Expression
     */
    public Expression(String expr) {
        this.expr = expr;
    }

    /**
     * Populates the scalars and arrays lists with symbols for scalar and array
     * variables in the expression. For every variable, a SINGLE symbol is created and stored,
     * even if it appears more than once in the expression.
     * At this time, values for all variables are set to
     * zero - they will be loaded from a file in the loadSymbolValues method.
     */
    public void buildSymbols() {
       this.expr = this.expr.replaceAll(" ", "");
       scalars = new ArrayList<ScalarSymbol>();
       arrays = new ArrayList<ArraySymbol>();
       String temp = "";
       for(int i = 0; i < this.expr.length(); i++) {
    	   char ch = this.expr.charAt(i);
    	   if(ch == '+' || ch == '-' || ch == '*' || ch == '/' || ch == '(' || ch == ')' || ch == '[' || ch == ']' || Character.isDigit(ch)) {
    		   if(ch == '[') {
    			   ArraySymbol newArr = new ArraySymbol(temp);
    			   if(arrays.contains(newArr)) {
    				   temp = "";
    				   continue;
    			   }
    			   arrays.add(newArr);
    			   temp = "";
    		   }else if(!temp.isEmpty()){
    			   ScalarSymbol newScalar = new ScalarSymbol(temp);
    			   if(scalars.contains(newScalar)) {
    				   temp = "";
    				   continue;
    			   }
    			   scalars.add(newScalar);
    			   temp = "";
    		   }else {
    			   continue;
    		   }
    	   }else {
    		   temp += Character.toString(ch);
    	   }
       }
       //This will for sure be a scalar variable if temp isn't empty when the loop ends
       if(!temp.isEmpty()) {
    	   ScalarSymbol newS = new ScalarSymbol(temp);
    	   if(scalars.contains(newS)) {
			   temp = "";  
		   }else {
			   scalars.add(newS);
			   temp = "";
		   }
       }
    }
   
    
    /**
     * Loads values for symbols in the expression
     * 
     * @param sc Scanner for values input
     * @throws IOException If there is a problem with the input 
     */
    public void loadSymbolValues(Scanner sc) 
    throws IOException {
        while (sc.hasNextLine()) {
            StringTokenizer st = new StringTokenizer(sc.nextLine().trim());
            int numTokens = st.countTokens();
            String sym = st.nextToken();
            ScalarSymbol ssymbol = new ScalarSymbol(sym);
            ArraySymbol asymbol = new ArraySymbol(sym);
            int ssi = scalars.indexOf(ssymbol);
            int asi = arrays.indexOf(asymbol);
            if (ssi == -1 && asi == -1) {
            	continue;
            }
            int num = Integer.parseInt(st.nextToken());
            if (numTokens == 2) { // scalar symbol
                scalars.get(ssi).value = num;
            } else { // array symbol
            	asymbol = arrays.get(asi);
            	asymbol.values = new int[num];
                // following are (index,val) pairs
                while (st.hasMoreTokens()) {
                    String tok = st.nextToken();
                    StringTokenizer stt = new StringTokenizer(tok," (,)");
                    int index = Integer.parseInt(stt.nextToken());
                    int val = Integer.parseInt(stt.nextToken());
                    asymbol.values[index] = val;              
                }
            }
        }
    }
    
    
    /**
     * Evaluates the expression, using RECURSION to evaluate subexpressions and to evaluate array 
     * subscript expressions.
     * 
     * @return Result of evaluation
     */
    public float evaluate() {
    	return recursiveEvaluate(this.expr);
    }
    
    private float recursiveEvaluate(String subexpr) {
    	int first = 0;
    	int last = 0;
    	Stack<String> paren = new Stack<String>();
    	for(int i = 0; i < subexpr.length(); i++) {
    		char ch = subexpr.charAt(i);
    		if(ch == '(') {
    			paren.push(Character.toString(ch));
    			if(paren.size() == 1) {
    				first = i;
    			}	
    		}else if(ch == ')') {
    			if(paren.size() == 1) {
    				last = i;
    			}else {
    				paren.pop();
    			}
    		}
    	}
    	if(first == 0 && last == 0) {
    		int b1 = 0;
    		int b2 = 0;
    		String temp2 = "";
    		for(int i = 0; i < subexpr.length(); i++) {
 	    	   char ch2 = subexpr.charAt(i);
 	    	   Stack<String> brackets = new Stack<String>();
 	    	   if(ch2 == '+' || ch2 == '-' || ch2 == '*' || ch2 == '/' || ch2 == '[' || ch2 == ']' || Character.isDigit(ch2)) {
 	    		  if(ch2 == '[') {
 	    			 b1 = i;
 	    			 brackets.push(Character.toString(ch2));
 	    			 for(int a = i+1; a < subexpr.length();a++) {
 	    				char temp = subexpr.charAt(a); 
 	    				if(temp == '[') {
 	    	    			brackets.push(Character.toString(temp));
 	    	    			if(brackets.size() == 1) {
 	    	    				b1 = a;
 	    	    			}	
 	    	    		}else if(temp == ']') {
 	    	    			if(brackets.size() == 1) {
 	    	    				b2 = a;
 	    	    			}else {
 	    	    				brackets.pop();
 	    	    			}
 	    	    		}
 	    			 }
 	    			 int innerArr = (int) recursiveEvaluate(subexpr.substring(b1+1, b2));
 	    			 int arrVal = 0;
 	    			 String arrName = "";
 	    			 for(int j = 0; j < arrays.size(); j++) {
	    				  if(arrays.get(j).name.equals(temp2)) {
	    					  arrVal = arrays.get(j).values[innerArr];
	    					  arrName = arrays.get(j).name;
	    					  i=0;
	    					  break;
	    				  }
	    			  }
 	    			String tempComp = "";
 	    			if(b2 == subexpr.length()-1) {
 	    				tempComp = arrName+subexpr.substring(b1);
 	    			}else {
 	    				tempComp = arrName+subexpr.substring(b1,b2+1);
 	    			}
 	     	        ArrayList<String> arrAppend = new ArrayList<String>();
 	     	        String c = "";
 	     	        for(int k = 0; k < subexpr.length(); k++) {
 	     	        	char tempchar = subexpr.charAt(k);
 	     	        	if(k >= b1 && k <= b2) {
 	     	        		c += Character.toString(tempchar);
 	     	        	}else if(tempchar == '+' || tempchar == '-' || tempchar == '*' || tempchar == '/') {
 	     	        		arrAppend.add(c);
 	     	        		arrAppend.add(Character.toString(tempchar));
 	     	        		c = "";
 	     	        	}else {
 	     	        		c += Character.toString(tempchar);
 	     	        	}
 	     	        }
 	     	        if(!c.isEmpty()) {
 	     	        	arrAppend.add(c);
 	     	        }
 	     	        for(int j = 0; j < arrAppend.size(); j++) {
 	     	        	if(arrAppend.get(j).equals(tempComp)) {
 	       	    		arrAppend.set(j, Integer.toString(arrVal));
 	     	        	}
 	     	        }
 	     	        subexpr = "";
 	     	        for(int k = 0; k < arrAppend.size(); k++) {
 	     	        	subexpr += arrAppend.get(k);
 	     	        }
 	     	        temp2 = "";
 	    		  }else if(!temp2.isEmpty()) {
 	    			  int intVal = 0;
 	    			  for(int j = 0; j < scalars.size(); j++) {
 	    				  if(scalars.get(j).name.equals(temp2)) {
 	    					  intVal = scalars.get(j).value;
 	    					  String value = Integer.toString(intVal);
 	    					  subexpr = subexpr.replaceAll(temp2, value);
 	    					  temp2 = "";
 	    					  i=0;
 	    					  break;
 	    				  }
 	    			  }
 	    			  intVal = 0;
 	    		  }
 	    	   }else {
 	    		  temp2 += Character.toString(ch2); 
 	    	   }
 	        } 
    		if(!temp2.isEmpty()) {
    			for(int b = 0; b < scalars.size();b++) {
    				if(scalars.get(b).name.equals(temp2)) {
    					subexpr = subexpr.replaceAll(temp2, Integer.toString(scalars.get(b).value));
    				}
    			}	
    	    }
    	    
    	    ArrayList<String> split = new ArrayList<String>();
    	    String concat = "";
    	    int optracker = 0;
    	    for(int i = 0; i < subexpr.length(); i++) {
    	    	char ch3 = subexpr.charAt(i);
    	    	if(optracker == 1 || i == 0) {
    	    		optracker = 0;
    	    		concat += Character.toString(ch3);
    	    	}else if(ch3 == '+' || ch3 == '-' || ch3 == '/' || ch3 == '*') {
    	    		split.add(concat);
    	    		split.add(Character.toString(ch3));
    	    		concat = "";
    	    		optracker++;
    	    	}else {
    	    		concat += Character.toString(ch3);
    	    	}
    	    }
    	    if(!concat.isEmpty()) {
    	    	split.add(concat);
    	    }
    	    for(int i = 0; i < split.size(); i++) {
    	    	float f1 = 0;
    	    	float f2 = 0;
    	    	float res1 = 0;
    	    	if(split.get(i).equals("*")) {
    	    		f1 = Float.parseFloat(split.get(i-1));
    	    		f2 = Float.parseFloat(split.get(i+1));
    	    		res1 = f1*f2;
    	    		split.set(i, Float.toString(res1));
    	    		split.remove(i+1);
    	    		split.remove(i-1);
    	    		i = 0; //To make sure we don't overlook the last operation
    	    	}else if(split.get(i).equals("/")) {
    	    		f1 = Float.parseFloat(split.get(i-1));
    	    		f2 = Float.parseFloat(split.get(i+1));
    	    		res1 = f1/f2;
    	    		split.set(i, Float.toString(res1));
    	    		split.remove(i+1);
    	    		split.remove(i-1);
    	    		i = 0; //To make sure we don't overlook the last operation
    	    	}
    	    }
    	    for(int i = 0; i < split.size(); i++) {
    	    	float f3 = 0;
    	    	float f4 = 0;
    	    	float res2 = 0;
    	    	if(split.get(i).equals("+")) {
    	    		f3 = Float.parseFloat(split.get(i-1));
    	    		f4 = Float.parseFloat(split.get(i+1));
    	    		res2 = f3+f4;
    	    		split.set(i, Float.toString(res2));
    	    		split.remove(i+1);
    	    		split.remove(i-1);
    	    		i = 0; //To make sure we don't overlook the last operation
    	    	}else if(split.get(i).equals("-")) {
    	    		f3 = Float.parseFloat(split.get(i-1));
    	    		f4 = Float.parseFloat(split.get(i+1));
    	    		res2 = f3-f4;
    	    		split.set(i, Float.toString(res2));
    	    		split.remove(i+1);
    	    		split.remove(i-1);
    	    		i = 0; //To make sure we don't overlook the last operation
    	    	}
    	    }
    	    
    	    float finResult = Float.parseFloat(split.get(0));
    	    return finResult;
    		
    	}else {
    		String p = "";
    		if(last == subexpr.length()-1) {
    			p = subexpr.substring(first);
    		}else {
    			p = subexpr.substring(first,last+1);
    		}
  	        float subVal = recursiveEvaluate(subexpr.substring(first+1, last));
  	        ArrayList<String> append = new ArrayList<String>();
  	        String track = "";
  	        int optracker2 = 0;
  	        for(int i = 0; i < subexpr.length(); i++) {
  	        	char ch5 = subexpr.charAt(i);
  	        	if(optracker2 == 1 || i == 0) {
    	    		optracker2 = 0;
    	    		track += Character.toString(ch5);
  	        	}else if(i >= first && i <= last) {
  	        		if(!track.isEmpty() && i == first) {
  	        			append.add(track);
  	  	        		track= "";
  	  	        		track += Character.toString(ch5);
  	        		}else if(i == last) {
  	        			track += Character.toString(ch5);
  	        			append.add(track);
  	        			track = "";
  	        		}else {
  	        			track += Character.toString(ch5);
  	        		}
  	        	}else if(ch5 == '+' || ch5 == '-' || ch5 == '*' || ch5 == '/') {
  	        		append.add(track);
  	        		append.add(Character.toString(ch5));
  	        		track = "";
  	        		optracker2++;
  	        	}else {
  	        		track += Character.toString(ch5);
  	        	}
  	        }
  	        if(!track.isEmpty()) {
  	        	append.add(track);
  	        }
    	    for(int j = 0; j < append.size(); j++) {
    	    	if(append.get(j).equals(p)) {
    	    		append.set(j, Integer.toString((int) subVal));
    	    	}
    	    }
    	    
    	    subexpr = "";
    	    for(int k = 0; k < append.size(); k++) {
    	    	subexpr += append.get(k);
    	    }
    		return recursiveEvaluate(subexpr);
    	}
    	
    }
    
    
  
    /**
     * Utility method, prints the symbols in the scalars list
     */
    public void printScalars() {
        for (ScalarSymbol ss: scalars) {
            System.out.println(ss);
        }
    }
    
    /**
     * Utility method, prints the symbols in the arrays list
     */
    public void printArrays() {
    		for (ArraySymbol as: arrays) {
    			System.out.println(as);
    		}
    }

}