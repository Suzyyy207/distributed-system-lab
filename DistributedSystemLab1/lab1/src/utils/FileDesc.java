package utils;

//TODO: According to your design, complete the FileDesc class, which wraps the information returned by NameNode open()
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class FileDesc {
    /* the id should be assigned uniquely during the lifetime of NameNode,
     * so that NameNode can know which client's open has over at close
     * e.g., on nameNode1
     * client1 opened file "Hello.txt" with mode 'w' , and retrieved a FileDesc with 0x889
     * client2 tries opening the same file "Hello.txt" with mode 'w' , and since the 0x889 is not closed yet, the return
     * value of open() is null.
     * after a while client1 call close() with the FileDesc of id 0x889.
     * client2 tries again and get a new FileDesc with a new id 0x88a
     */
    final int id;    //每个文件的id
    String filepath;
    int mode;
    int size; // 文件大小
    int create_time; // 创建时间
    int modified_time; // 最后修改时间
    int access_time; // 最后访问时间
    int data_node;
    List<Integer> block_id;//文件数据块所在的dn与block id，采取一一对应关系
    //List<int> data_nodes; // 文件的数据块所在的DataNode列表
    //List<int> block_ids; //记录所在的block id


    public FileDesc(){
        this.id = -1;
        this.mode = 0b00;
        this.size = 0;
        this.create_time = 0;
        this.modified_time = 0;
        this.access_time = 0;
        this.filepath = "";
        this.data_node = -1;
        List<Integer> block_id = new ArrayList<>();
    }
    public FileDesc(int id, String filepath, int mode, int size, int create_time, int modified_time,
                    int access_time, int data_node, List<Integer> block_id) {
        this.id = id;
        this.mode = mode;
        this.size = size;
        this.create_time = create_time;
        this.modified_time = modified_time;
        this.access_time = access_time;
        this.filepath = filepath;
        this.data_node = data_node;
        this.block_id = new ArrayList<>();
        this.block_id.addAll(block_id);
    }

    public int getMode() {
        return mode;
    }

    public int getId() {
        return id;
    }

    public int getData_node() {
        return data_node;
    }

    public List<Integer> getBlock_id() {
        return block_id;
    }

    public String getFilepath() {
        return filepath;
    }

    public int getSize(){
        return size;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public void setAccess_time(int access_time) {
        this.access_time = access_time;
    }

    public void setModified_time(int modified_time) {
        this.modified_time = modified_time;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setCreate_time(int create_time) {
        this.create_time = create_time;
    }

    public void setFilepath(String filepath){
        this.filepath = filepath;
    }

    public void addBlockID(int new_block_id){
        if (new_block_id == -1){
            return;
        }
        int first = this.block_id.get(0);
        if (first == -1){
            this.block_id.remove(0);
        }
        this.block_id.add(new_block_id);
        return;
    }

    /* The following method is for conversion,
                                    so we can have interface that return string, which is easy to write in idl */
    //这两个函数在干什么？？？
    @Override
    public String toString() {
        String metadata = "";
        metadata += Integer.toString(this.id)+" ";
        metadata += this.filepath +" ";
        metadata += Integer.toString(this.mode)+" ";
        metadata += Integer.toString(this.size)+" ";
        metadata += Integer.toString(this.create_time)+" ";
        metadata += Integer.toString(this.modified_time)+" ";
        metadata += Integer.toString(this.access_time)+" ";
        metadata += Integer.toString(this.data_node)+" ";
        for(int bid: this.block_id){
            metadata += Integer.toString(bid)+" ";
        }

        return metadata;
    }

    public static FileDesc fromString(String str){
        //静态 所以调用构造函数形成新对象
        String[] metadata = str.split(" ");
        int id = Integer.parseInt(metadata[0]);
        String filepath = metadata[1];
        int mode = Integer.parseInt(metadata[2]);
        int size = Integer.parseInt(metadata[3]);
        int create_time = Integer.parseInt(metadata[4]);
        int modified_time = Integer.parseInt(metadata[5]);
        int access_time = Integer.parseInt(metadata[6]);
        int data_node = Integer.parseInt(metadata[7]);
        List<Integer> block_id = new ArrayList();
        for (int i = 8; i<metadata.length; i++){
            block_id.add(Integer.parseInt(metadata[i]));
        }
        return new FileDesc(id, filepath, mode, size, create_time, modified_time, access_time, data_node, block_id);
    }
}
