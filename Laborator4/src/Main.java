import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.*;

public class Main {

		
		public static void main(String[] args) throws IOException{
			
			Laborator4 lab4 = new Laborator4("E:\\Eclipse\\New folder\\Laborator4\\stackoverflow/", "https://stackoverflow.com/");
			
			System.out.print("Creare index direct...\n");
			
			HashMap<String, HashMap<String, Integer>> directIndex = lab4.directIndex();
			System.out.print("-------------------------------------------------------------------------\n");
			System.out.print("Creare index indirect...\n\n");
	        TreeMap<String, HashMap<String, Integer>> indirectIndex = lab4.indirectIndex();
	        System.out.print("-------------------------------------------------------------------------\n");
			
	        System.out.print("Aplicare cautare booleana\nIntroduceti ce vreti sa cautati:\n");
	        Scanner sc=new Scanner(System.in);
	        String interog=sc.nextLine();
	        System.out.print("-------------------------------------------------------------------------\n");
	        System.out.println("Incepe cautarea.");
	        Set<String>rez=Cautare_Booleana.Cautare_booleana(interog, indirectIndex);
	        if(rez==null)
	        {
	        	System.out.println("Nu s a gasit niciun rezultat\n");
	        }
	        else
	        {
	        	System.out.println("Rezultatele cautarii sunt:\n");
	        	for(String doc:rez)
	        	{
	        		System.out.print("\t"+doc+"\n");
	        	}
	        }
		}

} 
