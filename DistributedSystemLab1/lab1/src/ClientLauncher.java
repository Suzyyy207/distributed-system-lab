import java.util.Properties;
import impl.*;
import api.*;
import java.util.Scanner;
import java.io.IOException;
public class ClientLauncher {
    private static void open(String[] command_args, ClientImpl my_client){
        if (command_args.length != 3){
            System.out.println("INFO: Open wrong input");
            return;
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
                System.out.println("INFO: Mode wrong input");
                return;
        }

        int fd = my_client.open(command_args[1], mode);
        if (fd == -1){
            System.out.println("INFO: OPEN not allowed");
        }
        else {
            System.out.println("INFO: fd="+ fd);
        }
        return;
    }

    private static void read(String[] command_args, ClientImpl my_client){
        if (command_args.length != 2){
            System.out.println("INFO: Read wrong input");
            return;
        }

        byte[] file_data = my_client.read(Integer.parseInt(command_args[1]));
        if (file_data == null){
            System.out.println("INFO: READ not allowed");
            return;
        }
        String file_str = new String(file_data);
        System.out.println(file_str);
        return;
    }

    private static void append(String command, String[] command_args, ClientImpl my_client){
        if (command_args.length <= 2){
            System.out.println("INFO: Append wrong input");
            return;
        }
        int index1 = command.indexOf(" ");
        int index2 = command.indexOf(" ", index1 + 1);
        String append_str = command.substring(index2+1);
        byte[] append_data = append_str.getBytes();
        int info = my_client.append(Integer.parseInt(command_args[1]), append_data);
        if (info == -1){
            System.out.println("INFO: APPEND not allowed");
            return;
        }

        System.out.println("INFO: write done");
        return;
    }

    private static void close(String[] command_args, ClientImpl my_client){
        if (command_args.length != 2){
            System.out.println("INFO: Close wrong input");
            return;
        }
        my_client.close(Integer.parseInt(command_args[1]));
        return;
    }

    public static void main(String[] args) {
        ClientImpl my_client = new ClientImpl();
        Scanner input = new Scanner(System.in);
        while(true){
            System.out.print(">> ");
            String command = input.nextLine();

            String[] command_args = command.split(" ");
            //System.out.println("test: "+command);
            //退出
            if (command_args[0].equals("exit")){
                System.out.println("bye");
                break;
            }

            //打开文件
            if(command_args[0].equals("open")){
                open(command_args,my_client);
                continue;
            }

            //读取文件
            if (command_args[0].equals("read")){
                read(command_args,my_client);
                continue;
            }

            //插入数据
            if (command_args[0].equals("append")){
                append(command, command_args,my_client);
                continue;
            }

            //关闭文件
            if (command_args[0].equals("close")){
                close(command_args,my_client);
                continue;
            }

            System.out.println("INFO: NO command match");
            continue;
        }

    }

}