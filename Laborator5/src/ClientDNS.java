import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class ClientDNS {
	private String ServerDNS;
	private int port;
	//------------------------------
	public ClientDNS(String s,int p)
	{
		ServerDNS=s;
		port=p;
	}

	void rezolvaCerereDNS(String domeniu) throws SocketException, UnknownHostException,IOException
	{
		byte[] request=new byte[12+domeniu.length()+6];
		Random rand=new Random();
		
		int identificator=rand.nextInt(1<<16-1);
		byte [] MSB_LSB=ByteBuffer.allocate(4).putInt(identificator).array();
		byte MSB=MSB_LSB[2];
		byte LSB=MSB_LSB[3];
		request[0]=MSB;
		request[1]=LSB;
		request[2]=0x01;
		request[5]=0x1;
		int questionIdx=12;
		byte questionBuffer[]=new byte[domeniu.length()];
		int k=0;
		char [] NumeCaractere=domeniu.toCharArray();
		for(int j=0;j<domeniu.length();++j)
		{
			if(NumeCaractere[j]!='.')
			{
				questionBuffer[k++]=(byte)NumeCaractere[j];
			}
			else
			{
				request[questionIdx++]=(byte)k;
				int i;
				for(i=0;i<k;++i)
				{
					request[questionIdx++]=questionBuffer[i];
				}
				k=0;//cautam urmatorul punct
			}
			if(j==domeniu.length()-1)
			{
				request[questionIdx++]=(byte)k;
				int i;
				for(i=0;i<k;++i)
				{
					request[questionIdx++]=questionBuffer[i];
				}
			}		
			
		}
		request[questionIdx]=0x0;
		request[questionIdx+2]=0x1;
		request[questionIdx+4]=0x1;
		
		DatagramSocket datagram=new DatagramSocket();
		try {
			datagram.setSoTimeout(3000);
			InetAddress IP=InetAddress.getByName(ServerDNS);
			DatagramPacket packet=new DatagramPacket(request, request.length,IP,port);
			System.out.println("\nS-a construit pachetul de cerere DNS pentru numele de domeniu \"" + domeniu + "\".");
			int i;
			for(i=0;i<request.length;++i)
			{
                System.out.printf("0x%02X ", request[i]);
                if ((i + 1) % 15 == 0) {
                    System.out.println();
                }
			}
			System.out.println();
			datagram.send(packet);
			System.out.println("\nPachetul a fost trimis catre server-ul " + ServerDNS + ".");
			
			byte[] raspuns=new byte[512];
			DatagramPacket packetRaspuns= new DatagramPacket(raspuns, 512);
			System.out.println("Se asteapta raspuns de la server...");
            datagram.receive(packetRaspuns);
            System.out.println("\nPachetul de raspuns a fost primit cu succes:");
            for ( i = 0; i < raspuns.length; ++i) {
                System.out.print(' ');
                System.out.printf("0x%02X ", raspuns[i]);
                if ((i + 1) % 15 == 0) {
                    System.out.println();
                }
            }
            System.out.println();
            datagram.close();
            LSB=raspuns[1];
            MSB=raspuns[0];
            int identificatorReceptie = (((0xFF) & MSB) << 8) | (0xFF & LSB);
            if(identificator==identificatorReceptie)
            {
            	System.out.println("Identificatorii se potrivesc: " + identificatorReceptie);
            }
            
            if((raspuns[3]&0x0F)==0x00)
            {
            	System.out.println("Code primit ->ok ");
            	
            }
            else
            {
            	int errorCode = raspuns[3] & 0x0F;
                System.out.println("S-a produs o eroare: RCode = " + errorCode);
            }
                

		}
		catch (SocketTimeoutException s)
        {
            System.out.println("Eroare: Server-ul DNS nu a raspuns la timp.");
            return;
        }
        catch (ArrayIndexOutOfBoundsException a) 
        {
            return;
        }		
}
	
	public static void main(String args[])
	{
		String Tuiasi = "81.180.223.1";
		String tuiasi = "www.tuiasi.ro";
		ClientDNS dns=new ClientDNS(Tuiasi, 53);
		try {
			dns.rezolvaCerereDNS(tuiasi);
		}
		catch (SocketException se)
        {
            System.out.println("Eroare socket:");
            se.printStackTrace();
        }
		catch (UnknownHostException uhe)
        {
            System.out.println("Adresa IP invalida pentru server-ul DNS.");
            uhe.printStackTrace();
        }
        catch (IOException ioe)
        {
            System.out.println("Eroare socket:");
            ioe.printStackTrace();
        }
	}
	

}
