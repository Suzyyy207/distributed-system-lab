package impl;
//TODO: your implementation
import api.Client;
public class ClientImpl implements Client{
    private NameNode name_node;
    private DataNode[] data_nodes = new DataNode[MAX_DATA_NODE];
    private List<FileDesc> my_files;
    private HashMap<int, FileDesc> fd_file;

    //设定：map的时候将新文件的fd map为现在文件的长度+1
    @Override
    public int open(String filepath, int mode) {
        String meta_data_str;
        meta_data_str = this.name_node.open(filepath, mode);
        if (meta_data_str == null){
            return -1;
        }

        FileDesc file = new FileDesc();
        file = file.fromString(meta_data_str);
        this.my_files.add(file);
        int fd = this.fd_file.size() + 1;
        this.fd_file.put(fd, file);

        return fd;
    }

    @Override
    public void append(int fd, byte[] bytes) {

    }

    @Override
    public byte[] read(int fd) {
        return new byte[0];
    }

    @Override
    public void close(int fd) {
        FileDesc file = this.fd_file.get(fd);
        String filepath = file.getFilepath();
        this.name_node.close(file);
        this.fd_file.remove(fd);
        this.my_files.remove(file);
    }
}
