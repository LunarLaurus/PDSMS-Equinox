package utils.listeners;

// Written by Hello007.
public class AbstractToggleableListener {
    private boolean allowEvents = true;

    public void setAllowEvents(boolean val) {
        allowEvents = val;
    }

    public boolean getAllowEvents() {
        return allowEvents;
    }

    public static void setAllowEventsMulti(boolean value, AbstractToggleableListener... listeners) {
        for (AbstractToggleableListener l : listeners) {
            l.setAllowEvents(value);
        }
    }
}
