/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package group;

import cclo.util.args.*;
import java.util.*;

/**
 *
 * @author antonio
 */
public class Group{

    public static final String PROGRAM_NAME = "Grouping";
    public static final String PROGRAM_VERSION = "v1.5";
    private static final ArgumentParser OPTION;

    static{
        Option opr = new OptionBuilder().withName('r').withArgs(1, "round")
          .withDesp("the times of the change, default is equal to the <group>").create();
        Option ops = new OptionBuilder().withName('s').withArgs(1, "less")
          .withDesp("the least number of the people in a group, default is <count> / <group>")
          .create();
        Option opl = new OptionBuilder().withName('l').withArgs(1, "limit")
          .withDesp("the most number of the people in a group, default is <count> / <group> + 1")
          .create();
        Option opm = new OptionBuilder().withName('m').withArgs(1, "meet")
          .withDesp("the most times for a person meet the same another, default is 2").create();
        OPTION = new ArgumentParser(opr, ops, opl, opm);
    }
    public static final String HELP_DOC
      = "  usage:\n"
      + "    java [group.Group]|[-jar jarFile] <count> <group> [options]\n"
      + "    java [group.Group]|[-jar jarFile] -[h|?|help] : show help doc\n"
      + "    java [group.Group]|[-jar jarFile] -[v|version] : show version\n"
      + "\n"
      + "  arguement:\n"
      + "    count     : the number of the people\n"
      + "    group     : the number of the group\n"
      + "\n"
      + "  options:\n"
      + new ArgumentFormatter().format(OPTION);
    //

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args){
        if (args.length < 2 || args[0].matches("-(h(elp)?|\\?)")) {
            System.out.println(HELP_DOC);
            System.exit(0);
        } else if (args[0].matches("-v(ersion)?")) {
            System.out.println(PROGRAM_NAME + " " + PROGRAM_VERSION);
            System.exit(0);
        }
        new Group(paserTaskArgs(args)).execute();
    }

    public static Task paserTaskArgs(String[] args){
        Task t = new Task();
        t.count = Integer.parseInt(args[0]);
        t.group = Integer.parseInt(args[1]);
        t.setDefault();
        Argument arg = OPTION.parses(Arrays.copyOfRange(args, 2, args.length));
        if (arg.hasPresent("r")) {
            t.round = Integer.parseInt(arg.asStr("r"));
        }
        if (arg.hasPresent("s")) {
            t.less = Integer.parseInt(arg.asStr("s"));
        }
        if (arg.hasPresent("l")) {
            t.limit = Integer.parseInt(arg.asStr("l"));
        }
        if (arg.hasPresent("m")) {
            t.meet = Integer.parseInt(arg.asStr("m"));
        }
        try{
            System.out.println("check task arguement...");
            t.checkInit();
            System.out.println(t.toString());
        } catch (IllegalStateException e){
            System.out.println("failure");
            System.out.println(e);
            System.exit(1);
        }
        return t;
    }
    private final Task task;
    private final Set<Node> set;
    private long timeUsed;

    Group(Task task){
        Objects.requireNonNull(task);
        this.task = task;
        set = Collections.synchronizedSet(new HashSet<Node>(32));
    }

    void execute(){
        task.answer = null;
        try{
            System.out.println("start working...");
            fill();
            System.out.printf("used %.4fs\n", timeUsed / 1000.0);
            if (task.answer != null) {
                System.out.println("answer :");
                for (Node n = task.answer; n != null; n = n.prev) {
                    System.out.println(n.toString());
                }
                System.out.println("done");
            } else {
                System.out.println("no answer");
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void fill(){
        long time = System.currentTimeMillis();
        Node curt = new Node(task.count);
        for (int i = 0; i < task.count; i++) {
            curt.array[i] = i % task.group + 1;
        }
        set.add(curt);
        fillrow(curt, 0);
        timeUsed = System.currentTimeMillis() - time;
    }

    private boolean fillrow(Node curt, int level){
        int place = 0;
        int gv = 0;
        int[] gc = new int[task.group];
        while (task.answer == null) {
            if (place == task.count) {
                if (checkLess(task.less, gc)) {
                    if (set.add(curt)) {
                        if (level == task.round - 1) {
                            task.answer = curt;
                            return true;
                        } else {
                            Node next = new Node(task.count);
                            next.prev = curt;
                            if (fillrow(next, level + 1)) {
                                return true;
                            }
                        }
                    }
                }
                gv = curt.array[--place];
                gc[gv - 1]--;
            }
            while (gv >= task.group) {
                if (place == 0) return false;
                gv = curt.array[--place];
                gc[gv - 1]--;
            }
            if (gc[gv] > task.limit || checkIn(curt, place, gv + 1)) {
                gv++;
            } else {
                curt.array[place] = gv + 1;
                if (checkMeetForAll(curt, task.meet, place)) {
                    gc[gv]++;
                    gv = 0;
                    place++;
                } else {
                    gv++;
                }
            }
        }
        return task.answer != null;
    }

    private static boolean checkLess(int less, int[] gc){
        for (int i = 0, sz = gc.length; i < sz; i++) {
            if (gc[i] < less) return false;
        }
        return true;
    }

    private static boolean checkMeetForAll(Node node, int meet, int place){
        for (int i = 0; i < place; i++) {
            if (!checkMeet(node, meet, i, place)) return false;
        }
        return true;
    }

    private static boolean checkIn(Node node, int p, int v){
        for (Node n = node; n != null; n = n.prev) {
            if (n.array[p] == v) return true;
        }
        return false;
    }

    private static boolean checkMeet(Node node, int meet, int i, int j){
        if (i == j) return true;
        int r = 0;
        for (Node n = node; n != null && r <= meet; n = n.prev) {
            int v = n.array[i];
            int u = n.array[j];
            if (v != Node.EMPTY && u != Node.EMPTY && v == u) r++;
        }
        return r <= meet;
    }
}
