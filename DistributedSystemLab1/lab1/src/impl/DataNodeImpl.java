package impl;
//TODO: your implementation
import api.DataNodePOA;
import java.util.Random;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.*;
import utils.*;

public class DataNodeImpl extends DataNodePOA {
    private List<byte[]> blocks;
    private int node_id;

    private static int countTxtFiles(String folder_path) {
        File folder = new File(folder_path);
        if (!folder.exists() || !folder.isDirectory()) {
            return -1;
        }

        File[] files = folder.listFiles();
        int count = 0;
        if (files != null) {
            for (File file : files) {
                // 检查文件是否以.txt为扩展名
                if (file.isFile() && file.getName().toLowerCase().endsWith(".txt")) {
                    count++;
                }
            }
        }
        return count;
    }

    // 读取文件中的字节数据，每一行对应一个字节数组
    private static List<byte[]> readBytesFromFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        return Files.readAllLines(path).stream()
                .map(line -> parseByteArrayFromLine(line))
                .collect(Collectors.toList());
    }

    // 解析字符串为字节数组
    private static byte[] parseByteArrayFromLine(String line) {
        return line.getBytes();
    }

    // 将 List<byte[]> 转换为一个大的 byte[]
    private static byte[] concatenateByteArrays(List<byte[]> byteArrayList) {
        int totalLength = byteArrayList.stream().mapToInt(byteArray -> byteArray.length).sum();
        byte[] result = new byte[totalLength];
        int currentIndex = 0;
        for (byte[] byteArray : byteArrayList) {
            System.arraycopy(byteArray, 0, result, currentIndex, byteArray.length);
            currentIndex += byteArray.length;
        }
        return result;
    }

    public DataNodeImpl(int node_id){
        this.node_id = node_id ;
        this.blocks = new ArrayList<>();

        String folder_path = "../src/data/data_node_"+node_id;
        int txt_count = countTxtFiles(folder_path);
        for (int i = 0; i<txt_count; i++){
            String file_path = folder_path+"/"+i+".txt";
            try (FileReader reader = new FileReader(file_path);
                 BufferedReader br = new BufferedReader(reader)
            ) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] line_str =line.split(" ");
                    byte[] block_data = new byte[line_str.length];
                    for (int j=0; j<line_str.length; j++){
                        byte byte_data = Byte.parseByte(line_str[j]);
                        if (byte_data == 0){
                            break;
                        }
                        else {
                            block_data[i] = byte_data;
                        }
                    }
                    this.blocks.add(block_data);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public byte[] read(int block_id){
        byte[] data = this.blocks.get(block_id);
        System.out.println(data.length);
        return data;
    }

    public void update(int block_id){
        //空位补全
        byte[] data = this.blocks.get(block_id);
        byte[] all_data = new byte[4*1024];
        System.arraycopy(data, 0, all_data, 0, data.length);


        String file_path = "../src/data/data_node_"+this.node_id+"/"+block_id+".txt";
        Path path = Paths.get(file_path);
        if (!Files.exists(path)) {
            try{
                Files.createFile(path);
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file_path))) {
            for (byte d : all_data) {
                writer.write(d);
                writer.write(" ");
            }
        } catch (IOException e) {
            System.err.println("error in block update");
        }

    }

    @Override
    public int append(int block_id, byte[] bytes) {
        int length_info = 0;
        for (byte b : bytes) {
            if (b != 0) {
                length_info++;
            } else {
                break;
            }
        }
        byte[] real_bytes = Arrays.copyOfRange(bytes, 0, length_info);

        int new_allocated_id = -1;

        //针对之前没有分配过块的文件，先roll一个id
        if (block_id == -1){
            byte[] new_block = new byte[0];
            blocks.add(new_block);
            block_id = this.blocks.size() - 1;
            new_allocated_id = block_id;
        }

        //修改块数据
        byte[] old_data = this.blocks.get(block_id);
        int free_space = 4*1024 - old_data.length;

        if (real_bytes.length > free_space){
            //先装满一个块
            byte[] new_data = Arrays.copyOfRange(real_bytes, 0, free_space);
            int old_data_len = old_data.length;
            int new_data_len = new_data.length;
            byte[] all_data = new byte[new_data_len + old_data_len];
            System.arraycopy(old_data, 0, all_data, 0, old_data_len);
            System.arraycopy(new_data, 0, all_data, old_data_len, new_data_len);
            this.blocks.set(block_id,all_data);
            byte[] left_data = new byte[4*1024];
            System.arraycopy(real_bytes, free_space, left_data, 0, real_bytes.length);
            new_allocated_id = this.append(-1, left_data);
        }
        else{
            int new_data_len = bytes.length;
            int old_data_len = old_data.length;
            byte[] all_data = new byte[new_data_len + old_data_len];
            System.arraycopy(old_data, 0, all_data, 0, old_data_len);
            System.arraycopy(bytes, 0, all_data, old_data_len, new_data_len);
            this.blocks.set(block_id,all_data);
        }

        update(block_id);

        return new_allocated_id;
    }

    @Override
    public int randomBlockId() {
        int length = this.blocks.size();
        if (length == 0){
            return -1;
        }

        Random random = new Random();
        int random_id = random.nextInt(length) ;

        return random_id;
    }
}
