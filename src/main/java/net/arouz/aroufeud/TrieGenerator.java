package net.arouz.aroufeud;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 *
 * @author arouz
 */
public class TrieGenerator {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        File ordlista = new File("ordlista.txt");
        Trie trie = new Trie();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(ordlista), "UTF8"))) {
            for (String line; (line = br.readLine()) != null;) {
                if (!line.startsWith("#")) {
                    trie.addWord(line.toLowerCase());
                }
            }
        } catch (IOException ex) {
            System.out.println("Ordlista wasn't found.");
        } finally {
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            System.out.println("Trie generated in: " + totalTime + "ms.");
        }
        List list = trie.getWords("aba");
        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i));
        }
    }
}
