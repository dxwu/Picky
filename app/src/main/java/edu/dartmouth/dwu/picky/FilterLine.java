package edu.dartmouth.dwu.picky;

/**
 * Created by dwu on 5/19/16.
 */
public class FilterLine {
    public int uid;
    public int action;
    public String message;
    public String data;

    public int context;
    public int contextType;     // int or string?
    public int contextIntValue;
    public String contextStringValue;

    public FilterLine(int uid, int action, String message, String data) {
        this.action = action;
        this.uid = uid;
        this.message = message;
        this.data = data;

        this.context = 0;
        this.contextStringValue = "";

        if (message == null) {
            this.message = "";
        }
        if (data == null) {
            this.data = "";
        }
    }

    public FilterLine(int uid, int action, String message, String data, int context,
                      int contextType, int contextIntValue, String contextStringValue) {
        this.uid = uid;
        this.action = action;
        this.message = message;
        this.data = data;
        this.context = context;
        this.contextType = contextType;
        this.contextIntValue = contextIntValue;
        this.contextStringValue = contextStringValue;

        if (message == null) {
            this.message = "";
        }
        if (data == null) {
            this.data = "";
        }
        if (contextStringValue == null) {
            this.contextStringValue = "";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FilterLine that = (FilterLine) o;

        if (uid != that.uid) return false;
        if (action != that.action) return false;
        if (context != that.context) return false;
        if (contextType != that.contextType) return false;
        if (contextIntValue != that.contextIntValue) return false;
        if (!message.equals(that.message)) return false;
//        if (!data.equals(that.data)) return false;
        return contextStringValue.equals(that.contextStringValue);

    }
}
