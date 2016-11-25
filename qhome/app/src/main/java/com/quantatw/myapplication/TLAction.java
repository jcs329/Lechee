package com.quantatw.myapplication;


/*  ***************************************************** */
/*  ************** Bridge to LiveHD Actions ************* */
/*  ***************************************************** */
/*  --------------[ PLEASE DO NOT MODIFY ]--------------- */

/**
 * @author      Steven Lin <stevenlin1@quantatw.com>
 * @version     1.0
 * @since       2014-12-08
 */

public enum TLAction {
	ACTION_NONE						(0, "ACTION_NONE"),
	ACTION_GET_CALL_RESULT			(1, "ACTION_RECEIVE_CALL_RESULT"),
	ACTION_GET_SIPNAME				(2, "ACTION_GET_SIPNAME"),
	ACTION_ANSWER_DOOR_CALL			(3, "ACTION_ANSWER_DOOR_CALL"),
	ACTION_OPEN_DOOR				(4, "ACTION_OPEN_DOOR"),
	ACTION_LAUNCH_LIVEHD			(5, "ACTION_LAUNCH_LIVEHD"),
	ACTION_DIAL						(6, "ACTION_DIAL"),
	ACTION_GET_SIPPASSWORD			(7, "ACTION_GET_SIPPASSWORD"),
	ACTION_HANGUP_LIVEHD			(8, "ACTION_HANGUP_LIVEHD"),
	ACTION_ENABLE_DOOR				(9, "ACTION_ENABLE_DOOR"),
	ACTION_ENABLE_DONOTDISTURB		(10, "ACTION_ENABLE_DONOTDISTURB"),
	ACTION_GET_SIP_STATUS			(11, "ACTION_GET_SIP_STATUS"),
	ACTION_PUT_SIP_STATUS			(12, "ACTION_PUT_SIP_STATUS"),
	ACTION_AUTO_PROVISION			(13, "ACTION_AUTO_PROVISION"),
	ACTION_DOOR_CALL_FAILED			(14, "ACTION_DOOR_CALL_FAILED"),
	ACTION_KILL_LIVEHD				(15, "ACTION_KILL_LIVEHD"),
	ACTION_FORCE_HANGUP_TLAPP		(16, "ACTION_FORCE_HANGUP_TLAPP"),
	ACTION_SET_DEBUG_MODE			(17, "ACTION_SET_DEBUG_MODE"),
	ACTION_LOGOUT					(18, "ACTION_LOGOUT"),
	ACTION_GET_CONTACTS             (19, "ACTION_GET_CONTACTS"),
	ACTION_PUT_CONTACTS             (20, "ACTION_PUT_CONTACTS"),
	ACTION_ADD_CONTACT              (21, "ACTION_ADD_CONTACT"),
	ACTION_DELETE_CONTACT           (22, "ACTION_DELETE_CONTACT"),
	ACTION_GET_CALLLOGS             (23, "ACTION_GET_CALLLOGS"),
	ACTION_PUT_CALLLOGS             (24, "ACTION_PUT_CALLLOGS"),
	ACTION_DOOR_CALL_READY         	(25, "ACTION_DOOR_CALL_READY"),
    ACTION_DOOR_CALL_CANCEL          (26, "ACTION_DOOR_CALL_CANCEL"),
	ACTION_AUTO_ANSWER				(27, "ACTION_AUTO_ANSWER");
	
	private int value;
	private String name;

	private TLAction(int value, String name) {
		this.value = value;
		this.name  = name;
	}

    /**
     * Get index of the TLAction  
     * 
     * @return          index of the TLAction
     */
    public int getActionType() {
        return value;
    }
    
    /**
     * Get readable name of the TLAction  
     * 
     * @return          readable name of the TLAction
     */
    public String getActionName() {
        return name;
    }

    /**
     * Mapping action integer to TLAction 
     * 
     * @param action    Corresponding action integer 
     * @return          TLAction if the action int is valid
     */
    public static TLAction actionMapping(int action) {
    	switch(action)
    	{
    		case 0:
    			return TLAction.ACTION_NONE;
    		case 1:
    			return TLAction.ACTION_GET_CALL_RESULT;
    		case 2:
    			return TLAction.ACTION_GET_SIPNAME;
    		case 3:
    			return TLAction.ACTION_ANSWER_DOOR_CALL;
    		case 4:
    			return TLAction.ACTION_OPEN_DOOR;
    		case 5:
    			return TLAction.ACTION_LAUNCH_LIVEHD;
    		case 6:
    			return TLAction.ACTION_DIAL;
    		case 7:
    			return TLAction.ACTION_GET_SIPPASSWORD;
    		case 8:
    			return TLAction.ACTION_HANGUP_LIVEHD;
    		case 9:
    			return TLAction.ACTION_ENABLE_DOOR;
    		case 10:
    			return TLAction.ACTION_ENABLE_DONOTDISTURB;
    		case 11:
    			return TLAction.ACTION_GET_SIP_STATUS;
    		case 12:
    			return TLAction.ACTION_PUT_SIP_STATUS;
    		case 13:
    			return TLAction.ACTION_AUTO_PROVISION;
    		case 14:
    			return TLAction.ACTION_DOOR_CALL_FAILED;
    		case 15:
    			return TLAction.ACTION_KILL_LIVEHD;
    		case 16:
    			return TLAction.ACTION_FORCE_HANGUP_TLAPP;
    		case 17:
    			return TLAction.ACTION_SET_DEBUG_MODE;
    		case 18:
    			return TLAction.ACTION_LOGOUT;
			case 19:
				return TLAction.ACTION_GET_CONTACTS;
			case 20:
				return TLAction.ACTION_PUT_CONTACTS;
			case 21:
				return TLAction.ACTION_ADD_CONTACT;
			case 22:
				return TLAction.ACTION_DELETE_CONTACT;
			case 23:
				return TLAction.ACTION_GET_CALLLOGS;
			case 24:
				return TLAction.ACTION_PUT_CALLLOGS;
			case 25:
				return TLAction.ACTION_DOOR_CALL_READY;
            case 26:
                return TLAction.ACTION_DOOR_CALL_CANCEL;
			case 27:
				return TLAction.ACTION_AUTO_ANSWER;
    	}
    	return null;
    }
} 

