package impl;
//TODO: your implementation
import api.DataNodePOA;
import java.util.Random;

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
    public void append(int block_id, byte[] bytes) {
        //针对之前没有分配过块的文件，先roll一个id
        if (block_id == -1){
            byte[] new_block = new byte[];
            blocks.add(new_block);
            block_id = this.blocks.size() - 1;
        }

        //修改块数据
        byte[] block_data = this.blocks.get(block_id);
        int new_data_len = bytes.length;
        int block_len = block_data.length;
        byte[] new_block_data = new byte[new_data_len + block_len];
        System.arraycopy(block_data, 0, new_block_data, 0, block_len);
        for (int i = block_len; i < block_len + new_data_len; i++){
            new_block_data[i] = bytes[i - block_len];
        }

        //更新list
        this.blocks.set(block_id,new_block_data);

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
    }

    @Override
    public int randomBlockId() {
        int length = this.blocks.size();
        Random random = new Random();
        int random_id = random.nextInt(length) ;

        return random_id;
    }
}
