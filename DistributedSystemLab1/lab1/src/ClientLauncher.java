import com.sun.corba.se.spi.orb.ORB;
import java.util.Properties;
import impl.*;
import api.*;
import java.io.IOException;
public class ClientLauncher {
    public static void main(String[] args) {
        ClientImpl my_client = new ClientImpl();
        Scanner input = new Scanner(System.in);
        while(1){
            String command = input.next();
            String[] command_args = command.spilt(" ");
            if (command_args[0].equals("exit")){
                System.out.println("bye");
                break;
            }

            if(command_args[0].equals("open")){
                if (command_args.length != 3){
                    System.out.println("wrong input");
                    continue;
                }

                int mode;
                switch (command_args[2]){
                    case "w":
                       mode = 0b10;
                       break;
                    case "r":
                        mode = 0b01;
                        break;
                    case "rw":
                        mode = 0b11;
                        break;
                    default:
                        System.out.println("wrong input");
                        continue;
                }

                continue;
            }
        }

    }

}