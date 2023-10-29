package impl;
//TODO: your implementation
import api.DataNodePOA;
import java.util.Random;

public class DataNodeImpl extends DataNodePOA {
    private List<byte[]> blocks;

    public DataNodeImpl(node_id){
        this.blocks = new ArrayList<>();

        try (FileInputStream file = new FileInputStream("../DataNodeFile/DataNode"+node_id+".dat")) {
            //todo：不固定大小，怎么读呢？？？标记位？？
            int bytesRead = 4*1024;
            byte[] block = new byte[bytesRead];

            // 从文件中读取数据到buffer，直到文件末尾
            while ((bytesRead = file.read(buffer)) != -1) {
                // 创建新的byte数组，将读取的数据放入其中
                byte[] block = new byte[bytesRead];
                System.arraycopy(buffer, 0, block, 0, bytesRead);

                // 将octet数组添加到blocks列表中
                this.blocks.add(block);
            }

            System.out.println("DataNode "+node_id+" read done");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("DataNoe"+node_id+" read error");
        }

    }

    @Override
    public byte[] read(int block_id){
        //todo: 语法可能不对，要结合前面的改
        byte[] data = this.blocks.get(block_id);
        return data;
    }

    @Override
    public void append(int block_id, byte[] bytes) {
        //todo: 结尾加了识别符的话，要修改
        byte [] block_data = this.blocks.get(block_id);
        int new_data_len = bytes.length;
        int block_len = block_data.length;

        byte [] new_block_data = new byte[new_data_len + block_len];
        System.arraycopy(block_data, 0, new_block_data, 0, block_len);
        for (int i = block_len; i < block_len + new_data_len; i++){
            new_block_data[i] = bytes[i - block_len];
        }
    }

    @Override
    public int randomBlockId() {
        int min = 0;
        int max = this.blocks.size();

        // 创建一个Random对象
        Random random = new Random();

        // 生成在指定范围内的随机整数
        int random_id = random.nextInt(max - min + 1) + min;

        return random_id;
    }
}
