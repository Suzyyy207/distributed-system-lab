package impl;
//TODO: your implementation
import api.*;
import utils.*;
import java.util.HashMap;
import java.util.Arrays;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.*;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NameComponent;
import java.util.Properties;

public class ClientImpl implements Client{
    private NameNode name_node;
    private DataNode[] data_nodes = new DataNode[MAX_DATA_NODE];
    private int fd_count;
    private List<FileDesc> my_files;
    private HashMap<Integer, FileDesc> fd_file;
    private static final int MAX_DATA_NODE = 2;

    public ClientImpl() {
        this.my_files = new ArrayList<>();
        this.fd_file = new HashMap<>();
        this.fd_count = 0;
        try {
            String[] args = {};
            Properties properties = new Properties();
            properties.put("org.omg. CORBA. ORBInitialHost", "127.0.0.1"); //ORB IP
            properties.put("org.omg. CORBA. ORBInitialPort", "900");

            ORB orb = ORB.init(args, properties);

            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            this.name_node = NameNodeHelper.narrow(ncRef.resolve_str("NameNode"));
            System.out.println("NameNode is obtained.");

            for (int dataNodeId = 0; dataNodeId < MAX_DATA_NODE; dataNodeId++) {
                data_nodes[dataNodeId] = DataNodeHelper.narrow(ncRef.resolve_str("DataNode" + (dataNodeId+1)));
                System.out.println("DataNode" + (dataNodeId+1) + " is obtained.");
            }

        }  catch (Exception e) {
            e.printStackTrace();
        }
    }

    //设定：map的时候将新文件的fd map为现在文件的长度+1
    @Override
    public int open(String filepath, int mode) {
        String meta_data_str;
        meta_data_str = this.name_node.open(filepath, mode);
        if (meta_data_str.equals("")){
            return -1;
        }
        FileDesc file = FileDesc.fromString(meta_data_str);

        this.my_files.add(file);
        this.fd_count += 1;
        int fd = this.fd_count;
        this.fd_file.put(fd, file);

        return fd;
    }

    @Override
    public int append(int fd, byte[] bytes) {
        FileDesc file = this.fd_file.get(fd);

        //检查是否打开
        if (file == null){
            return -1;
        }

        //检查权限
        int check = file.getMode();
        if ((check & 0b10) == 0){
            return -1;
        }

        String filepath = file.getFilepath();
        int data_node_id = file.getData_node();
        List<Integer> blocks_id = file.getBlock_id();

        int append_id = blocks_id.get(blocks_id.size() - 1);
        int data_length = bytes.length;
        for (int i=0; i<data_length; i = i+4*1024){
            int end =  (i+4*1024 < data_length) ? (i+4*1024) : data_length;
            byte[] data = new byte[4*1024];
            System.arraycopy(bytes, i, data, 0, end-i);
            int new_id = this.data_nodes[data_node_id].append(append_id, data);
            if (new_id != -1){
                name_node.modifyBlockID(filepath, new_id);
                file.addBlockID(new_id);
                append_id = new_id;
            }
        }

        return 0;

    }

    @Override
    public byte[] read(int fd) {
        //检查是否open过
        FileDesc file = this.fd_file.get(fd);
        if (file == null){
            return null;
        }

        //检查权限
        int check = file.getMode();
        if ((check & 0b01) == 0b00){
            return null;
        }

        int data_node_id = file.getData_node();
        List<Integer> blocks_id = file.getBlock_id();

        //和DN联系并拼数据
        byte[] old_data = new byte[0];
        int all_data_len = 0;
        for (int id: blocks_id){
            System.out.println(id);
            byte[] data = data_nodes[data_node_id].read(id);
            all_data_len += data.length;
        }

        byte[] all_data = new byte[all_data_len];
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
        //只有写修改namenode的mode
        if ((file.getMode() & 0b10) != 0b00){
            this.name_node.close(filepath);
        }
        this.fd_file.remove(fd);
        this.my_files.remove(file);
    }
}
