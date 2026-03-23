package ftt;

import java.util.ArrayList;
import java.util.List;

public class Sinhronizer {

    public static final int ANY = 0;
    public static final int ALL = 1;

    class SyncItem {
        LineItem item;
        boolean mark = false;

        public SyncItem(LineItem item) {
            this.item = item;
        }

        public boolean chek(LineItem i) {
            if(!mark && i.equals(item)) {
                return mark = true;
            }
            return false;
        }

        public void reset() {
            mark = false;
        }
    }

    int type;
    int count = 0;
    int porog = 0;

    ArrayList<SyncItem> cl = new ArrayList<>();

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
        reset();
    }

    public void reset() {
        count = 0;
        for(SyncItem si: cl) {
            si.reset();
        }
    }

    public void check(LineItem i) {
        for(SyncItem si: cl) {
            if(si.chek(i)) count++;
        }
    }

    public void init(List<LineItem> list) {
        porog = 0;
        cl.removeAll(cl);
        for(LineItem li: list) {
            if(li.getWrite()) {
                cl.add(new SyncItem(li));
                porog++;
            }
        }
        count = 0;
    }

    public boolean sync(LineItem i) {
        switch (type) {
            case ANY: {
                reset();
                check(i);
                return count != 0;
            }
            case ALL: {
                check(i);
                if(count == porog) {
                    reset();
                    return true;
                }
            }
        }
        return false;
    }
}
