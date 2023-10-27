package utils;

//TODO: According to your design, complete the FileDesc class, which wraps the information returned by NameNode open()
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
    final long id;    //每个文件的id
    int mode;
    long size; // 文件大小
    long create_time; // 创建时间
    long modified_time; // 最后修改时间
    long access_time; // 最后访问时间
    Map<int, List<int>> dn_block; //文件数据块所在的dn与block id，采取一一对应关系
    //List<int> data_nodes; // 文件的数据块所在的DataNode列表
    //List<int> block_ids; //记录所在的block id


    //todo：这里要怎么初始化呢？
    public FileDesc(long id, int mode, long size, long create_time, long modified_time, long access_time) {
        this.id = id;
        this.mode = mode;
        this.size = size;
        this.create_time = create_time;
        this.modified_time = modified_time;
        this.access_time = access_time;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    /* The following method is for conversion,
            so we can have interface that return string, which is easy to write in idl */
    //这两个函数在干什么？？？
    @Override
    public String toString() {
        String fd;
        fd = Integer.toString(this.id);
        return fd;
    }

    public static FileDesc fromString(String str){
        return null;
    }
}
