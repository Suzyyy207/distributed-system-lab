package impl;
//TODO: your implementation
import api.DataNodePOA;

public class DataNodeImpl extends DataNodePOA {
    private List<byteArray> blocks;

    public DataNodeImpl(node_id){
        this.blocks = new ArrayList<>();

        try (FileInputStream file = new FileInputStream("../DataNodeFile/DataNode"+node_id+".dat")) {
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
        byteArray data;
        data = blocks.get(block_id);
        return data;
    }

    @Override
    public void append(int block_id, byte[] bytes) {

    }

    @Override
    public int randomBlockId() {

        return 0;
    }
}
