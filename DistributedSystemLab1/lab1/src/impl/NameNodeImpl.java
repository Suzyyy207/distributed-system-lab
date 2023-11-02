package impl;
//TODO: your implementation
import api.NameNodePOA;
import java.util.HashMap;
import utils;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

// todo： NameNode要有一个函数实现接受DN的block id信息
public class NameNodeImpl extends NameNodePOA {
    private List<FileDesc> file_descriptor;   //元数据列表
    private HashMap<String, int> path_descriptor;   //file_path -> descriptor

    //初始化，读取fsimage并载入
    public NameNodeImpl(){
        String filepath = "fsimage.json";
        ObjectMapper objectMapper = new ObjectMapper();
        path_descriptor = new HashMap<>();

        try {
            List<FileDesc> my_files = objectMapper.readValue(new File(filepath), new TypeReference<List<FileDesc>>() {});
            for (FileDesc file : my_files) {
                path_descriptor.put(file.getFilepath(),file.getId());
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("load fsimage error");
        }

    }

    @Override
    public String open(String filepath, int mode) {
        int descriptor_id = this.path_descriptor.get(filepath);

        //对于不存在的文件，创建，file的id是index+1
        if (descriptor_id == null){
            int next_id = file_descriptor.length + 1;
            long time_now = System.currentTimeMillis();
            //todo: 怎么roll data node
            int data_node = 0;
            List<int> block_id = new ArrayList<>();
            block_id.add(-1);
            this.file_descriptor.add(new FileDesc(next_id,mode,0,time_now,
                    time_now,time_now,filepath,data_node,block_id));
            descriptor_id = next_id;
        }
        FileDesc file = file_descriptor.get(descriptor_id - 1);

        //检查文件是否以w模式open过
        if (file.getMode() & 0b10){
            return null;
        }
        file.setMode(mode);

        //todo：传回需要的信息
        String meta_data = file.toString();
        return meta_data;
    }

    public void modifyBlockID(String filepath, int new_block_id){
        int descriptor_id = this.path_descriptor.get(filepath);
        FileDesc file = file_descriptor.get(descriptor_id - 1);
        file.addBlockID(new_block_id);
    }

    @Override
    public void close(String filepath) {
        int descriptor_id = this.path_descriptor.get(filepath);
        FileDesc file = file_descriptor.get(descriptor_id - 1);

        //修改元数据：访问时间/修改时间/模式
        long time_now = System.currentTimeMillis();
        file.setAccess_time(time_now);
        if (file.mode & 0b10){
            file.setModified_time(time_now);
        }
        file.setMode(0b00);

        //更新fsimage
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(new File("fsimage.json"), this.file_descriptor);
            System.out.println("save fsimage");
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
