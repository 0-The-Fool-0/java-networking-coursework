package csdev;

import java.io.Serializable;

/**
 * <p>MessageLetterResult class
 * @author Sergey Gutnikov
 * @version 1.0
 */
public class MessageLetterResult extends MessageResult implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public MessageLetterResult( String errorMessage ) { //Error
		
		super( Protocol.CMD_LETTER, errorMessage );
	}
	
	public MessageLetterResult() { // No errors
		
		super( Protocol.CMD_LETTER );
	}
}