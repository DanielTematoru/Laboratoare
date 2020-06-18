import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class Laborator2 {
    private String websiteFolder;
    private String baseUri;
    private boolean HTMLFilesParsed;

    Laborator2(String websiteFolder, String baseUri) throws IOException
    {
        this.websiteFolder = websiteFolder;
        this.baseUri = baseUri;
        HTMLFilesParsed = false;
    }

    private String getTitle(Document doc) // preia titlul documentului
    {
        String title = doc.title();
        // System.out.println("Titlul site-ului: " + title);
        return title;
    }

    private String getKeywords(Document doc) // preia cuvintele cheie
    {
        Element keywords = doc.selectFirst("meta[name=keywords]");
        String keywordsString = "";
        if (keywords == null) {
            // System.out.println("Nu exista tag-ul <meta name=\"keywords\">!");
        } else {
            keywordsString = keywords.attr("content");
            // System.out.println("Cuvintele cheie au fost preluate!");
        }
        return keywordsString;
    }

    private String getDescription(Document doc) // preia descrierea site-ului
    {
        Element description = doc.selectFirst("meta[name=description]");
        String descriptionString = "";
        if (description == null) {
            // System.out.println("Nu exista tag-ul <meta name=\"description\">!");
        } else {
            descriptionString = description.attr("content");
            // System.out.println("Descrierea site-ului a fost preluata!");
        }
        return descriptionString;
    }

    private String getRobots(Document doc) // preia lista de robots
    {
        Element robots = doc.selectFirst("meta[name=robots]");
        String robotsString = "";
        if (robots == null) {
            System.out.println("Nu exista tag-ul <meta name=\"robots\">!");
        } else {
            robotsString = robots.attr("content");
            // System.out.println("Lista de robots a site-ului a fost preluata!");
        }
        return robotsString;
    }

    private Set<String> getLinks(Document doc) throws IOException // preia link-urile de pe site (ancorele)
    {
        Elements links = doc.select("a[href]");
        Set<String> URLs = new HashSet<String>();
        for (Element link : links) {
            String absoluteLink = link.attr("abs:href"); // facem link-urile relative sa fie absolute
            if (absoluteLink.contains(baseUri)) // ignoram legaturile interne
            {
                continue;
            }

            
            int anchorPosition = absoluteLink.indexOf('#');
            if (anchorPosition != -1) // daca exista o ancora (un #)
            {
                
                StringBuilder tempLink = new StringBuilder(absoluteLink);
                tempLink.replace(anchorPosition, tempLink.length(), "");
                absoluteLink = tempLink.toString();
            }

            
            URLs.add(absoluteLink);
        }
        // System.out.println("Link-urile de pe site au fost preluate!");
        return URLs;
    }

    private String getTextFromHTML(Document doc, File html) throws IOException // preia textul din document si il pune intr-un fisier
    {
        StringBuilder sb = new StringBuilder();
        sb.append(getTitle(doc)); // titlul
        sb.append(System.lineSeparator());
        sb.append(getKeywords(doc)); // cuvintele cheie
        sb.append(System.lineSeparator());
        sb.append(getDescription(doc));
        sb.append(System.lineSeparator());
        sb.append(doc.body().text());
        String text = sb.toString();

        // generam numere fisierului text corespunzator, cu extensia txt
        StringBuilder textFileNameBuilder = new StringBuilder(html.getAbsolutePath());        
        String textFileName = textFileNameBuilder.toString();

        FileWriter fw = new FileWriter(new File(textFileName), false);
        fw.write(text);
        fw.close();

        return textFileName;
    }

    // preia textul din fisier, caracter cu caracter si returneaza lista de cuvinte
    private List<String> processText(String fileName) throws IOException
    {
        List<String> wordList = new LinkedList<>();
       
        FileReader inputStream = null;
        inputStream = new FileReader(fileName);

       
        StringBuilder sb = new StringBuilder();
        int c; // caracterul curent
        while ((c = inputStream.read()) != -1)
        {
            
            if (!Character.isLetterOrDigit((char)c)) // suntem pe un separator
            {
                String newWord = sb.toString(); // cream cuvantul nou

               
                if (ExceptionList.exceptions.contains(newWord))
                {
                    
                    if (!wordList.contains(newWord))
                    {
                        wordList.add(newWord);
                    }
                }
                
                else if (StopWordList.stopwords.contains(newWord))
                {
                    // il ignoram
                    sb.setLength(0);
                    continue;
                }
                else {
                    if (!wordList.contains(newWord))
                    {
                        wordList.add(newWord);
                    }
                }

                // System.out.println(newWord + " -> " + hashText.get(newWord));
                sb.setLength(0); // curatam StringBuilder-ul
            }
            else // suntem in mijlocul unui cuvant
            {
                sb.append((char)c); // adaugam litera curenta la cuvantul ce se creeaza
            }
        }

        // eliminam cuvantul vid
        wordList.remove("");

        
        Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(fileName + ".words"), "utf-8"));
        for (String word : wordList)
        {
            writer.write(word + "\n");
        }
        writer.close();

        inputStream.close();
        // System.out.println("Cuvintele din textul de pe site au fost prelucrate!");

        return wordList;
    }

    // cauta fisierele HTML din website si extrage textul din ele
    public void getTextFromHTMLFiles() throws IOException
    {
        // pentru parcurgerea directoarelor, folosim o coada
        LinkedList<String> folderQueue = new LinkedList<>();

        // pornim cu folder-ul radacina
        folderQueue.add(websiteFolder);

        while (!folderQueue.isEmpty()) // cat timp nu mai sunt foldere copil de parcurs
        {
            
            String currentFolder = folderQueue.pop();
            File folder = new File(currentFolder);
            File[] listOfFiles = folder.listFiles();

            try {
                for (int i = 0; i < listOfFiles.length; i++)
                {
                    File file = listOfFiles[i];

                   
                    if (file.isFile() && Files.probeContentType(file.toPath()).equals("text/html"))
                    {
                       
                        Document doc = Jsoup.parse(file, null, baseUri);

                        
                        getTextFromHTML(doc, file);

                        System.out.println("Am procesat fisierul HTML \"" + file.getAbsolutePath() + "\".");
                    }
                    else if (file.isDirectory()) // daca este folder, il punem in coada
                    {
                        folderQueue.add(file.getAbsolutePath());
                    }
                }
            } catch (NullPointerException e) {
                System.out.println("Nu exista fisiere in folderul \"" + currentFolder + "\"!");
            }
        }

        HTMLFilesParsed = true;
    }

    // parseaza fisierele text rezultate de dupa extragerea din HTML-uri si extrage si prelucreaza cuvintele
    public void getWordsFromTextFiles() throws IOException
    {
        if (!HTMLFilesParsed)
        {
            getTextFromHTMLFiles();
        }

        LinkedList<String> folderQueue = new LinkedList<>();

        folderQueue.add(websiteFolder);

        while (!folderQueue.isEmpty()) // cat timp nu mai sunt foldere copil de parcurs
        {
            // preluam un folder din coada
            String currentFolder = folderQueue.pop();
            File folder = new File(currentFolder);
            File[] listOfFiles = folder.listFiles();

            // ii parcurgem lista de fisiere / foldere
            try {
                for (int i = 0; i < listOfFiles.length; i++)
                {
                    File file = listOfFiles[i];

                    if (file.isFile() && file.getAbsolutePath().endsWith(".txt"))
                    {
                        processText(file.getAbsolutePath());
                        System.out.println("Am procesat fisierul TEXT \"" + file.getAbsolutePath() + "\".");
                    }
                    else if (file.isDirectory()) // daca este folder, il punem in coada
                    {
                        folderQueue.add(file.getAbsolutePath());
                    }
                }
            } catch (NullPointerException e) {
                System.out.println("Nu exista fisiere in folderul \"" + currentFolder + "\"!");
            }
        }
    }
}
