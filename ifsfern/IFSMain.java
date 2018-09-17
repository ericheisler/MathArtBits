import javax.swing.*;
/**
 * An IFS fern app
 * 
 *
 * 
 */
public class IFSMain{
	
	private static IFSFern fern;
	
    // the main function used to be the init function. I hope it works
    public static void main(String args[]){
		fern = new IFSFern();
		//init on the event-dispatching thread:
		try {
			javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					fern.initializeThings();
				}
			});
		} catch (Exception e) {
			System.err.println("initialization didn't successfully complete");
		}
	}
}