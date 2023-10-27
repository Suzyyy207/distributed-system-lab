package impl;
//TODO: your implementation
import api.DataNodePOA;

public class DataNodeImpl extends DataNodePOA {
    private List<octet> blocks;

    @Override
    public byte[] read(int block_id){
        octet data;
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
