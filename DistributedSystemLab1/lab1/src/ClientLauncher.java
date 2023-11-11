import java.util.Properties;
import impl.*;
import api.*;
import java.util.Scanner;
import java.io.IOException;
public class ClientLauncher {
    public static void main(String[] args) {
        ClientImpl my_client = new ClientImpl();
        Scanner input = new Scanner(System.in);
        while(true){
            String command = input.next();
            String[] command_args = command.split(" ");

            //退出
            if (command_args[0].equals("exit")){
                System.out.println("bye");
                break;
            }

            //打开文件
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
                        System.out.println("INFO: wrong input");
                        continue;
                }

                int fd = my_client.open(command_args[1], mode);
                if (fd == -1){
                    System.out.println("INFO: OPEN not allowed");
                }
                else {
                    System.out.println("INFO: fd="+ fd);
                }
                continue;
            }

            //读取文件
            if (command_args[0].equals("read")){
                if (command_args.length != 2){
                    System.out.println("INFO: wrong input");
                    continue;
                }

                byte[] file_data = my_client.read(Integer.parseInt(command_args[1]));
                if (file_data == null){
                    System.out.println("INFO: READ not allowed");
                    continue;
                }
                String file_str = new String(file_data);
                System.out.println(file_str);
                continue;
            }

            //插入数据
            if (command_args[0].equals("append")){
                if (command_args.length <= 2){
                    System.out.println("INFO: wrong input");
                    continue;
                }
                int index1 = command.indexOf(" ");
                int index2 = command.indexOf(" ", index1 + 1);
                String append_str = command.substring(index2+1);
                byte[] append_data = append_str.getBytes();
                int info = my_client.append(Integer.parseInt(command_args[1]), append_data);
                if (info == -1){
                    System.out.println("INFO: APPEND not allowed");
                    continue;
                }

                System.out.println("INFO: write done");
                continue;
            }

            //关闭文件
            if (command_args[0].equals("close")){
                if (command_args.length != 2){
                    System.out.println("INFO: wrong input");
                    continue;
                }
                my_client.close(Integer.parseInt(command_args[1]));
                continue;
            }

            System.out.println("INFO: wrong input");
            continue;
        }

    }

}