import java.io.IOException;
import java.util.HashMap;
import java.util.TreeMap;

public class Main {
    public static void main(String[] args) throws IOException {
        Laborator3 lab3 = new Laborator3("E:\\Eclipse\\New folder\\Laborator3\\stackoverflow/", "https://stackoverflow.com/");
        HashMap<String, HashMap<String, Integer>> directIndex = lab3.directIndex();
        TreeMap<String, HashMap<String, Integer>> indirectIndex = lab3.indirectIndex();
        
    }
}
