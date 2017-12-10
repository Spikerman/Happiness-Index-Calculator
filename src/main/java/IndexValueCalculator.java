import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

/**
 * Created Date: 12/10/17
 */
public class IndexValueCalculator {

    static final int clusterNum = 2000;
    Map<Integer, List<Record>> mapTwitter = new HashMap<>();
    Map<Integer, List<Record>> map311 = new HashMap<>();
    Map<Integer, List<Record>> mapCrime = new HashMap<>();
    Map<Integer, Double> valueTwitter = new HashMap<>();
    Map<Integer, Double> value311 = new HashMap<>();
    Map<Integer, Double> valueCrime = new HashMap<>();
    Map<Integer, Double> indexMap = new TreeMap<>();

    public IndexValueCalculator() {
        read();
        valueCompute();
    }

    public static void main(String args[]) {
        IndexValueCalculator indexValueCalculator = new IndexValueCalculator();
        System.out.println("test");
    }

    private void read() {
        try {
            File sourceFile = new File("result.csv");
            FileReader fileReader = new FileReader(sourceFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] tuple = line.split(",");
                Record record = new Record(tuple[0], tuple[1], tuple[2], tuple[3], tuple[4]);
                if (record.getSourceId() == 0) {
                    if (!mapTwitter.containsKey(record.getClusterId()))
                        mapTwitter.put(record.getClusterId(), new ArrayList<>());
                    mapTwitter.get(record.getClusterId()).add(record);
                } else if (record.getSourceId() == 1) {
                    if (!map311.containsKey(record.getClusterId()))
                        map311.put(record.getClusterId(), new ArrayList<>());
                    map311.get(record.getClusterId()).add(record);
                } else {
                    if (!mapCrime.containsKey(record.getClusterId()))
                        mapCrime.put(record.getClusterId(), new ArrayList<>());
                    mapCrime.get(record.getClusterId()).add(record);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void valueCompute() {
        DescriptiveStatistics dsT = new DescriptiveStatistics();
        DescriptiveStatistics dsC = new DescriptiveStatistics();
        DescriptiveStatistics ds3 = new DescriptiveStatistics();
        for (int i = 0; i < clusterNum; i++) {
            if (mapTwitter.containsKey(i) && mapCrime.containsKey(i) && map311.containsKey(i)) {
                List<Record> listT = mapTwitter.get(i);
                List<Record> listC = mapCrime.get(i);
                List<Record> list3 = map311.get(i);
                double avgT = 0, avgC = 0, avg3 = 0;
                for (Record record : listT) {
                    avgT += record.getSocre();
                }
                avgT = avgT / listT.size();
                avgC = listC.size() / (double) listT.size();
                avg3 = list3.size() / (double) listT.size();
                dsT.addValue(avgT);
                dsC.addValue(avgC);
                ds3.addValue(avg3);

                valueTwitter.put(i, avgT);
                valueCrime.put(i, avgC);
                value311.put(i, avg3);

                //System.out.println(i + " " + avgT + " " + avgC + " " + avg3);
            }
        }

        double svT = dsT.getStandardDeviation();
        double svC = dsC.getStandardDeviation();
        double sv3 = ds3.getStandardDeviation();

        System.out.println(svT + " " + svC + " " + sv3);

        DescriptiveStatistics checkT = new DescriptiveStatistics();
        DescriptiveStatistics checkC = new DescriptiveStatistics();
        DescriptiveStatistics check3 = new DescriptiveStatistics();

        for (Map.Entry<Integer, Double> entry : valueTwitter.entrySet()) {
            int i = entry.getKey();
            double normalT = valueTwitter.get(i) / svT;
            double normalC = valueCrime.get(i) / svC;
            double normal3 = value311.get(i) / sv3;
            checkT.addValue(normalT);
            checkC.addValue(normalC);
            check3.addValue(normal3);
            System.out.println(i + " " + normalT + " " + normalC + " " + normal3);
            indexMap.put(i, normal3 + normalC + normalT);
        }
        System.out.println(checkT.getStandardDeviation() + " " + checkC.getStandardDeviation() + " " + check3.getStandardDeviation());
    }
}
