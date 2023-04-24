package hkust.edu.visualneo.utils.graphprocessor;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/*
* GraphLabelReplacer will load the label maps from node_label_map.txt and relation_label_map.txt
* Then, it replaces all the numbers in final_patterns.txt with corresponding label texts
* The output will be visualneo_patterns_file.txt
* visualneo_patterns_file.txt is the final file we are going to feed into VisualNeo
*
* */
public class GraphLabelReplacer {
    private static final String PREFIX = "src/main/resources/hkust/edu/visualneo/data/worldcup/";
    private static final String INPUT_NODE_LABEL_NAME = "node_label_map.txt";
    private static final String INPUT_RELATION_LABEL_NAME = "relation_label_map.txt";
    private static final String INPUT_PATTERN_NAME = "final_patterns.txt";
    private static final String OUTPUT_NAME = "visualneo_patterns_file.txt";
    private static final String VISUALNEO_PATTERN = PREFIX + OUTPUT_NAME;
    private static final String NODE_LABEL_MAP = PREFIX + INPUT_NODE_LABEL_NAME;
    private static final String RELATION_LABEL_MAP = PREFIX + INPUT_RELATION_LABEL_NAME;
    private static final String FILE_TO_BE_REPLACED = PREFIX + INPUT_PATTERN_NAME;

    public static void replaceLabel() {
        try {
            List<String> node_labels = new ArrayList<>();
            List<String> relation_labels = new ArrayList<>();

            // Read the label maps
            File node_label_map = new File(NODE_LABEL_MAP);
            Scanner sc_node = new Scanner(node_label_map);
            List<String> node_labels_text = new ArrayList<>();
            while (sc_node.hasNextLine()) {
                String line = sc_node.nextLine();
                if (!line.isEmpty()) node_labels_text.add(line.trim());
            }
            for (String node_label : node_labels_text)
                node_labels.add(node_label.split(":")[1]);

            File node_relation_map = new File(RELATION_LABEL_MAP);
            Scanner sc_relation = new Scanner(node_relation_map);
            List<String> relation_labels_text = new ArrayList<>();
            while (sc_relation.hasNextLine()) {
                String line = sc_relation.nextLine();
                if (!line.isEmpty()) relation_labels_text.add(line.trim());
            }
            for (String relation_label : relation_labels_text)
                relation_labels.add(relation_label.split(":")[1]);

            // Read the final_pattern.txt
            File patterns_to_be_replaced = new File(FILE_TO_BE_REPLACED);
            Scanner sc_pattern = new Scanner(patterns_to_be_replaced);
            List<String> patterns_to_be_replaced_text = new ArrayList<>();
            while (sc_pattern.hasNextLine()) {
                String line = sc_pattern.nextLine();
                if (!line.isEmpty()) patterns_to_be_replaced_text.add(line.trim());
            }
            try {
                // Write the output file
                File writeFile = new File(VISUALNEO_PATTERN);
                FileWriter myWriter = new FileWriter(writeFile);

                for (String line : patterns_to_be_replaced_text) {
                    if (line.substring(0, 5).equals("Final")) {
                        myWriter.write(line + '\n');
                        continue;
                    }
                    String remaining = line.substring(0, line.length() - 1);
                    int label_id = Integer.parseInt(line.substring(line.length() - 1, line.length()));
                    if (line.substring(0, 1).equals("v"))
                        myWriter.write(remaining + node_labels.get(label_id) + '\n');
                    else
                        myWriter.write(remaining + relation_labels.get(label_id) + '\n');
                }

                myWriter.close();
                System.out.println("Successfully wrote to " + OUTPUT_NAME);

            } catch (Exception e) {
                System.out.println("Error when writing to " + OUTPUT_NAME);
                e.printStackTrace();
            }

        } catch (Exception e) {
            System.out.println("Error when reading label maps");
            e.printStackTrace();
        }
    }
}
