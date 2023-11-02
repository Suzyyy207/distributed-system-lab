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
        FileDesc file = this.fd_file.get(fd);

        //检查是否打开
        if (file == null){
            return -1;
        }

        //检查权限
        int check = file.getMode();
        if (!(check & 0b10)){
            return -1;
        }

        String filepath = file.getFilepath();
        int data_node_id = file.getData_node();
        List<int> blocks_id = file.getBlock_id();

        int append_id = blocks_id.get(blocks_id.size() - 1);
        int data_length = bytes.length;
        for (int i=0; i<data_length; i = i+4*1024){
            int end =  (i+4*1024 < data_length) ? (i+4*1024) : data_length;
            byte[] data = Arrays.copyOfRange(bytes, i, end);
            int new_id = this.data_nodes[data_node_id].append(append_id, data);
            if (new_id != -1){
                name_node.modifyBlockID(filepath, new_id);
                append_id = new_id;
            }
        }

    }

    @Override
    public byte[] read(int fd) {
        //检查是否open过
        FileDesc file = this.fd_file.get(fd);
        if (file == null){
            return -1;
        }

        //检查权限
        int check = file.getMode();
        if (!(check & 0b01)){
            return -1;
        }

        int data_node_id = file.getData_node();
        List<int> blocks_id = file.getBlock_id();

        //和DN联系并拼数据
        byte[] old_data = new byte[0];
        int all_data_len = 0;
        for (int id: blocks_id){
            byte[] data = data_nodes[data_node_id].read(id);
            all_data_len += data.length;
        }

        byte[] = new byte[all_data_len];
        int index = 0;
        for (int id: blocks_id){
            byte[] new_data = data_nodes[data_node_id].read(id);
            System.arraycopy(new_data, 0, all_data, index, new_data.length);
            index += new_data.length;
        }

        return all_data;
    }

    @Override
    public void close(int fd) {
        FileDesc file = this.fd_file.get(fd);
        if (file == null){
            return;
        }
        String filepath = file.getFilepath();
        this.name_node.close(filepath);
        this.fd_file.remove(fd);
        this.my_files.remove(file);
    }
}
