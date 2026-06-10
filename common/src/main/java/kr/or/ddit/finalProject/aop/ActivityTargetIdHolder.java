package kr.or.ddit.finalProject.aop;

public class ActivityTargetIdHolder {

    private static final ThreadLocal<String> HOLDER = new ThreadLocal<>();

    public static void set(String targetId) { HOLDER.set(targetId); }
    public static String get()              { return HOLDER.get(); }
    public static void clear()              { HOLDER.remove(); }
}
