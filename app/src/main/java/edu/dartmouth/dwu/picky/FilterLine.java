package edu.dartmouth.dwu.picky;

/**
 * Created by dwu on 5/19/16.
 */
public class FilterLine {
    public int uid;
    public int action;
    public String message;
    public String data;

    public FilterLine(int uid, int action, String message, String data) {
        this.action = action;
        this.uid = uid;
        this.message = message;
        this.data = data;
    }

    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;

        FilterLine fl = (FilterLine) obj;
        if (fl.action == this.action &&
            fl.uid == this.uid &&
            fl.message.equals(this.message) &&
            fl.data.equals(this.data)) {
            return true;
        }

        return false;
    }
}
