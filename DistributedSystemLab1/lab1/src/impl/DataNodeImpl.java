package impl;
//TODO: your implementation
import api.DataNodePOA;
import java.util.Random;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import utils.*;

public class DataNodeImpl extends DataNodePOA {
    private List<byte[]> blocks;
    private int node_id;
    public DataNodeImpl(int node_id){
        this.node_id = node_id;
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
        }
    }

    @Override
    public byte[] read(int block_id){
        byte[] data = this.blocks.get(block_id);
        return data;
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

        //更新本地文件
        List<String> base64_list = new ArrayList<>();
        for (byte[]  block: this.blocks) {
            String base64_string = Base64.getEncoder().encodeToString(block);
            base64_list.add(base64_string);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(new File("../data/data_node_"+node_id+".json"), base64_list);
            System.out.println("data_node update!");
        } catch (IOException e) {
            e.printStackTrace();
        }

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
