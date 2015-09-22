import java.io.*;
import java.util.*;

class Benchmark {
  String name;
  TreeSet<String> systems=new TreeSet<String>();
  HashMap time=new HashMap();
  TreeSet<Integer> sizes=new TreeSet<Integer>();
  
  Benchmark(String n) {name=n;}
  void addResult(String currentsystem, int size, String exectime) {
    systems.add(currentsystem);    
    String timekey=currentsystem+"#"+size;
    time.put(timekey, exectime);
    sizes.add(new Integer(size));
  }

  boolean includeSize(Integer s) {
    if (sizes.contains(50) && s > 5 && s < 10)
      return false;
    if (sizes.contains(250) && (s < 10 || s == 15))
      return false;
    if (sizes.contains(100) && s < 10)
      return false;
    if (s == 9 || s == 90)
      return false;
    if (sizes.contains(60) && s < 5)
      return false;
    return true;
  }
  
  String getSizes() {
    StringBuffer sb = new StringBuffer();
    sb.append("(");
    boolean notFirst = false;
    for (Integer s : sizes) {
      if (includeSize(s)) {
          if (!notFirst)
            notFirst = true;
          else
            sb.append(",");
          sb.append(s);
      }
    }
    sb.append(")");
    return sb.toString();
  }

  public void dump(String name) throws Exception {
    FileWriter fw2=new FileWriter(name+".cmd");
    fw2.write("set ylabel \"Time (s)\"\n");
    fw2.write("set xlabel \"Data Structure Ops per Thread\"\n");
    fw2.write("set terminal pdfcairo enhanced color\n");
    fw2.write("set style line 1 lt 1 lw 3 ps 1\n");
    fw2.write("set datafile missing '?'\n");
    fw2.write("set style data linespoints\n");
    fw2.write("set xtics "+getSizes()+"\n");
    fw2.write("set y2label offset character 0, 0, 0 font \"\" textcolor lt -1 rotate by 90\n");
    fw2.write("set grid ytics\n");
    fw2.write("set yrange [ 0.1 : * ] noreverse nowriteback\n");
    fw2.write("set cblabel offset character 0, 0, 0 font \"\" textcolor lt -1 rotate by 90\n");
    fw2.write("set locale \"C\"\n");
    fw2.write("set logscale y\n");
    fw2.write("set output \""+name+".pdf\"\n");
    fw2.write("plot '"+name+".dat' ");
    
    int count=1;
    String value="time";
    for(String system: systems) {
      count++;
      String rewrite=system.replace("_","-");;
      if (rewrite.indexOf("nidhugg")!=-1)
        rewrite="nidhugg";
      else if (rewrite.indexOf("satcheck")!=-1)
        rewrite="satcheck";

      value+=", "+rewrite;
      //PRUNE
      if (system.indexOf("zchaff")!=-1)
        continue;
      if (name.indexOf("msqueue")==0&&system.indexOf("satcheck")!=-1&&system.indexOf("offset")==-1)
        continue;
      if (system.indexOf("checkfencelb")!=-1)
        continue;
      
      if (count!=2)
        fw2.write(", '' ");
      int col=count;
      if (rewrite.equals("satcheck"))
        col=0;
      else if (rewrite.equals("nidhugg"))
        col=7;
      else if (rewrite.equals("checkfence"))
        col=1;
      else if (rewrite.equals("cdschecker"))
        col=2;

      int pt=col+1;
      fw2.write("using 1:"+count+" ti col("+count+") w lp lc "+col+" pt "+pt);

    }
    value+="\n";
    for(Integer size: sizes) {
      value+=size;
      for(String system: systems) {
        String timekey=system+"#"+size;
        if (time.containsKey(timekey)) {
          String strtime=(String) time.get(timekey);
          double min=Double.parseDouble(strtime.substring(0, strtime.indexOf('m')));
          double sec=Double.parseDouble(strtime.substring(strtime.indexOf('m')+1, strtime.indexOf('s')));
          double dtime=min*60+sec;

          if (dtime > 3600)
            value+="   ? ";
          else
            value+="  "+dtime;

        } else {
          value+="  ? ";
        }
      }
      value+="\n";
    }
    fw2.write("\n");
    fw2.close();

    FileWriter fw=new FileWriter(name+".dat");
    fw.write(value);
    fw.close();
  }
}

public class parser {
  HashMap<String, Benchmark> bm=new HashMap<String, Benchmark>();
	public static void main(String args[]) throws Exception {
    parser p=new parser();
    p.process(args);
  }

  Benchmark getBench(String name) {
    if (!bm.containsKey(name))
      bm.put(name, new Benchmark(name));
    return bm.get(name);
  }
  
  void process(String args[]) throws Exception {
    for(int i=0;i<args.length;i++) {
      String inputfile=args[i];
      BufferedReader br=new BufferedReader(new FileReader(inputfile));
      String currentsystem=null;
      String currentbench=null;
      int size=0;
      String time=null;
      boolean finished=false;
      boolean bad=false;
      boolean loopbound=false;
      while(true) {
        String input=br.readLine();
        if (input==null)
          break;
        if (input.indexOf("cdschecker/")==0||
            input.indexOf("satcheck/")==0||
            input.indexOf("satcheck-precompiled/")==0||
            input.indexOf("checkfence/")==0||
            input.indexOf("cbmc/")==0||
            input.indexOf("nidhugg/")==0) {
          currentbench=input; 
        } else if (input.indexOf("cdschecker")==0||
                   input.indexOf("checkfence")==0||
                   input.indexOf("satcheck")==0||
                   input.indexOf("cbmc_")==0||
                   input.indexOf("nidhugg_")==0) {
          currentsystem=input;
        } else if (input.indexOf("size=")==0) {
          String strsize=input.substring(5);
          while(strsize.indexOf(" ")==0)
            strsize=strsize.substring(1);
          size=Integer.parseInt(strsize);
          bad=false;
          loopbound=false;
        } else if (input.indexOf("real")==0) {
          time=input.substring(5);
          System.out.println(currentsystem+" "+currentbench+" "+size+" "+time);

          if ((currentsystem.indexOf("satcheck_")==-1 || finished) && !bad) {
            String benchmark=currentbench.substring(currentbench.indexOf('/')+1);
            String csystem=currentsystem;
            if (loopbound) {
              csystem+="lb";
            }
            if (benchmark.equals("msqueueoffset")) {
              benchmark="msqueue";
              csystem=csystem+"offset";
            }
            if (csystem.indexOf("tso")!=-1)
              benchmark+="tso";
            
            Benchmark bm=getBench(benchmark);
            bm.addResult(csystem, size, time);            
          } else
            System.out.println("bad");
          
          finished=false;
        } else if (input.indexOf("Finished")==0) {
          finished=true;
        } else if (input.indexOf("failed")!=-1) {
          bad=true;
        } else if (input.indexOf("Killed")!=-1) {
          bad=true;
        } else if (input.indexOf("error")!=-1 && input.indexOf("No errors")==-1 && input.indexOf("error label")==-1 && input.indexOf("Building error trace")==-1) {
          bad=true;
        } else if (input.indexOf("ERROR")!=-1) {
          bad=true;
        } else if (input.indexOf("Aborted")!=-1) {
          bad=true;
        } else if (input.indexOf("bound")!=-1) {
          loopbound=true;
        }
      }
      br.close();
    }
    for(String key:bm.keySet()) {
      bm.get(key).dump(key);
    }
	}
}
