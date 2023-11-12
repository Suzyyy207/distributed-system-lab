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

    private byte[] decodeBytes(byte[] all_bytes){
        int length_info = 0;
        for (byte b : all_bytes) {
            if (b != 0) {
                length_info++;
            } else {
                break;
            }
        }
        byte[] real_bytes = Arrays.copyOfRange(all_bytes, 0, length_info);
        return real_bytes;
    }

    private byte[] parseBytes(byte[] real_bytes){
        byte[] all_bytes = new byte[4*1024];
        for (int i = 0; i<real_bytes.length; i++){
            all_bytes[i] = real_bytes[i];
        }
        return all_bytes;
    }


    public DataNodeImpl(int node_id){
        this.node_id = node_id ;
        this.blocks = new ArrayList<>();

        String folder_path = "../src/data/data_node_"+node_id;
        int txt_count = countTxtFiles(folder_path);
        for (int i = 0; i<txt_count; i++){
            String file_path = folder_path+"/"+i+".txt";
            try {
                FileInputStream fileInputStream = new FileInputStream(file_path);
                BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                long file_size = fileInputStream.getChannel().size();
                byte[] data = new byte[(int) file_size];
                bufferedInputStream.read(data);
                bufferedInputStream.close();
                fileInputStream.close();
                this.blocks.add(data);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    @Override
    public byte[] read(int block_id){
        byte[] data = this.blocks.get(block_id);
        byte[] all_data = parseBytes(data);
        return all_data;
    }

    public void update(int block_id){
        byte[] data = this.blocks.get(block_id);

        String file_path = "../src/data/data_node_"+this.node_id+"/"+block_id+".txt";
        Path path = Paths.get(file_path);
        if (!Files.exists(path)) {
            try{
                Files.createFile(path);
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file_path);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);

            bufferedOutputStream.write(data);

            bufferedOutputStream.close();
            fileOutputStream.close();
        } catch (IOException e) {
            System.err.println("error in block update");
        }

    }

    @Override
    public int append(int block_id, byte[] bytes) {
        byte[] real_bytes = decodeBytes(bytes);
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
            int new_data_len = real_bytes.length;
            int old_data_len = old_data.length;
            byte[] all_data = new byte[new_data_len + old_data_len];
            System.arraycopy(old_data, 0, all_data, 0, old_data_len);
            System.arraycopy(real_bytes, 0, all_data, old_data_len, new_data_len);
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
