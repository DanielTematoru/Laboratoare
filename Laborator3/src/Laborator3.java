import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.*;

public class Laborator3 {
	   private String websiteFolder;
	   private String baseUri;
	   
	   Laborator3(String websiteFolder, String baseUri)
	    {
	        this.websiteFolder = websiteFolder;
	        this.baseUri = baseUri;
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

	            // cautam eventuale ancore in link-uri
	            int anchorPosition = absoluteLink.indexOf('#');
	            if (anchorPosition != -1) // daca exista o ancora (un #)
	            {
	                // stergem partea cu ancora din link
	                StringBuilder tempLink = new StringBuilder(absoluteLink);
	                tempLink.replace(anchorPosition, tempLink.length(), "");
	                absoluteLink = tempLink.toString();
	            }

	            // nu vrem sa adaugam duplicate, asa incat folosim o colectie de tip Set
	            URLs.add(absoluteLink);
	        }
	        // System.out.println("Link-urile de pe site au fost preluate!");
	        return URLs;
	    }

	    private File getTextFromHTML(Document doc, File html) throws IOException // preia textul din document si il pune intr-un fisier
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

	        // adaugam extensia txt
	        textFileNameBuilder.append(".txt");
	        /*
	        // fisierele HTML ce contin "?" in nume vor primi extensia ".txt" alaturi de intregul nume
	        if (textFileNameBuilder.indexOf("?") != -1)
	        {
	            textFileNameBuilder.append(".txt");
	        }
	        else // daca nu, inlocuim extensia de dupa "." cu "txt"
	        {
	            textFileNameBuilder.replace(textFileNameBuilder.lastIndexOf(".") + 1, textFileNameBuilder.length(), "txt");
	        }
	        */
	        String textFileName = textFileNameBuilder.toString();

	        // scriem rezultatul in fisierul text
	        FileWriter fw = new FileWriter(new File(textFileName), false);
	        fw.write(text);
	        fw.close();

	        return new File(textFileName);
	    }
	    
	    private HashMap<String,Integer> processText(String fileName) throws IOException
	    {
	    	HashMap<String,Integer> wordList=new HashMap<String,Integer>();
	    	FileReader inputStream = null;
	        inputStream = new FileReader(fileName);
	        
	        StringBuilder sb=new StringBuilder();
	        int c;
	        while ((c = inputStream.read()) != -1)
	        {
	            
	            if (!Character.isLetterOrDigit((char)c)) // suntem pe un separator
	            {
	                String newWord = sb.toString(); // cream cuvantul nou

	              
	                if (ExceptionList.exceptions.contains(newWord))
	                {
	                    
	                    if (wordList.containsKey(newWord)) 
	                    {
	                        wordList.put(newWord, wordList.get(newWord) + 1); 
	                    } else 
	                    {
	                        wordList.put(newWord, 1);
	                    }
	                }
	               
	                else if (StopWordList.stopwords.contains(newWord))
	                {
	                   
	                    sb.setLength(0);
	                    continue;
	                }
	                else 
	                {
	                    
	                    if (wordList.containsKey(newWord)) 
	                    {
	                        wordList.put(newWord, wordList.get(newWord) + 1); 
	                    } else // daca nu, il adaugam
	                    {
	                        wordList.put(newWord, 1);
	                    }
	                }

	                // System.out.println(newWord + " -> " + hashText.get(newWord));
	                sb.setLength(0); 
	            }
	            else // suntem in mijlocul unui cuvant
	            {
	                sb.append((char)c); 
	            }
	        }

	        
	        wordList.remove("");

	        
	        StringBuilder sbDirectIndexFileName = new StringBuilder(fileName);

	        
	        sbDirectIndexFileName.replace(sbDirectIndexFileName.lastIndexOf(".") + 1, sbDirectIndexFileName.length(), "directindex.json");
	        Writer writer = new BufferedWriter(new OutputStreamWriter(
	                new FileOutputStream(sbDirectIndexFileName.toString()), "utf-8"));

	        Gson gsonBuilder = new GsonBuilder().setPrettyPrinting().create();
	        String jsonFile = gsonBuilder.toJson(wordList);

	        writer.write(jsonFile);
	        writer.close();

	        inputStream.close();
	        // System.out.println("Cuvintele din textul de pe site au fost prelucrate!");

	        return wordList;
	    }
	    
	    public HashMap<String, HashMap<String, Integer>> directIndex() throws IOException
	    {
	        HashMap<String, HashMap<String, Integer>> directIndex = new HashMap<>();
	        Gson gsonBuilder = new GsonBuilder().setPrettyPrinting().create();
	        Writer mapFileWriter = new BufferedWriter(new OutputStreamWriter(
	        new FileOutputStream(websiteFolder + "directindex.idx"), "utf-8"));
	        HashMap<String, String> mapFile = new HashMap<>();

	        LinkedList<String> folderQueue = new LinkedList<>();

	        folderQueue.add(websiteFolder);

	        while (!folderQueue.isEmpty()) 
	        {
	           
	            String currentFolder = folderQueue.pop();
	            File folder = new File(currentFolder);
	            File[] listOfFiles = folder.listFiles();
	            try {
	                for (File file : listOfFiles)
	                {
	                    
	                    if (file.isFile() && Files.probeContentType(file.toPath()).equals("text/html"))
	                    {
	                       
	                        Document doc = Jsoup.parse(file, null, baseUri);
	                        String fileName = file.getAbsolutePath();
	                        System.out.println("Am parsat fisierul HTML \"" + fileName + "\".");

	                        // stocam textul in fisier separat
	                        File textFile = getTextFromHTML(doc, file);
	                        String textFileName = textFile.getAbsolutePath();
	                        System.out.println(" Am preluat textul din fisierul HTML \"" + fileName + "\".");

	                        // procesam cuvintele, rezultand un HashMap de tip (cuvant -> numar_aparitii)
	                        // stocam index-ul direct intr-un fisier cu extensia ".directindex"
	                        HashMap<String, Integer> currentDocWords = processText(textFileName);

	                        
	                        directIndex.put(fileName, currentDocWords);

	                       
	                        mapFile.put(fileName, fileName + ".directindex.json");

	                        System.out.println(" Am creat index-ul direct din fisierul TEXT \"" + textFileName + "\".");
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

	    
	        mapFileWriter.write(gsonBuilder.toJson(mapFile));
	        mapFileWriter.close();
	        System.out.println(System.lineSeparator());
	        System.out.println(" Am creat fisierul de mapare \"" + websiteFolder + "directindex.idx\".");

	        return directIndex;
	    }
	    
	    public TreeMap<String, HashMap<String, Integer>> indirectIndex() throws IOException
	    {
	        TreeMap<String, HashMap<String, Integer>> indirectIndex = new TreeMap<>();
	        Gson gsonBuilder = new GsonBuilder().setPrettyPrinting().create();

	        // pentru obtinerea fisierului de mapare, numit "indirectindex.map", aflat in directorul radacina
	        Writer mapFileWriter = new BufferedWriter(new OutputStreamWriter(
	                new FileOutputStream(websiteFolder + "indirectindex.map"), "utf-8"));
	        HashMap<String, String> mapFile = new HashMap<>();

	        // pentru parcurgerea directoarelor, folosim o coada
	        LinkedList<String> folderQueue = new LinkedList<>();

	        // pornim cu folder-ul radacina
	        folderQueue.add(websiteFolder);

	        while (!folderQueue.isEmpty()) // cat timp nu mai sunt foldere copil de parcurs
	        {
	            // preluam un folder din coada
	            String currentFolder = folderQueue.pop();
	            File folder = new File(currentFolder);
	            File[] listOfFiles = folder.listFiles();

	            // ii parcurgem lista de fisiere / foldere
	            try {
	                for (File file : listOfFiles)
	                {
	                    // daca am ajuns pe un fisier, verificam sa fie fisier de tip index direct, creat anterior
	                    if (file.isFile() && file.getAbsolutePath().endsWith(".directindex.json"))
	                    {
	                        String fileName = file.getAbsolutePath();
	                        String docName = fileName.replace(".directindex.json", "");

	                        // preluam fisierul JSON cu indexul direct si il parsam
	                        Type directIndexType = new TypeToken<HashMap<String, Integer>>(){}.getType();
	                        HashMap<String, Integer> directIndex = gsonBuilder.fromJson(new String(Files.readAllBytes(file.toPath())), directIndexType);
	                        System.out.println(" Am parsat fisierul JSON \"" + fileName + "\".");

	                        // stocam indexul indirect al fisierului curent
	                        TreeMap<String, HashMap<String, Integer>> localIndirectIndex = new TreeMap<>();
	                        for(Map.Entry<String, Integer> entry : directIndex.entrySet()) // luam fiecare cuvant si stocam numarul de aparitii si documentul din care face parte
	                        {
	                            String word = entry.getKey();
	                            int numberOfApparitions = entry.getValue();

	                            // adaugam intrarea in TreeMap-ul local
	                            if (localIndirectIndex.containsKey(word)) // daca acel cuvant exista in TreeMap
	                            {
	                                // il adaugam in vectorul de aparitii
	                                HashMap<String, Integer> apparitions = localIndirectIndex.get(word);
	                                apparitions.put(docName, numberOfApparitions);
	                                // localIndirectIndex.put(word, numberOfApparitions);
	                            }
	                            else
	                            {
	                                HashMap<String, Integer> apparitions = new HashMap<>();
	                                apparitions.put(docName, numberOfApparitions);
	                                localIndirectIndex.put(word, apparitions);
	                            }

	                            // adaugam intrarea in TreeMap-ul final
	                            if (indirectIndex.containsKey(word)) // daca acel cuvant exista in TreeMap
	                            {
	                                // il adaugam in vectorul de aparitii
	                                HashMap<String, Integer> apparitions = indirectIndex.get(word);
	                                apparitions.put(docName, numberOfApparitions);
	                                // indirectIndex.put(word, numberOfApparitions);
	                            }
	                            else
	                            {
	                                HashMap<String, Integer> apparitions = new HashMap<>();
	                                apparitions.put(docName, numberOfApparitions);
	                                indirectIndex.put(word, apparitions);
	                            }
	                        }

	                        // scriem fisierul JSON cu index-ul indirect
	                        Writer writer = new BufferedWriter(new OutputStreamWriter(
	                                new FileOutputStream(docName + ".indirectindex.json"), "utf-8"));
	                        writer.write(gsonBuilder.toJson(localIndirectIndex));
	                        writer.close();

	                        System.out.println(" Am creat index-ul indirect in fisierul JSON \"" + docName + ".indirectindex.json\".");

	                        // adaugam documentul curent, impreuna cu index-ul indirect asociat in fisierul de mapare
	                        mapFile.put(docName, docName + ".indirectindex.json");
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

	        // cream fisierul concatenat de index indirect
	        Writer indirectIndexWriter = new BufferedWriter(new OutputStreamWriter(
	                new FileOutputStream(websiteFolder + "indirectindex.json"), "utf-8"));
	        indirectIndexWriter.write(gsonBuilder.toJson(indirectIndex));
	        indirectIndexWriter.close();

	        System.out.println(System.lineSeparator());
	        System.out.println("Am creat fisierul de index indirect \"" + websiteFolder + "indirectindex.json\".");

	        // scriem fisierul de mapare cu string-ul JSON creat
	        mapFileWriter.write(gsonBuilder.toJson(mapFile));
	        mapFileWriter.close();
	        System.out.println(" Am creat fisierul de mapare \"" + websiteFolder + "indirectindex.map\".");

	        return indirectIndex;
	    }
	       
	   

}
