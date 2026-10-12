package csdev;

import java.io.Serializable;

/**
 * <p>MessageConnect class
 * @author Sergey Gutnikov
 * @version 1.0
 */
public class MessageConnect extends Message implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public String userNic;
	public String userFullName;
	
	public MessageConnect( String userNic, String userFullName ) {
		super( Protocol.CMD_CONNECT );
		this.userNic = userNic;
		this.userFullName = userFullName;
	}
	
}