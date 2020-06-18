
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class ClientHTTP {

		private String user;
		private String folder;
		
		public ClientHTTP(String u,String f)
		{
			user=u;
			folder=f;
		}
		
		public boolean getResursa(String resursa,String domeniu, String host, int port) throws IOException
		{
			StringBuilder cerere=new StringBuilder();
			cerere.append("GET "+resursa+ "  HTTP/1.1\r\n");
			cerere.append("Host: " + domeniu + "\r\n");
			cerere.append("User-Agent: " + user + "\r\n");
			cerere.append("Connection: close\r\n");
			cerere.append("\r\n");
			String requestHTTP=cerere.toString();
			System.out.println("Cerere HTTP:\n");
	        System.out.println(requestHTTP);
	        
			Socket tcp=new Socket(host, port);
			DataOutputStream out=new DataOutputStream(tcp.getOutputStream());
			BufferedReader in=new BufferedReader(new InputStreamReader(tcp.getInputStream()));
			out.writeBytes(requestHTTP);
			System.out.println("\nCerere trimisa catre: " + host + ".");
			
			String raspuns=in.readLine();
			System.out.println("Raspuns:\n\t"+raspuns+"");
			boolean ok=false;
			if(raspuns.contains("200 OK"))
			{
				ok=true;
			}
			while((raspuns=in.readLine())!=null)
			{
				if(raspuns.equals(""))
				{
					break;
				}
				System.out.println("\t"+raspuns);
				
			}
			if(ok)
			{
				StringBuilder page =new StringBuilder();
				while((raspuns=in.readLine())!=null)
				{
					page.append(raspuns+System.lineSeparator());
				}
				page.toString();
				//System.out.print(page);
				String filePath=folder+"/"+domeniu+resursa+"/index.html";
				File file=new File(filePath);
				File dir=file.getParentFile();
				if(!dir.exists())
				{
					dir.mkdirs();
				}
				BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
	            writer.write(page.toString());
	            writer.close();
	            System.out.println("\nContinutul raspunsului se afla in fisierul \"" + filePath + "\".");
			}
			
			return true;
			
		}
		public static void main(String args[])
	    {
			
	        ClientHTTP httpClient = new ClientHTTP("CLIENT RIW", "./http");

	        String tibeica = "riweb.tibeica.com";
	        
	        try
	        {
	        	
	            httpClient.getResursa("/crawl", tibeica, "riweb.tibeica.com", 80);
	        }
	        catch (IOException ioe)
	        {
	            System.out.println("Eroare socket:");
	            ioe.printStackTrace();
	        }
}
}
