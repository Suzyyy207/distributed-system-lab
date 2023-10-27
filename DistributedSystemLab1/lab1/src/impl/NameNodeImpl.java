package impl;
//TODO: your implementation
import api.NameNodePOA;
import java.util.HashMap;
import utils;

public class NameNodeImpl extends NameNodePOA {
    private List<FileDesc> file_descriptor;   //元数据列表
    private HashMap<String, int> path_descriptor;   //file_path -> descriptor

    public NameNodeImpl(String fsimage_path){
        //载入fsimage
    }

    @Override
    public String open(String filepath, int mode) {
        int descriptor_id = this.path_descriptor.get(filepath);

        if(descriptor_id == null){
            int next_id = file_descriptor.length + 1;
            long time_now = System.currentTimeMillis();
            this.file_descriptor.add(new FileDescr(next_id,mode,0,time_now,time_now,time_now));
            descriptor_id = next_id;
        }
        FileDesc file = file_descriptor[descriptor_id];

        if (file.getMode() & 0b10){
            return null;
        }
        file.setMode(mode);

        //传回id
        String fd = file.toString();
        return fd;
    }


    @Override
    public void close(String file_info) {
        FileDesc file = file_descriptor.get(file_info);

        time_now = System.currentTimeMillis();
        file.last_access_time = time_now;
        if (file.mode & 0b10){
            file.last_modified_time = time_now;
        }
        file.mode = 0b00;

        //todo：更新fsimage信息

    }
}
