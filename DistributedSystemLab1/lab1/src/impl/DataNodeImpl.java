package impl;
//TODO: your implementation
import api.DataNodePOA;
import java.util.Random;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileReader;
import java.io.File;
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
                .toList();
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
        this.node_id = node_id;
        String folder_path = "../data/data_node_"+node_id;
        int txt_count = countTxtFiles(folder_path);
        for (int i = 0; i<txt_count; i++){
            String file_path = folder_path+"/"+i+".txt";
            try {
                List<byte[]> byte_array_list = readBytesFromFile(file_path);
                byte[] byte_array = concatenateByteArrays(byte_array_list);
                this.blocks.add(byte_array);
            } catch (IOException e) {
                System.err.println("error in read block");
            }
        }

        /*
        //读json文件
        ObjectMapper objectMapper = new ObjectMapper();
        List<String> base64_list = new ArrayList<>();
        try {
            base64_list = objectMapper.readValue(new File("../data/data_node_"+node_id+".json"), new ArrayList<String>().getClass());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 将Base64编码的字符串解码为byte[]数组，并存储到List<byte[]>对象中
        this.blocks = new ArrayList<>();
        for (String base64_string : base64_list) {
            byte[] block = Base64.getDecoder().decode(base64_string);
            blocks.add(block);
        }*/
    }

    @Override
    public byte[] read(int block_id){
        byte[] data = this.blocks.get(block_id);
        return data;
    }

    public void update(int block_id){
        byte[] data = this.blocks.get(block_id);
        String file_path = "../data/data_node_"+this.node_id+"/"+block_id+".txt";
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            Files.createFile(path);
        }
        for (byte b : data) {
            Files.writeString(path, String.valueOf(b) + System.lineSeparator(),
                    java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
        }
    }

    @Override
    public int append(int block_id, byte[] bytes) {
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

        if (bytes.length > free_space){
            //先装满一个块
            byte[] new_data = Arrays.copyOfRange(bytes, 0, free_space);
            int old_data_len = old_data.length;
            int new_data_len = new_data.length;
            byte[] all_data = new byte[new_data_len + old_data_len];
            System.arraycopy(old_data, 0, all_data, 0, old_data_len);
            System.arraycopy(new_data, 0, all_data, old_data_len, new_data_len);
            this.blocks.set(block_id,all_data);

            //剩下的装进新的块
            byte[] left_data = Arrays.copyOfRange(bytes, free_space, bytes.length);
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
