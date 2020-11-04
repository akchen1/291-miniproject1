import java.io.*;

public class PasswordField {

	   /**
	    *@param prompt The prompt to display to the user
	    *@return The password as entered by the user
	    */
	   public static String readPassword (String prompt) {
	      EraserThread et = new EraserThread(prompt);
		  Thread mask = new Thread(et);
		  

		  BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		  mask.start();
	      String password = "";

	      try {
	         password = in.readLine();
	      } catch (IOException ioe) {
	        ioe.printStackTrace();
	      }
	      // stop masking
	      et.stopMasking();
	      // return the password entered by the user
	      return password;
	   }
	}
