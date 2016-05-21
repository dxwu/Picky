package edu.dartmouth.dwu.picky;

/**
 * Created by dwu on 5/20/16.
 */
public class PolicyMessage {

    // what we display to the user
    public String displayMessage;

    // what we actually block
    public String filterMessage;

    public PolicyMessage(String displayMessage, String filterMessage) {
        this.displayMessage = displayMessage;
        this.filterMessage = filterMessage;
    }

    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;

        PolicyMessage pm = (PolicyMessage) obj;
        if (pm.displayMessage.equals(this.displayMessage) &&
                pm.filterMessage.equals(this.filterMessage)) {
            return true;
        }

        return false;
    }
}
