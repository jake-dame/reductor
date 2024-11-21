package reductor;

//public class Context {
//
//    public final long length;
//    public final int resolution;
//
//    public static Context CONTEXT;
//
//    private Context(int resolution, long length) {
//        this.length = length;
//        this.resolution = resolution;
//    }
//
//    public static Context ContextFactory(int resolution, long length) {
//        if (CONTEXT != null) { throw new IllegalStateException("tried to make a new context"); }
//        CONTEXT = new Context(resolution, length);
//        return CONTEXT;
//    }
//
//}

public class Context {

    public static Context INSTANCE;

    public static long DEFAULT_LAST_TICK = Long.MAX_VALUE;
    public static int DEFAULT_RESOLUTION = 480;

    private final long lastTick;
    private final int resolution;

    private Context(int resolution, long lastTick) {
        this.lastTick = lastTick;
        this.resolution = resolution;
    }

    public static Context createContext() {
        //if (INSTANCE != null) { throw new IllegalStateException("tried to make a new context"); }
        INSTANCE = new Context(DEFAULT_RESOLUTION, DEFAULT_LAST_TICK);
        return INSTANCE;
    }

    public static Context createContext(int resolution, long lastTick) {
        //if (INSTANCE != null) { throw new IllegalStateException("tried to make a new context"); }
        INSTANCE = new Context(resolution, lastTick);
        return INSTANCE;
    }

    public static int resolution() {
        if (Context.INSTANCE == null) {
            throw new RuntimeException("context has not been initialized, can't get resolution");
        }
        return Context.INSTANCE.resolution;
    }

    public static long finalTick() {
        if (Context.INSTANCE == null) {
            throw new RuntimeException("context has not been initialized, can't get last tick");
        }
        return Context.INSTANCE.lastTick;
    }


}
