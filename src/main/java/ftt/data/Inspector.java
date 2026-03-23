package ftt.data;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Inspector {

    UUID uuid;
    ActionListener listener;

    AtomicBoolean close = new AtomicBoolean(false);

    public Inspector(UUID uuid, ScheduledExecutorService executor, ActionListener listener) {
        this.listener = listener;
        this.uuid = uuid;
        executor.schedule(() -> {
            if(!close.get())
                listener.actionPerformed(new ActionEvent(uuid,0,"del"));
        },5, TimeUnit.SECONDS);
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public void close() {
        close.set(true);
    }
}
