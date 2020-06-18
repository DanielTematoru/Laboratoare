import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        Laborator2 l2 = new Laborator2("./stackoverflow/", "https://stackoverflow.com/");
        l2.getWordsFromTextFiles();
    }
}
